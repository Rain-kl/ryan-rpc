# 手动实现 RPC 框架

- 创建 tomcat 服务器, 以网络的方式实现远程调用
- 定义远程调用数据对象 Invocation 用于传输数据
- tomcat 收到请求后, 反序列化 Invocation 对象交给 dispatcher 进行处理
- dispatcher 通过反射调用对应的服务方法
- 定义 Httpclient 客户端, 发送 Invocation 对象到 tomcat 服务器