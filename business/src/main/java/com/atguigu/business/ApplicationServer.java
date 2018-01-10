/*
 * Copyright (c) 2018. wuyufei
 */

package com.atguigu.business;

import com.atguigu.commons.ConfigManager;
import com.atguigu.business.scheduler.SchedulerService;
import com.atguigu.business.socket.EventServer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;

public class ApplicationServer {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationServer.class);
    public static Server jettyWebServer;
    public static EventServer eventServer;

    public static void main(String[] args) throws InterruptedException {

        // Embbed Jetty Server
        jettyWebServer = setupJettyServer();

        // Image Server
        ResourceHandler imagesHandler = setupImageServer();

        // Servlet Server
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        final WebAppContext webApp = setupWebAppContext(contexts);

        // WebSocket server
        setupEventServer(webApp);

        // Set Handlers
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] {imagesHandler, contexts, new DefaultHandler() });

        jettyWebServer.setHandler(handlers);
        LOG.info("Starting scheduler server");
        try {
            jettyWebServer.start();
        } catch (Exception e) {
            LOG.error("Error while running jettyServer", e);
            System.exit(-1);
        }
        LOG.info("Done, Scheduler server started");

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override public void run() {
                LOG.info("Shutting down Scheduler Server ... ");
                try {
                    jettyWebServer.stop();
                    SchedulerService.shutdown();
                    Thread.sleep(3000);
                } catch (Exception e) {
                    LOG.error("Error while stopping servlet container", e);
                }
                LOG.info("Bye");
            }
        });
        jettyWebServer.join();
    }

    private static Server setupJettyServer() {

        final Server server = new Server();
        ServerConnector connector = new ServerConnector(server);

        // Set some timeout options to make debugging easier.
        int timeout = 1000 * 30;
        connector.setIdleTimeout(timeout);
        connector.setSoLingerTime(-1);
        connector.setHost(ConfigManager.getServerAddress());
        connector.setPort(ConfigManager.getServerPort());

        server.addConnector(connector);

        return server;
    }

    private static ResourceHandler setupImageServer(){

        ResourceHandler imageHandler = new ResourceHandler();

        imageHandler.setDirectoriesListed(true);
        imageHandler.setResourceBase("/Users/wuyufei/GitHub/bigdata/distribute/assert/");
        imageHandler.setStylesheet("");

        return imageHandler;
    }

    private static WebAppContext setupWebAppContext(ContextHandlerCollection contexts) {

        WebAppContext webApp = new WebAppContext();
        webApp.setContextPath(ConfigManager.getServerContextPath());

        File warPath = new File("/Users/wuyufei/GitHub/bigdata/business/src/main/webapp/dist");
        if (warPath.isDirectory()) {
            // Development mode, read from FS
            //webApp.setDescriptor("WEB-INF/web.xml");
            webApp.setResourceBase(warPath.getPath());
            webApp.setParentLoaderPriority(true);
        } else {
            // use packaged WAR
            /*webApp.setWar(warPath.getAbsolutePath());
            File warTempDirectory = new File(conf.getRelativeDir(ConfVars.ZEPPELIN_WAR_TEMPDIR));
            warTempDirectory.mkdir();
            LOG.info("ZeppelinServer Webapp path: {}", warTempDirectory.getPath());
            webApp.setTempDirectory(warTempDirectory);*/
        }

        contexts.addHandler(webApp);

        webApp.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed","true");

        return webApp;

    }

    private static void setupEventServer(WebAppContext webapp) {
        eventServer = new EventServer();
        final ServletHolder servletHolder = new ServletHolder(eventServer);
        servletHolder.setInitParameter("maxTextMessageSize", "1024000");

        final ServletContextHandler context = new ServletContextHandler(
                ServletContextHandler.SESSIONS);

        webapp.addServlet(servletHolder, "/ws/*");
    }

}
