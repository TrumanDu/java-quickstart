package origin;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * @author Truman.P.Du
 * @date 2022/03/13
 * @description
 */
public class ProducerTest {
    @Test
    public void producer() throws InterruptedException {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("linger.ms", 1);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        CountDownLatch latch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++)
            producer.send(new ProducerRecord<String, String>("my-topic", Integer.toString(i), Integer.toString(i)),(recordMetadata,exception)->{
                if(exception!=null){
                    exception.printStackTrace();
                }
                latch.countDown();
            });

        latch.await();
        producer.close();
    }
}
