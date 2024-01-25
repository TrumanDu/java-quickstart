package top.trumandu;


import top.trumandu.config.HiveProperties;

import java.sql.*;

/**
 * @author Truman.P.Du
 * @date 2024/01/09
 */
@SuppressWarnings("unused")
public class HiveClient implements AutoCloseable {
    static {
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private final Connection conn;
    private final Statement stmt;

    public static HiveClient getInstance(HiveProperties hiveProperties) throws SQLException {
        return new HiveClient(hiveProperties);
    }

    private HiveClient(HiveProperties hiveProperties) throws SQLException {
        this.conn = getConnection(hiveProperties.getUrl(), hiveProperties.getUser(), hiveProperties.getPassword());
        this.stmt = conn.createStatement();
    }


    private Connection getConnection(String hiveUrl, String hiveUser, String hivePassword) throws SQLException {
        return DriverManager.getConnection(hiveUrl, hiveUser, hivePassword);
    }


    public void execute(String sql) throws SQLException {
        stmt.execute(sql);
    }


    public ResultSet executeQuery(String sql) throws SQLException {
        return stmt.executeQuery(sql);
    }

    @Override
    public void close() {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
