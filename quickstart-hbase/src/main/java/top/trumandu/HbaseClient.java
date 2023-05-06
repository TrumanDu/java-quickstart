package top.trumandu;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.exceptions.HBaseException;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.util.*;

/**
 * @author Truman.P.Du
 * @date 2023/04/28
 * @description
 */
@Component
@SuppressWarnings("unused")
public class HbaseClient implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(HbaseClient.class);

    @Value("${db.hbase.host:127.0.0.1}")
    private String hbaseHost;
    @Value("${db.hbase.port:2181}")
    private String hbasePort;
    private static final String FAMILY_COLUMN_SEPARATOR = ":";

    protected Connection connection = null;


    //此代码为了防止 windows下 不设置 hadoop.home.dir 会报错
    //但设不设值都不影响程序执行
    static {
        String osName = System.getProperty("os.name");
        String split = "!";
        String windowStr = "Windows";
        if (osName.startsWith(windowStr)) {
            try {
                String hadoop = Objects.requireNonNull(HbaseClient.class.getResource("/hadoop")).getPath();
                if (hadoop == null || hadoop.contains(split)) {
                    String userDir = System.getProperty("user.dir");
                    hadoop = "/" + userDir + "/config/hadoop";
                }
                hadoop = java.net.URLDecoder.decode(hadoop, "utf-8");
                System.setProperty("hadoop.home.dir", hadoop);
            } catch (Exception e) {
                LOGGER.error("set hadoopHome error", e);
            }
        }
    }

    interface ScanWatch {
        boolean onResult(String key, Map<String, String> map) throws Exception;
    }

    @PostConstruct
    private void init() {
        Configuration hbaseConfig = HBaseConfiguration.create();
        hbaseConfig.set("hbase.zookeeper.quorum", hbaseHost);
        hbaseConfig.set("hbase.zookeeper.property.clientPort", hbasePort);
        hbaseConfig.setInt("hbase.rpc.timeout", 60000);
        hbaseConfig.setInt("hbase.client.retries.number", 3);
        UserGroupInformation.setLoginUser(UserGroupInformation.createRemoteUser("xaetbd"));
        try {
            this.connection = ConnectionFactory.createConnection(hbaseConfig);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("HBase connection init success");
    }

    public boolean ping(String tableName) {
        try {
            Table t = connection.getTable(TableName.valueOf(tableName));
            t.get(new Get(Bytes.toBytes("null")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 查询数据
     */
    public Map<String, String> get(String tableName, String key) throws HBaseException {
        if (key == null) {
            return null;
        }
        Table table = getTable(tableName);
        try {
            Get get = new Get(Bytes.toBytes(key));
            Result result = table.get(get);
            if (result.isEmpty()) {
                return null;
            }
            List<Cell> cells = result.listCells();
            Map<String, String> returns = new HashMap<>(16);
            for (Cell cell : cells) {
                String family = Bytes.toString(CellUtil.cloneFamily(cell));
                String name = Bytes.toString(CellUtil.cloneQualifier(cell));
                String value = Bytes.toString(CellUtil.cloneValue(cell));
                returns.put(family + ":" + name, value);
            }
            return returns;
        } catch (Exception e) {
            throw new HBaseException("get data error:" + key, e);
        } finally {
            closeTable(table);
        }
    }

    /**
     * 是否存在
     */
    public boolean exist(String tableName, String key) throws HBaseException {
        Table table = getTable(tableName);
        try {
            Get get = new Get(Bytes.toBytes(key));
            Result result = table.get(get);
            return result.getExists() && !result.isEmpty();
        } catch (Exception e) {
            throw new HBaseException("exist error:" + key, e);
        } finally {
            closeTable(table);
        }
    }

    /**
     * 插入数据
     */
    public void put(String tableName, String key, Map<String, String> map) throws HBaseException {
        Table table = getTable(tableName);
        try {
            table.delete(new Delete(Bytes.toBytes(key)));
            Put put = new Put(Bytes.toBytes(key));
            for (String name : map.keySet()) {
                String[] family = name.split(":");
                String value = map.get(name);
                put.addColumn(Bytes.toBytes(family[0]), Bytes.toBytes(family[1]), Bytes.toBytes(value));
            }
            table.put(put);
        } catch (Exception e) {
            throw new HBaseException("put data error:" + key, e);
        } finally {
            closeTable(table);
        }
    }

    /**
     * 插入数据
     */
    public void putAll(String tableName, List<Put> puts) throws HBaseException {
        Table table = getTable(tableName);
        try {
            List<Delete> deletes = new ArrayList<>();
            for (Put put : puts) {
                deletes.add(new Delete(put.getRow()));
            }
            table.delete(deletes);
            table.put(puts);
        } catch (Exception e) {
            throw new HBaseException("put all data error", e);
        } finally {
            closeTable(table);
        }
    }

    /**
     * startWith过滤
     */
    public void scan(String tableName, String startWith, ScanWatch watch) throws HBaseException {
        if (watch == null) {
            return;
        }
        Table table = getTable(tableName);
        try {
            Scan scan = new Scan();
            if (startWith != null && !"".equals(startWith)) {
                scan.setRowPrefixFilter(Bytes.toBytes(startWith));
            }
            scan.setCaching(100);
            ResultScanner scanner = table.getScanner(scan);
            Result result;
            while ((result = scanner.next()) != null) {
                String key = Bytes.toString(result.getRow());
                if (!result.isEmpty()) {
                    List<Cell> cells = result.listCells();
                    Map<String, String> returns = new HashMap<>(16);
                    for (Cell cell : cells) {
                        String family = Bytes.toString(CellUtil.cloneFamily(cell));
                        String name = Bytes.toString(CellUtil.cloneQualifier(cell));
                        String value = Bytes.toString(CellUtil.cloneValue(cell));
                        returns.put(family + ":" + name, value);
                    }
                    if (!watch.onResult(key, returns)) {
                        return;
                    }
                }
            }
        } catch (Exception e) {
            throw new HBaseException("scan data startWith:" + startWith, e);
        } finally {
            closeTable(table);
        }
    }

    /**
     * scan column
     */
    public void scanColumn(String tableName, String startWith, String[] columns, ScanWatch watch) throws HBaseException {
        if (watch == null) {
            return;
        }
        Table table = getTable(tableName);
        try {
            Scan scan = new Scan();
            if (startWith != null && !"".equals(startWith)) {
                scan.setRowPrefixFilter(Bytes.toBytes(startWith));
            }
            if (Objects.nonNull(columns)) {
                for (String column : columns) {
                    String[] cf = column.split(FAMILY_COLUMN_SEPARATOR);
                    if (cf.length == 2) {
                        scan.addColumn(Bytes.toBytes(cf[0]), Bytes.toBytes(cf[1]));
                    }
                }
            }
            scan.setCaching(100);
            ResultScanner scanner = table.getScanner(scan);
            Result result;
            while ((result = scanner.next()) != null) {
                String key = Bytes.toString(result.getRow());
                if (!result.isEmpty()) {
                    List<Cell> cells = result.listCells();
                    Map<String, String> returns = new HashMap<>(16);
                    for (Cell cell : cells) {
                        String family = Bytes.toString(CellUtil.cloneFamily(cell));
                        String name = Bytes.toString(CellUtil.cloneQualifier(cell));
                        String value = Bytes.toString(CellUtil.cloneValue(cell));
                        returns.put(family + ":" + name, value);
                    }
                    if (!watch.onResult(key, returns)) {
                        return;
                    }
                }
            }
        } catch (Exception e) {
            throw new HBaseException("scan data startWith:" + startWith, e);
        } finally {
            closeTable(table);
        }
    }

    /**
     * 正则过滤
     */
    public void columnValueRegex(String tableName, String filterFamily, String filterColumn, String regex, ScanWatch watch) throws HBaseException {
        if (watch == null) {
            return;
        }
        Table table = getTable(tableName);
        try {
            Scan scan = new Scan();
            if (regex != null && !"".equals(regex)) {
                SingleColumnValueFilter columnValueFilter = new SingleColumnValueFilter(
                        Bytes.toBytes(filterFamily), Bytes.toBytes(filterColumn), CompareFilter.CompareOp.EQUAL, new RegexStringComparator(regex));
                scan.setFilter(columnValueFilter);
            }
            scan.setCaching(100);
            ResultScanner scanner = table.getScanner(scan);
            Result result;
            while ((result = scanner.next()) != null) {
                String key = Bytes.toString(result.getRow());
                if (!result.isEmpty()) {
                    List<Cell> cells = result.listCells();
                    Map<String, String> returns = new HashMap<>();
                    for (Cell cell : cells) {
                        String family = Bytes.toString(CellUtil.cloneFamily(cell));
                        String name = Bytes.toString(CellUtil.cloneQualifier(cell));
                        String value = Bytes.toString(CellUtil.cloneValue(cell));
                        returns.put(family + ":" + name, value);
                    }
                    if (!watch.onResult(key, returns)) {
                        return;
                    }
                }
            }
        } catch (Exception e) {
            throw new HBaseException("scan data regex:" + regex, e);
        } finally {
            closeTable(table);
        }
    }

    /**
     * 正则过滤
     */
    public void scanRegex(String tableName, String regex, ScanWatch watch) throws HBaseException {
        if (watch == null) {
            return;
        }
        Table table = getTable(tableName);
        try {
            Scan scan = new Scan();
            if (regex != null && !"".equals(regex)) {
                RowFilter filter = new RowFilter(CompareFilter.CompareOp.EQUAL, new RegexStringComparator(regex));
                scan.setFilter(filter);
            }
            scan.setCaching(100);
            ResultScanner scanner = table.getScanner(scan);
            Result result;
            while ((result = scanner.next()) != null) {
                String key = Bytes.toString(result.getRow());
                if (!result.isEmpty()) {
                    List<Cell> cells = result.listCells();
                    Map<String, String> returns = new HashMap<>();
                    for (Cell cell : cells) {
                        String family = Bytes.toString(CellUtil.cloneFamily(cell));
                        String name = Bytes.toString(CellUtil.cloneQualifier(cell));
                        String value = Bytes.toString(CellUtil.cloneValue(cell));
                        returns.put(family + ":" + name, value);
                    }
                    if (!watch.onResult(key, returns)) {
                        return;
                    }
                }
            }
        } catch (Exception e) {
            throw new HBaseException("scan data regex:" + regex, e);
        } finally {
            closeTable(table);
        }
    }

    /**
     * scan column
     */
    public void scanByTimestamp(String tableName, String column, long startTimestamp, long endTimestamp, ScanWatch watch) throws HBaseException {
        if (watch == null) {
            return;
        }
        Table table = getTable(tableName);
        try {
            Scan scan = new Scan();
            if (Objects.nonNull(column)) {
                String[] cf = column.split(FAMILY_COLUMN_SEPARATOR);
                if (cf.length == 2) {
                    scan.addColumn(Bytes.toBytes(cf[0]), Bytes.toBytes(cf[1]));
                }
            }
            scan.setTimeRange(startTimestamp, endTimestamp);
            scan.setCaching(100);
            ResultScanner scanner = table.getScanner(scan);
            Result result;
            while ((result = scanner.next()) != null) {
                String key = Bytes.toString(result.getRow());
                if (!result.isEmpty()) {
                    List<Cell> cells = result.listCells();
                    Map<String, String> returns = new HashMap<>();
                    for (Cell cell : cells) {
                        String family = Bytes.toString(CellUtil.cloneFamily(cell));
                        String name = Bytes.toString(CellUtil.cloneQualifier(cell));
                        String value = Bytes.toString(CellUtil.cloneValue(cell));
                        returns.put(family + ":" + name, value);
                    }
                    if (!watch.onResult(key, returns)) {
                        return;
                    }
                }
            }
        } catch (Exception e) {
            throw new HBaseException("scan by data error.", e);
        } finally {
            closeTable(table);
        }
    }

    /**
     * 创建表
     *
     * @param tableName  表名
     * @param familyName family
     */
    public boolean createTable(String tableName, String familyName) {
        try (Admin admin = connection.getAdmin()) {
            HTableDescriptor table = new HTableDescriptor(Bytes.toBytes(tableName));
            HColumnDescriptor family = new HColumnDescriptor(Bytes.toBytes(familyName));
            table.addFamily(family);
            admin.createTable(table);
            return true;
        } catch (Exception exception) {
            LOGGER.warn("create table has failed.{}", tableName, exception);
            return false;
        }
    }

    public boolean clearTable(String tableName) {
        try (Admin admin = connection.getAdmin()) {
            TableName table = TableName.valueOf(tableName);
            admin.disableTable(table);
            admin.truncateTable(table, Boolean.TRUE);
            return true;
        } catch (Exception exception) {
            LOGGER.warn("clear table has failed.{}", tableName, exception);
            return false;
        }
    }

    /**
     * 删除数据
     */
    public void delete(String tableName, String key) throws HBaseException {
        Table table = getTable(tableName);
        try {
            Delete delete = new Delete(Bytes.toBytes(key));
            table.delete(delete);
        } catch (Exception e) {
            throw new HBaseException("delete error:" + key, e);
        } finally {
            closeTable(table);
        }
    }

    public void deleteAll(String tableName, List<Delete> deleteList) throws HBaseException {
        Table table = getTable(tableName);
        try {
            table.delete(deleteList);
        } catch (Exception e) {
            throw new HBaseException("delete all error", e);
        } finally {
            closeTable(table);
        }
    }

    public List<String> getTableList() throws IOException {
        List<String> tableNames = new ArrayList<>();
        for (TableName table : connection.getAdmin().listTableNames()) {
            tableNames.add(table.getNameAsString());
        }
        return tableNames;
    }


    private Table getTable(String table) throws HBaseException {
        try {
            return connection.getTable(TableName.valueOf(table));
        } catch (Exception e) {
            throw new HBaseException("HBase getTable exception", e);
        }
    }

    /**
     * 关闭到表的连接
     */
    protected void closeTable(Table table) {
        try {
            if (table != null) {
                table.close();
            }
        } catch (Exception e) {
            LOGGER.error("HBase close exception", e);
        }
    }

    @Override
    @PreDestroy
    public void close() {
        try {
            if (connection != null) {
                connection.close();
                LOGGER.info("Shutdown HBase Connection");
            }
        } catch (Exception e) {
            LOGGER.error("HBase close exception", e);
        }
    }
}
