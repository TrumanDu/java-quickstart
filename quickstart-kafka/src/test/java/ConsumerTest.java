import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author Truman.P.Du
 * @date 2021/05/27
 * @description
 */

public class ConsumerTest {
    @Test
    public void consumer() {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "127.0.0.1:9093");
        props.setProperty("group.id", "test");
        props.setProperty("enable.auto.commit", "true");
        props.setProperty("auto.commit.interval.ms", "1000");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        props.setProperty("security.protocol", "SASL_PLAINTEXT");
        props.setProperty("sasl.mechanism", "SCRAM-SHA-256");
        props.setProperty("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required  username=\"truman\" password=\"123456\";");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("truman_test"));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
            }
        }
    }
}
