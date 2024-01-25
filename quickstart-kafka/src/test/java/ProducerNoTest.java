import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;
import top.trumandu.KafkaProducerTemplate;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * @author Truman.P.Du
 * @date 2021/05/27
 * @description
 */

public class ProducerNoTest {
    @Test
    public void producer() throws ExecutionException, InterruptedException, IOException {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "127.0.0.1:9092");
        props.setProperty("key.serializer", StringSerializer.class.getName());
        props.setProperty("value.serializer", StringSerializer.class.getName());

        KafkaProducerTemplate<String, String> producer = new KafkaProducerTemplate<>(props);
        while (true){
            producer.send("truman_topic_same",System.currentTimeMillis()+"",System.currentTimeMillis()+"");
            Thread.sleep(1000);
        }
    }
}
