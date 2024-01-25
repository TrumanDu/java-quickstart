package top.trumandu;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Truman.P.Du
 * @date 2021/06/09
 */
@SuppressWarnings("unused")
public class KafkaConsumerTemplate<K, V> implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerTemplate.class);
    private static final AtomicInteger CONSUMER_CLIENT_ID_SEQUENCE = new AtomicInteger(1);
    ThreadFactory threadFactory;
    private final List<ConsumerThread<K, V>> consumers = new ArrayList<>();

    private final String hosts;
    private final String topic;
    private final String group;
    private final MessageWatched<K, V> watched;
    private final Properties consumerProperties;

    public KafkaConsumerTemplate(String hosts, String topic, String group, int threadSize, MessageWatched<K, V> watched) {
        this(hosts, topic, group, threadSize, watched, null);
    }


    public KafkaConsumerTemplate(String hosts, String topic, String group, int threadSize, MessageWatched<K, V> watched, Properties consumerProperties) {
        this.hosts = hosts;
        this.topic = topic;
        this.group = group;
        this.watched = watched;
        this.consumerProperties = consumerProperties;
        threadFactory = new ThreadFactoryBuilder().setNameFormat("consumer-" + group.toLowerCase() + "-%d").build();

        for (int i = 0; i < threadSize; i++) {
            ConsumerThread<K, V> consumerThread = new ConsumerThread<>(hosts, topic, group, watched, consumerProperties);
            threadFactory.newThread(consumerThread).start();
            consumers.add(consumerThread);
        }
    }


    public int getCurrentConsumerNum() {
        return consumers.size();
    }

    public void slowSpeed(long sleep) {
        for (ConsumerThread<K, V> consumerThread : consumers) {
            consumerThread.slowSpeed(sleep);
        }
    }

    public synchronized void addThread(int thread) {
        for (int i = 0; i < thread; i++) {
            ConsumerThread<K, V> consumerThread = new ConsumerThread<>(hosts, topic, group, watched, consumerProperties);
            threadFactory.newThread(consumerThread).start();
            consumers.add(consumerThread);
        }
    }

    public synchronized void subtractThread(int thread) {
        for (int i = 0; i < thread; i++) {
            ConsumerThread<K, V> consumerThread = consumers.get(0);
            consumerThread.close();
            consumers.remove(0);
        }
    }

    @Override
    public void close() {

        for (ConsumerThread<K, V> consumerThread : consumers) {
            consumerThread.close();
        }
        consumers.clear();
    }

    public interface MessageWatched<K, V> {
        int MAP_SIZE = 1;

        /**
         * 处理单条消息
         * @param group 消费组名称
         * @param record 消息
         * @return 如果返回false，则只代表不提交offset
         * @throws Exception 处理异常
         */
        default boolean onMessage(String group, ConsumerRecord<K, V> record) throws Exception {
            Map<TopicPartition, List<ConsumerRecord<K, V>>> map = new HashMap<>(MAP_SIZE);
            map.put(new TopicPartition(record.topic(), record.partition()), Collections.singletonList(record));
            ConsumerRecords<K, V> records = new ConsumerRecords<>(map);
            return this.onMessage(group, records);
        }

        /**
         * 处理多条消息
         *
         * @param group   消费组名称
         * @param records 消息集合
         * @return 如果返回false，则只代表不提交offset
         * @throws Exception 处理异常
         */
        boolean onMessage(String group, ConsumerRecords<K, V> records) throws Exception;
    }

    public class ConsumerThread<KK, VV> implements Runnable {
        private org.apache.kafka.clients.consumer.KafkaConsumer<KK, VV> consumer;
        private final Properties consumerProperties;
        private final MessageWatched<KK, VV> messageHandle;
        private final String topic;

        private final AtomicBoolean isStopConsumer = new AtomicBoolean(false);
        private final AtomicLong sleepMs = new AtomicLong(0);


        public ConsumerThread(String brokers, String topic, String group,
                              MessageWatched<KK, VV> messageHandle) {
            this(brokers, topic, group, messageHandle, null);
        }

        public ConsumerThread(String brokers, String topic, String group,
                              MessageWatched<KK, VV> messageHandle, Properties consumerProperties) {
            this.topic = topic;
            this.messageHandle = messageHandle;
            if (consumerProperties == null) {
                this.consumerProperties = (Properties) KafkaConsumerTemplate.buildDefaultConsumerConfig(brokers, group).clone();
            } else {
                this.consumerProperties = (Properties) consumerProperties.clone();
            }
        }

        @Override
        public void run() {
            if (!consumerProperties.containsKey(ConsumerConfig.CLIENT_ID_CONFIG)) {
                String clientId = Thread.currentThread().getName() + "_" + CONSUMER_CLIENT_ID_SEQUENCE.getAndIncrement();
                this.consumerProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
            }
            consumer = new KafkaConsumer<>(consumerProperties);
            consumer.subscribe(Collections.singletonList(topic));

            try {
                while (!isStopConsumer.get()) {
                    ConsumerRecords<KK, VV> records = consumer.poll(Duration.ofMillis(50));

                    try {
                        // 批量处理，批量提交offset
                        if (!messageHandle.onMessage(consumerProperties.getProperty(ConsumerConfig.GROUP_ID_CONFIG), records)) {
                            LOGGER.error(String.format("topic:%s, group:%s has error.", topic, consumerProperties.getProperty(ConsumerConfig.GROUP_ID_CONFIG)));
                        }
                        // 如果records为空，这里的提交可能导致报错
                        //consumer.commitAsync();
                    } catch (Exception e) {
                        LOGGER.error("ConsumerThread processing message have errors", e);
                    }

                    if (sleepMs.get() > 0) {
                        Set<TopicPartition> partitions = null;
                        try {
                            partitions = consumer.paused();
                            Thread.sleep(sleepMs.get());
                        } catch (InterruptedException e) {
                            LOGGER.error("ConsumerThread sleep {} ms InterruptedException", sleepMs.get(), e);
                        } finally {
                            if (partitions != null) {
                                consumer.resume(partitions);
                            }
                        }
                    }
                }
            } catch (WakeupException e) {
                if (!isStopConsumer.get()) {
                    throw e;
                }
            } finally {
                consumer.close();
            }
        }

        private void slowSpeed(long sleep) {
            sleepMs.set(sleep);
        }

        private void close() {
            isStopConsumer.set(true);
            consumer.wakeup();
        }
    }

    public static Properties buildDefaultConsumerConfig(String brokers, String group) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "50");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "9000");
        return props;
    }
}