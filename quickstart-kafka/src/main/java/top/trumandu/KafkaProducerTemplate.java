package top.trumandu;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.Closeable;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * @author Truman.P.Du
 * @date 2021/06/09
 */
@SuppressWarnings("unused")
public class KafkaProducerTemplate<K, V> implements Closeable {
    public KafkaProducer<K, V> producer;

    /**
     * props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProducerProperties.getBootstrap());
     * props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
     * props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
     * props.put("acks", "all");
     * props.put("retries", "0");
     * props.put("batch.size", "16384");
     */
    public KafkaProducerTemplate(Properties producerProps) {
        this.producer = new KafkaProducer<>(producerProps);
    }

    public KafkaProducerTemplate(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "30000");
        //send最大阻塞10s
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "10000");
        this.producer = new KafkaProducer<>(props);
    }

    public KafkaProducer<K, V> getKafkaProducer() {
        return producer;
    }

    /**
     * 同步发送
     */
    public void syncSend(String topic, K k, V v) throws InterruptedException, ExecutionException {
        this.producer.send(new ProducerRecord<>(topic, k, v)).get();
    }

    /**
     * 同步发送
     */
    public void syncSend(String topic, K key, V value, Iterable<Header> headers) throws InterruptedException, ExecutionException {
        this.producer.send(new ProducerRecord<>(topic, null, null, key, value, headers)).get();
    }

    /**
     * 异步发送
     */
    public void send(String topic, K k, V v) {
        this.producer.send(new ProducerRecord<>(topic, k, v), (metadata, exception) -> {
            if (exception != null) {
                exception.printStackTrace();
            }
        });
    }

    /**
     * 异步发送
     */
    public void send(String topic, K key, V value, Iterable<Header> headers) {
        this.producer.send(new ProducerRecord<>(topic, null, null, key, value, headers), (metadata, exception) -> {
            if (exception != null) {
                exception.printStackTrace();
            }
        });
    }

    public void send(String topic, K key, V value, Callback callback) {
        this.send(topic, key, value, null, callback);
    }

    public void send(String topic, K key, V value, Iterable<Header> headers, Callback callback) {
        this.producer.send(new ProducerRecord<>(topic, null, null, key, value, headers), callback);
    }

    public void flush() {
        this.producer.flush();
    }

    @Override
    public void close() {
        this.producer.close();
    }
}
