package io.ryan.register;

import io.ryan.common.dto.URI;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;

import java.util.ArrayList;
import java.util.List;


public class RedisRegister {

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

    public static void register(String interfaceName, URI URI) {
        String key = "ryanRpc:" + interfaceName;
        java.net.URI uri = java.net.URI.create(URI.getProtocol() + "://" + URI.getHostname() + ":" + URI.getPort());
        jedis.sadd(key, uri.toString());
    }

    public static List<URI> get(String interfaceName) {
        String key = "ryanRpc:" + interfaceName;
        List<URI> URIS = new ArrayList<>();
        for (String value : jedis.smembers(key)) {
            java.net.URI uri = java.net.URI.create(value);
            String protocol = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();
            URIS.add(new URI(host, port, protocol));
        }
        return URIS;

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
