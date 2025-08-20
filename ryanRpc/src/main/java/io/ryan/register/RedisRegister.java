package io.ryan.register;

import io.ryan.common.URL;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;

import java.util.ArrayList;
import java.util.List;


public class RedisRegister{

    private static final Jedis jedis;

    static {
        JedisClientConfig jedisClientConfig = DefaultJedisClientConfig.builder()
                .password("root")
                .build();
        jedis = new Jedis("localhost", 6379, jedisClientConfig);

        // 注册shutdown hook，在程序停止时自动关闭jedis连接
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (jedis.isConnected()) {
                jedis.close();
                System.out.println("Redis connection closed successfully.");
            }
        }));
    }

    public static void register(String interfaceName, URL url) {
        String key = "ryanRpc:" + interfaceName;
        jedis.sadd(key, url.getHostname() + ":" + url.getPort());
    }

    public static List<URL> get(String interfaceName) {
        String key = "ryanRpc:" + interfaceName;
        List<URL> urls = new ArrayList<>();
        for (String value : jedis.smembers(key)) {
            String[] parts = value.split(":");
            if (parts.length == 2) {
                String hostname = parts[0];
                int port = Integer.parseInt(parts[1]);
                urls.add(new URL(hostname, port));
            }
        }
        return urls;

    }

    /**
     * 手动关闭Redis连接
     */
    public static void close() {
        if (jedis != null && jedis.isConnected()) {
            jedis.close();
        }
    }

}
