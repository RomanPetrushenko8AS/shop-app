package com.example;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
    private static final Logger logger =  LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        logger.info("Запуск приложения");

        ResourceConfig config = new ResourceConfig()
                .packages("com.example.rest")
                .register(OpenApiResource.class);

        ServletContextHandler ctx =
                new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        ctx.setContextPath("/");
        ctx.addServlet(new ServletHolder(new ServletContainer(config)), "/*");

        Server server = new Server(8080);
        server.setHandler(ctx);
        server.start();

        logger.info("Server started: http://localhost:8080/swagger");
        logger.info("OpenApi started: http://localhost:8080/openapi.json");
        server.join();
    }
}