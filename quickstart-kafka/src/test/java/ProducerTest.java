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

public class ProducerTest {
    @Test
    public void producer() throws ExecutionException, InterruptedException, IOException {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "127.0.0.1:9093");
        props.setProperty("key.serializer", StringSerializer.class.getName());
        props.setProperty("value.serializer", StringSerializer.class.getName());
        //props.setProperty("max.request.size", "2000000");

        props.setProperty("security.protocol", "SASL_PLAINTEXT");
        props.setProperty("sasl.mechanism", "SCRAM-SHA-256");
        props.setProperty("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required  username=\"truman\" password=\"123456\";");

        KafkaProducerTemplate<String, String> producer = new KafkaProducerTemplate<>(props);
        while (true){
            producer.send("truman_test",System.currentTimeMillis()+"",System.currentTimeMillis()+"");
            Thread.sleep(1000);
        }
    }
}
