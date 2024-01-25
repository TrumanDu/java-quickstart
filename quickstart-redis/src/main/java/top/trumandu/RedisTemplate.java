package top.trumandu;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Connection;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.SetParams;

import java.io.Closeable;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Truman.P.Du
 * @date 2021/06/09
 */
@SuppressWarnings("unused")
public class RedisTemplate implements Closeable {

    private final JedisCluster jedisCluster;
    private final String keyPrefix;

    private final static String CLIENT_NAME = "lab";

    private RedisTemplate(RedisBuilder builder) {
        final String[] hosts = builder.hosts.split(RedisConstants.HOST_SPLIT);
        keyPrefix = builder.prefix;
        Set<HostAndPort> nodes = new HashSet<>() {
            private static final long serialVersionUID = 5341345879054512402L;

            {
                for (String hostAndPort : hosts) {
                    String[] array = hostAndPort.split(":");
                    if (array.length > 1) {
                        add(new HostAndPort(array[0], Integer.parseInt(array[1])));
                    } else {
                        add(new HostAndPort(array[0], RedisConstants.DEFAULT_PORT));
                    }
                }
            }
        };
        this.jedisCluster = new JedisCluster(nodes, builder.timeout, builder.timeout, builder.retry, builder.password, CLIENT_NAME, buildPoolConfig());
    }

    private static class RedisConstants {
        private static final String HANDLE_SUCCESS = "OK";
        private static final String HOST_SPLIT = ",";
        private static final int DEFAULT_PORT = 6379;
    }

    private String wrapKey(String key) {
        return keyPrefix + key;
    }

    /**
     * 如果key,不存在的话写入，并且设置ttl
     *
     * @return true:不存在，写入成功，false:key存在，未写入成功。
     */
    public boolean setnx(String key, String value, long ttl) {
        SetParams setParams = new SetParams();
        setParams = setParams.ex(ttl);
        setParams = setParams.nx();
        String result = jedisCluster.set(wrapKey(key), value, setParams);
        return RedisConstants.HANDLE_SUCCESS.equalsIgnoreCase(result);
    }

    public void set(String key, String value, long ttl) {
        SetParams setParams = new SetParams();
        setParams = setParams.ex(ttl);
        String result = jedisCluster.set(wrapKey(key), value, setParams);
    }

    public boolean exists(String key) {
        return jedisCluster.exists(wrapKey(key));
    }

    public String get(String key) {
        return jedisCluster.get(wrapKey(key));
    }

    public Long incr(String key) {
        return jedisCluster.incr(wrapKey(key));
    }

    public Long del(String key) {
        return jedisCluster.del(wrapKey(key));
    }

    public Long ttl(String key) {
        return jedisCluster.ttl(wrapKey(key));
    }

    public Long sadd(String key, String... member) {
        return jedisCluster.sadd(wrapKey(key), member);
    }

    public Long scard(String key) {
        return jedisCluster.scard(wrapKey(key));
    }

    private static GenericObjectPoolConfig<Connection> buildPoolConfig() {
        GenericObjectPoolConfig<Connection> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleDuration(Duration.ofMillis(1_800_000));
        return poolConfig;
    }

    @Override
    public void close() {
        if (jedisCluster != null) {
            jedisCluster.close();
        }
    }

    public static class RedisBuilder {
        private final String hosts;
        private String prefix;
        private int timeout = JedisCluster.DEFAULT_TIMEOUT;
        private int retry = JedisCluster.DEFAULT_MAX_ATTEMPTS;
        private String password = null;

        public RedisBuilder(String hosts) {
            this.hosts = hosts;
        }

        public RedisBuilder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public RedisBuilder retry(int retry) {
            this.retry = retry;
            return this;
        }

        public RedisBuilder password(String password) {
            this.password = password;
            return this;
        }

        public RedisBuilder keyPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public RedisTemplate builder() {
            return new RedisTemplate(this);
        }
    }
}
