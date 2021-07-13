package top.trumandu;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Truman.P.Du
 * @date 2021/06/09
 * @description
 */
public class KafkaAdminTemplate implements Closeable {
    private AdminClient adminClient;
    private String bootstrapServers;
    private static final long DEFAULT_TIMEOUT_MS = 120000;

    public KafkaAdminTemplate(String bootstrapServers) {
        this(bootstrapServers, DEFAULT_TIMEOUT_MS);
    }

    public KafkaAdminTemplate(String bootstrapServers, long requestTimeoutMs) {
        this.bootstrapServers = bootstrapServers;
        Properties adminProps = new Properties();
        adminProps.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        adminProps.setProperty(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, String.valueOf(requestTimeoutMs));
        this.adminClient = KafkaAdminClient.create(adminProps);
    }

    public KafkaAdminTemplate(Properties adminProps) {
        this.adminClient = KafkaAdminClient.create(adminProps);
    }

    public AdminClient getAdminClient() {
        return this.adminClient;
    }

    /**
     * 创建topic
     *
     * @throws Exception
     */
    public boolean createTopic(String topicName, int numPartitions, short replicationFactor) throws Exception {
        return this.createTopic(topicName, numPartitions, replicationFactor, null);
    }

    public boolean createTopic(String topicName, int numPartitions, short replicationFactor, long ttl)
            throws Exception {
        Map<String, String> configs = new HashMap<>(1 << 3);
        configs.put(TopicConfig.RETENTION_MS_CONFIG, ttl + "");
        configs.put(TopicConfig.DELETE_RETENTION_MS_CONFIG, ttl + "");
        return this.createTopic(topicName, numPartitions, replicationFactor, configs);
    }

    /**
     * 精确创建 topic 通过 Map<String, String> configs 来指定 eg: 旧日志段的保留测率，删除或压缩，此时选择删除
     * topicConfig.put(TopicConfig.CLEANUP_POLICY_CONFIG,TopicConfig.CLEANUP_POLICY_DELETE);
     * 过期数据的压缩方式，如果上面选项为压缩的话才有效
     * topicConfig.put(TopicConfig.COMPRESSION_TYPE_CONFIG,"snappy"); The amount of
     * time to retain delete tombstone markers for log compacted topics. This
     * setting also gives a bound on the time in which a consumer must complete a
     * read if they begin from offset 0 to ensure that they get a valid snapshot of
     * the final stage (otherwise delete tombstones may be collected before they
     * complete their scan). 默认1天
     * <p>
     * topicConfig.put(TopicConfig.DELETE_RETENTION_MS_CONFIG,"86400000");
     * 文件在文件系统上被删除前的保留时间，默认为60秒
     * topicConfig.put(TopicConfig.FILE_DELETE_DELAY_MS_CONFIG,"60000");
     * 将数据强制刷入日志的条数间隔
     * topicConfig.put(TopicConfig.FLUSH_MESSAGES_INTERVAL_CONFIG,"9223372036854775807");
     * 将数据强制刷入日志的时间间隔
     * topicConfig.put(TopicConfig.FLUSH_MS_CONFIG,"9223372036854775807"); offset设置
     * topicConfig.put(TopicConfig.INDEX_INTERVAL_BYTES_CONFIG,"4096"); 每个批量消息最大字节数
     * topicConfig.put(TopicConfig.MAX_MESSAGE_BYTES_CONFIG,"1000012");
     * 记录标记时间与kafka本机时间允许的最大间隔，超过此值的将被拒绝
     * topicConfig.put(TopicConfig.MESSAGE_TIMESTAMP_DIFFERENCE_MAX_MS_CONFIG,"9223372036854775807");
     * 标记时间类型，是创建时间还是日志时间 CreateTime/LogAppendTime
     * topicConfig.put(TopicConfig.MESSAGE_TIMESTAMP_TYPE_CONFIG,"CreateTime");
     * 如果日志压缩设置为可用的话，设置日志压缩器清理日志的频率。默认情况下，压缩比率超过50%时会避免清理日志。
     * 此比率限制重复日志浪费的最大空间，设置为50%，意味着最多50%的日志是重复的。更高的比率设置意味着更少、更高效 的清理，但会浪费更多的磁盘空间。
     * topicConfig.put(TopicConfig.MIN_CLEANABLE_DIRTY_RATIO_CONFIG,"0.5");
     * 消息在日志中保持未压缩状态的最短时间，只对已压缩的日志有效
     * topicConfig.put(TopicConfig.MIN_COMPACTION_LAG_MS_CONFIG,"0");
     * 当一个producer的ack设置为all（或者-1）时，此项设置的意思是认为新记录写入成功时需要的最少副本写入成功数量。
     * 如果此最小数量没有达到，则producer抛出一个异常（NotEnoughReplicas
     * 或者NotEnoughReplicasAfterAppend）。 你可以同时使用min.insync.replicas
     * 和ack来加强数据持久话的保障。一个典型的情况是把一个topic的副本数量设置为3,
     * min.insync.replicas的数量设置为2,producer的ack模式设置为all，这样当没有足够的副本没有写入数据时，producer会抛出一个异常。
     * topicConfig.put(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG,"1");
     * 如果设置为true，会在新日志段创建时预分配磁盘空间
     * topicConfig.put(TopicConfig.PREALLOCATE_CONFIG,"true");
     * 当保留策略为删除（delete）时，此设置控制在删除就日志段来清理磁盘空间前，保存日志段的partition能增长到的最大尺寸。
     * 默认情况下没有尺寸大小限制，只有时间限制。。由于此项指定的是partition层次的限制，它的数量乘以分区数才是topic层面保留的数量。
     * topicConfig.put(TopicConfig.RETENTION_BYTES_CONFIG,"-1");
     * 当保留策略为删除（delete）时，此设置用于控制删除旧日志段以清理磁盘空间前，日志保留的最长时间。默认为7天。
     * 这是consumer在多久内必须读取数据的一个服务等级协议（SLA）。
     * topicConfig.put(TopicConfig.RETENTION_MS_CONFIG,"604800000");
     * 此项用于控制日志段的大小，日志的清理和持久话总是同时发生，所以大的日志段代表更少的文件数量和更小的操作粒度。
     * topicConfig.put(TopicConfig.SEGMENT_BYTES_CONFIG,"1073741824");
     * 此项用于控制映射数据记录offsets到文件位置的索引的大小。我们会给索引文件预先分配空间，然后在日志滚动时收缩它。 一般情况下你不需要改动这个设置。
     * topicConfig.put(TopicConfig.SEGMENT_INDEX_BYTES_CONFIG,"10485760");
     * 从预订的段滚动时间中减去最大的随机抖动，避免段滚动时的惊群（thundering herds）
     * topicConfig.put(TopicConfig.SEGMENT_JITTER_MS_CONFIG,"0");
     * 此项用户控制kafka强制日志滚动时间，在此时间后，即使段文件没有满，也会强制滚动，以保证持久化操作能删除或压缩就数据。默认7天
     * topicConfig.put(TopicConfig.SEGMENT_MS_CONFIG,"604800000");
     * 是否把一个不在isr中的副本被选举为leader作为最后手段，即使这样做会带来数据损失
     * topicConfig.put(TopicConfig.UNCLEAN_LEADER_ELECTION_ENABLE_CONFIG,"false");
     *
     * @throws Exception
     */
    private boolean createTopic(String topicName, int numPartitions, short replicationFactor,
                                Map<String, String> topicConfig) throws Exception {
        boolean success = false;
        NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);
        newTopic.configs(topicConfig);
        CreateTopicsResult createTopicResult = this.adminClient.createTopics(Collections.singleton(newTopic));

        try {
            createTopicResult.values().get(topicName).get();
            success = true;
        } catch (Exception e) {
            throw e;
        }
        return success;
    }

    /**
     * 检查是否存在topic
     *
     * @param topicName
     * @return
     * @throws Exception
     */
    public boolean existsTopic(String topicName) throws Exception {
        Set<String> topics = this.adminClient.listTopics().names().get();
        return topics.contains(topicName);
    }

    public List<TopicPartitionInfo> listTopicPartition(String topicName) throws Exception {
        return this.adminClient.describeTopics(Collections.singleton(topicName)).values().get(topicName).get().partitions();
    }


    /**
     * 获取集群 node节点信息
     *
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public List<Node> nodes() throws ExecutionException, InterruptedException {
        List<Node> nodes = new ArrayList<>(this.adminClient.describeCluster().nodes().get());
        return nodes;
    }

    /**
     * 根据Topic获取LogSize之和
     *
     * @param topic
     * @return
     */
    public long getLogSize(String topic) throws Exception {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "sns_sys_temp_group");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        KafkaConsumer<String, String> consumer = null;
        long size = 0;
        try {
            consumer = new KafkaConsumer<>(consumerProps);
            List<PartitionInfo> partitions = consumer.partitionsFor(topic);
            if (Objects.isNull(partitions)) {
                return 0;
            }
            List<TopicPartition> topicPartitions = new ArrayList<>();
            partitions.forEach(partition -> {
                TopicPartition topicPartition = new TopicPartition(topic, partition.partition());
                topicPartitions.add(topicPartition);
            });
            Map<TopicPartition, Long> result = consumer.endOffsets(topicPartitions);
            for (Long value : result.values()) {
                size = size + value.longValue();
            }
        } finally {
            if (consumer != null) {
                consumer.close();
            }

        }
        return size;
    }


    public long getConsumerGroupLagByTopic(String topic, String groupId) throws Exception {
        long logSize = getLogSize(topic);
        if (logSize == 0) {
            return 0;
        }
        Set<Map.Entry<TopicPartition, OffsetAndMetadata>> consumerPartitionOffsets = adminClient.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata()
                .get(60, TimeUnit.SECONDS).entrySet()
                .stream().filter(entry -> topic.equalsIgnoreCase(entry.getKey().topic()))
                .collect(Collectors.toSet());
        if (Objects.isNull(consumerPartitionOffsets)) {
            return 0;
        }
        for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : consumerPartitionOffsets) {
            logSize = logSize - entry.getValue().offset();
        }
        return logSize;
    }

    @Override
    public void close() throws IOException {
        adminClient.close();
    }
}
