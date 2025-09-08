package io.ryan.protocol.server;

import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RpcServer SPI 工厂类
 * 使用Java SPI机制自动发现和加载RpcServer实现
 */
@Slf4j
public class RpcServerFactory {

    private static final Map<String, Class<? extends RpcServer>> SERVER_CACHE = new ConcurrentHashMap<>();
    private static volatile boolean initialized = false;

    /**
     * 初始化并加载所有可用的RpcServer实现
     */
    private static void init() {
        if (initialized) {
            return;
        }

        synchronized (RpcServerFactory.class) {
            if (initialized) {
                return;
            }

            ServiceLoader<RpcServer> serviceLoader = ServiceLoader.load(RpcServer.class);
            for (RpcServer provider : serviceLoader) {
                String protocol = provider.getProtocol();

                // 根据provider确定实际的实现类
                Class<? extends RpcServer> actualServerClass = getActualServerClass(provider);
                if (actualServerClass != null) {
                    SERVER_CACHE.put(protocol.toLowerCase(), actualServerClass);
                    log.info("Loaded RpcServer implementation: {} for protocol: {}",
                            actualServerClass.getSimpleName(), protocol);
                }
            }

            // 如果通过ServiceLoader没有找到实现，加载默认实现
            if (SERVER_CACHE.isEmpty()) {
                loadDefaultImplementations();
            }

            initialized = true;
        }
    }

    /**
     * 根据提供者获取实际的服务器实现类
     */
    private static Class<? extends RpcServer> getActualServerClass(RpcServer provider) {
        String className = provider.getClass().getName();

        if (className.endsWith("Provider")) {
            try {
                className = className.replace("Provider", "");
                @SuppressWarnings("unchecked")
                Class<? extends RpcServer> httpClass = (Class<? extends RpcServer>) Class
                        .forName(className);
                return httpClass;
            } catch (ClassNotFoundException e) {
                log.warn("HttpServer implementation not found", e);
            }

        } else {
            // 对于用户自定义的实现，直接返回其类
            return provider.getClass();
        }

        return null;
    }

    /**
     * 加载默认的RpcServer实现
     */
    private static void loadDefaultImplementations() {
        try {
            // 加载Netty实现
            Class<?> nettyClass = Class.forName("io.ryan.protocol.server.NettServerImpl.NettyServer");
            if (RpcServer.class.isAssignableFrom(nettyClass)) {
                @SuppressWarnings("unchecked")
                Class<? extends RpcServer> nettyServerClass = (Class<? extends RpcServer>) nettyClass;
                // 创建一个临时实例来获取协议信息
                RpcServer tempServer = nettyServerClass.getDeclaredConstructor(String.class, Integer.class)
                        .newInstance("localhost", 8080);
                SERVER_CACHE.put(tempServer.getProtocol().toLowerCase(), nettyServerClass);
                log.info("Loaded default Netty RpcServer implementation for protocol: {}", tempServer.getProtocol());
            }
        } catch (Exception e) {
            log.warn("Failed to load Netty RpcServer implementation", e);
        }

        try {
            // 加载HTTP实现
            Class<?> httpClass = Class.forName("io.ryan.protocol.server.HttpServerImpl.HttpServer");
            if (RpcServer.class.isAssignableFrom(httpClass)) {
                @SuppressWarnings("unchecked")
                Class<? extends RpcServer> httpServerClass = (Class<? extends RpcServer>) httpClass;
                // 创建一个临时实例来获取协议信息
                RpcServer tempServer = httpServerClass.getDeclaredConstructor(String.class, Integer.class)
                        .newInstance("localhost", 8080);
                SERVER_CACHE.put(tempServer.getProtocol().toLowerCase(), httpServerClass);
                log.info("Loaded default HTTP RpcServer implementation for protocol: {}", tempServer.getProtocol());
            }
        } catch (Exception e) {
            log.warn("Failed to load HTTP RpcServer implementation", e);
        }
    }

    /**
     * 根据协议创建RpcServer实例
     *
     * @param protocol 协议类型（如 "tcp", "http"）
     * @param hostname 主机名
     * @param port     端口号
     * @return RpcServer实例
     * @throws IllegalArgumentException 如果找不到对应协议的实现
     */
    public static RpcServer createServer(String protocol, String hostname, Integer port) {
        init();

        Class<? extends RpcServer> serverClass = SERVER_CACHE.get(protocol.toLowerCase());
        if (serverClass == null) {
            throw new IllegalArgumentException("No RpcServer implementation found for protocol: " + protocol +
                    ". Available protocols: " + getSupportedProtocols());
        }

        try {
            // 尝试使用带参数的构造函数
            return serverClass.getDeclaredConstructor(String.class, Integer.class)
                    .newInstance(hostname, port);
        } catch (Exception e) {
            log.error("Failed to create RpcServer instance for protocol: {}", protocol, e);
            throw new RuntimeException("Failed to create RpcServer instance", e);
        }
    }

    /**
     * 获取所有支持的协议
     *
     * @return 支持的协议列表
     */
    public static Set<String> getSupportedProtocols() {
        init();
        return new HashSet<>(SERVER_CACHE.keySet());
    }

    /**
     * 检查是否支持指定协议
     *
     * @param protocol 协议类型
     * @return 是否支持
     */
    public static boolean isProtocolSupported(String protocol) {
        init();
        return SERVER_CACHE.containsKey(protocol.toLowerCase());
    }

    /**
     * 手动注册RpcServer实现（用于测试或动态注册）
     *
     * @param protocol    协议类型
     * @param serverClass RpcServer实现类
     */
    public static void registerServer(String protocol, Class<? extends RpcServer> serverClass) {
        SERVER_CACHE.put(protocol.toLowerCase(), serverClass);
        log.info("Manually registered RpcServer implementation: {} for protocol: {}",
                serverClass.getSimpleName(), protocol);
    }
}
