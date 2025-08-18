package io.ryan;

import io.ryan.common.Invocation;
import io.ryan.protocol.HttpClient;
import io.ryan.service.HelloService;

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
public class Main {
    public static void main(String[] args) {
        Invocation invocation = new Invocation(HelloService.class.getName(),"sayHello", new Class[]{String.class}, new Object[]{"Ryan"});
        HttpClient httpClient = new HttpClient();
        String result = httpClient.send("localhost", 8080, invocation);
        System.out.println(result);
    }
}