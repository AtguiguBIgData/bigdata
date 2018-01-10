
/*
 * Copyright (c) 2018. wuyufei
 */

package com.atguigu.business.socket;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible to create the WebSockets for the NotebookServer.
 */
public class WebSocketCreator implements org.eclipse.jetty.websocket.servlet.WebSocketCreator {

  private static final Logger LOG = LoggerFactory.getLogger(WebSocketCreator.class);
  private EventServer eventServer;

  public WebSocketCreator(EventServer eventServer) {
    this.eventServer = eventServer;
  }
  public Object createWebSocket(ServletUpgradeRequest request, ServletUpgradeResponse response) {
    return new ClientSocket(request.getHttpServletRequest(), "", eventServer);
  }

}
