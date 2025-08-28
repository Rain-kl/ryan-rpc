package io.ryan.loadbalance.impl;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ConsistencyHashBalanceTest {

    private ConsistencyHashBalance balance;

    @Before
    public void setUp() {
        ConsistencyHashBalance.BASE_VIRTUAL_NUM=10;
        balance = new ConsistencyHashBalance();
    }


    @Test
    public void testGetServer() {
        // 模拟真实节点
        List<String> nodes = Arrays.asList("server1", "server2", "server3");

        // 使用 UUID 作为请求的唯一标识符进行负载均衡
        String server = balance.select(nodes,"request-1" );
        assertNotNull("Server should not be null", server);
        assertTrue("Server should be one of the real nodes", nodes.contains(server));

        // 确保多个请求的分配在不同节点上（可根据测试的多次运行结果观察）
        String server2 = balance.select(nodes,"request-3" );

        assertNotEquals("Server should be different from the previous request", server, server2);
    }
}