package top.trumandu.example;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.io.Closeable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Truman.P.Du
 * @date 2024/01/25
 */
public class JedisSentinelDemo implements Closeable {
    private final JedisSentinelPool jedisSentinelPool;

    public JedisSentinelDemo(String masterName, String sentinels) {
        Set<String> sentinelNodes = getHostAndPortSet(sentinels);
        this.jedisSentinelPool = new JedisSentinelPool(masterName, sentinelNodes);
    }

    public JedisSentinelDemo(String masterName, String sentinels, String password) {
        Set<String> sentinelNodes = getHostAndPortSet(sentinels);
        this.jedisSentinelPool = new JedisSentinelPool(masterName, sentinelNodes, password);
    }

    /**
     * 使用jedisSentinelPool需要close资源
     * 这里使用自动关闭
     */
    public String get(String key) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            return jedis.get(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> getHostAndPortSet(String sentinels) {
        return new HashSet<String>() {
            private static final long serialVersionUID = 5341345879054512402L;
            final String[] cacheServiceHostArray = sentinels.split(",");

            {
                for (String hostAndPort : cacheServiceHostArray) {
                    String[] array = hostAndPort.split(":");
                    if (array.length > 1) {
                        add(new HostAndPort(array[0], Integer.parseInt(array[1])).toString());
                    } else {
                        add(new HostAndPort(array[0], 6379).toString());
                    }
                }
            }
        };
    }

    @Override
    public void close() {
        if (jedisSentinelPool != null) {
            jedisSentinelPool.close();
        }
    }
}
