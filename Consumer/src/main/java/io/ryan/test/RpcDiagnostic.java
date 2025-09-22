package io.ryan.test;

import io.ryan.loadbalance.impl.RoundLoadBalance;
import io.ryan.proxy.ProxyFactory;
import io.ryan.service.HelloService;
import io.ryan.serviceCenter.impl.nacosImpl.NacosCenter;

/**
 * RPC服务连接诊断工具
 */
public class RpcDiagnostic {
    
    public static void main(String[] args) {
        System.out.println("======== RPC服务连接诊断 ========");
        
        try {
            // 1. 初始化连接
            System.out.println("1. 正在初始化服务中心连接...");
            NacosCenter serviceCenter = new NacosCenter("localhost", 8848, new RoundLoadBalance<>());
            System.out.println("✓ 服务中心初始化成功");
            
            // 2. 设置代理工厂
            System.out.println("2. 正在设置代理工厂...");
            ProxyFactory.setServiceCenter(serviceCenter);
            System.out.println("✓ 代理工厂设置成功");
            
            // 3. 获取服务代理
            System.out.println("3. 正在获取HelloService代理...");
            HelloService helloService = ProxyFactory.getProxy(HelloService.class);
            System.out.println("✓ 服务代理获取成功");
            
            // 4. 进行几次测试调用
            System.out.println("4. 进行测试调用...");
            for (int i = 0; i < 5; i++) {
                try {
                    long startTime = System.currentTimeMillis();
                    String result = helloService.sayHello("DiagnosticTest-" + i);
                    long responseTime = System.currentTimeMillis() - startTime;
                    
                    if (result != null && !result.isEmpty()) {
                        System.out.println("✓ 调用 " + i + " 成功 (" + responseTime + "ms): " + 
                                         (result.length() > 50 ? result.substring(0, 50) + "..." : result));
                    } else {
                        System.out.println("✗ 调用 " + i + " 返回空结果");
                    }
                } catch (Exception e) {
                    System.out.println("✗ 调用 " + i + " 失败: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    if (i == 0) { // 打印第一个错误的详细堆栈
                        System.out.println("详细错误信息:");
                        e.printStackTrace();
                    }
                }
                
                // 短暂等待
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            System.out.println("\n======== 诊断完成 ========");
            
        } catch (Exception e) {
            System.err.println("✗ 诊断过程中发生错误: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            System.err.println("详细错误信息:");
            e.printStackTrace();
        }
    }
}