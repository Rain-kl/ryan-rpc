package io.ryan;


import io.ryan.protocol.HttpServer;
import io.ryan.register.LocalRegister;
import io.ryan.service.HelloService;
import io.ryan.serviceImpl.HelloServiceImpl;

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
public class Main {
    public static void main(String[] args) {
        LocalRegister.register(HelloService.class.getName(), HelloServiceImpl.class);

        HttpServer server = new HttpServer();
        server.start("localhost", 8080);

    }
}