
/*
 * Copyright (c) 2018. wuyufei
 */

package com.atguigu.business.socket;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class ClientSocket extends WebSocketAdapter {

  private Session connection;
  private ClientSocketListener listener;
  private HttpServletRequest request;
  private String protocol;
  private String user;

  public ClientSocket(HttpServletRequest req, String protocol,
                      ClientSocketListener listener) {
    this.listener = listener;
    this.request = req;
    this.protocol = protocol;
    this.user = StringUtils.EMPTY;
  }

  @Override
  public void onWebSocketClose(int closeCode, String message) {
    listener.onClose(this, closeCode, message);
  }

  @Override
  public void onWebSocketConnect(Session connection) {
    this.connection = connection;
    listener.onOpen(this);
  }

  @Override
  public void onWebSocketText(String message) {
    listener.onMessage(this, message);
  }


  public HttpServletRequest getRequest() {
    return request;
  }

  public String getProtocol() {
    return protocol;
  }

  public synchronized void send(String serializeMessage) throws IOException {
    connection.getRemote().sendString(serializeMessage);
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }
}
