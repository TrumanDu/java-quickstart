package top.trumandu;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.hadoop.mapred.JobConf;
import org.apache.orc.OrcFile;
import org.apache.orc.Reader;
import org.apache.orc.RecordReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Truman.P.Du
 * @date 2024/01/09
 */
@Component
@SuppressWarnings("unused")
public class HdfsClient {
    JobConf conf = new JobConf();


    static {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            try {
                String hadoop = Objects.requireNonNull(HdfsClient.class.getResource("/hadoop")).getPath();
                if (hadoop == null || hadoop.contains("!")) {
                    String userdir = System.getProperty("user.dir");
                    hadoop = "/" + userdir + "/config/hadoop";
                }
                hadoop = java.net.URLDecoder.decode(hadoop, "utf-8");
                System.setProperty("hadoop.home.dir", hadoop);
            } catch (Exception ignored) {
            }
        }
    }


    public List<String> readFromOrcFile(String pathName) throws IOException {
        List<String> activeSubscriberList = new ArrayList<>(50_000);
        List<Path> paths = listDir(pathName);
        for (Path path : paths) {
            RecordReader rows = null;
            try {
                Reader reader = OrcFile.createReader(path, OrcFile.readerOptions(conf));
                rows = reader.rows();
                VectorizedRowBatch batch = reader.getSchema().createRowBatch();
                BytesColumnVector customerNumber = (BytesColumnVector) batch.cols[0];
                while (rows.nextBatch(batch)) {
                    for (int r = 0; r < batch.size; ++r) {
                        activeSubscriberList.add(customerNumber.toString(r));
                    }
                }


            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (rows != null) {
                    rows.close();
                }
            }
        }

        return activeSubscriberList;
    }

    public List<Path> listDir(String pathName) {
        List<Path> paths = new ArrayList<>();
        try (FileSystem fs = FileSystem.get(conf)) {
            Path path = new Path(pathName);
            FileStatus[] fileStatusList = fs.listStatus(path);
            for (FileStatus fileStatus : fileStatusList) {
                paths.add(fileStatus.getPath());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return paths;
    }

    public static void main(String[] args) throws IOException {
        HdfsClient hdfsClient = new HdfsClient();
        System.out.println(hdfsClient.readFromOrcFile("/user/hive/cu_activity").size());
    }
}
