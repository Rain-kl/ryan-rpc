package io.ryan.protocol.server.HttpServerImpl;

import io.ryan.common.constant.RpcProtocol;
import io.ryan.protocol.server.AbstractRpcServer;
import org.apache.catalina.*;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;


public class HttpServer extends AbstractRpcServer {

    public static final String PROTOCOL = RpcProtocol.HTTP;

    public HttpServer(String host, Integer port) {
        super(host, port);
    }

    public void start() {
        Tomcat tomcat = new Tomcat();

        Server server = tomcat.getServer();
        Service service = server.findService("Tomcat");

        Connector connector = new Connector();
        connector.setPort(this.getPort());

        Engine engine = new StandardEngine();
        engine.setDefaultHost(this.getHostname());

        Host host = new StandardHost();
        host.setName(this.getHostname());

        String ContextPath = "";
        Context context = new StandardContext();
        context.setPath(ContextPath);
        context.addLifecycleListener(new Tomcat.FixContextListener());

        host.addChild(context);
        engine.addChild(host);

        service.setContainer(engine);
        service.addConnector(connector);

        tomcat.addServlet(ContextPath, "dispatcher", new DispatcherServlet());
        context.addServletMappingDecoded("/*", "dispatcher");

        try {
            tomcat.start();
            tomcat.getServer().await();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getProtocol() {
        return RpcProtocol.HTTP;
    }
}
