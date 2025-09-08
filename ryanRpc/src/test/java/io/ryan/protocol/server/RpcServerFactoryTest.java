package io.ryan.protocol.server;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * RpcServerFactory SPI机制测试
 */
public class RpcServerFactoryTest {

    @Test
    public void testGetSupportedProtocols() {
        Set<String> protocols = RpcServerFactory.getSupportedProtocols();
        assertNotNull(protocols);
        assertTrue(protocols.size() > 0);
        System.out.println("支持的协议: " + protocols);
    }

    @Test
    public void testIsProtocolSupported() {
        // 测试默认协议
        assertTrue(RpcServerFactory.isProtocolSupported("tcp"));
        assertTrue(RpcServerFactory.isProtocolSupported("http"));

        // 测试不存在的协议
        assertFalse(RpcServerFactory.isProtocolSupported("unknown"));
    }

    @Test
    public void testCreateNettyServer() {
        RpcServer server = RpcServerFactory.createServer("tcp", "localhost", 8080);
        assertNotNull(server);
        assertEquals("tcp", server.getProtocol().toLowerCase());
        assertTrue(server.getClass().getSimpleName().contains("Netty"));
    }

    @Test
    public void testCreateHttpServer() {
        RpcServer server = RpcServerFactory.createServer("http", "localhost", 8080);
        assertNotNull(server);
        assertEquals("http", server.getProtocol().toLowerCase());
        assertTrue(server.getClass().getSimpleName().contains("Http"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateServerWithUnknownProtocol() {
        RpcServerFactory.createServer("unknown", "localhost", 8080);
    }

    @Test
    public void testRegisterCustomServer() {
        // 创建自定义服务器类
        class TestServer extends AbstractRpcServer {
            public TestServer(String hostname, Integer port) {
                super(hostname, port);
            }

            @Override
            public void start() {
                // 测试实现
            }

            @Override
            public String getProtocol() {
                return "test";
            }
        }

        // 手动注册
        RpcServerFactory.registerServer("test", TestServer.class);

        // 验证注册成功
        assertTrue(RpcServerFactory.isProtocolSupported("test"));

        // 验证可以创建实例
        RpcServer server = RpcServerFactory.createServer("test", "localhost", 8080);
        assertNotNull(server);
        assertEquals("test", server.getProtocol());
        assertTrue(server instanceof TestServer);
    }
}
