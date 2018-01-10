/*
 * Copyright (c) 2018. wuyufei
 */

package com.atguigu.business.socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventServer extends WebSocketServlet implements ClientSocketListener{

    private static final Logger LOG = LoggerFactory.getLogger(EventServer.class);

    final Queue<ClientSocket> connectedSockets = new ConcurrentLinkedQueue<>();
    private static Gson gson = new GsonBuilder().create();

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.setCreator(new WebSocketCreator(this));
    }

    public ClientSocket doWebSocketConnect(HttpServletRequest req, String protocol) {
        return new ClientSocket(req, protocol, this);
    }

    @Override
    public void onOpen(ClientSocket conn) {
        LOG.info("New connection from {} : {}", conn.getRequest().getRemoteAddr(),
                conn.getRequest().getRemotePort());
        connectedSockets.add(conn);
    }

    @Override
    public void onMessage(ClientSocket conn, String msg) {

        try {
            Message messagereceived = deserializeMessage(msg);
            LOG.debug("RECEIVE << " + messagereceived.op +
                    ", RECEIVE DATA << " + messagereceived.data);

            if (LOG.isTraceEnabled()) {
                LOG.trace("RECEIVE MSG = " + messagereceived);
            }

            switch (messagereceived.op) {

                case PING:
                    break; //do nothing

                default:
                    break;
            }
        } catch (Exception e) {
            LOG.error("Can't handle message: " + msg, e);
        }
    }

    @Override
    public void onClose(ClientSocket conn, int code, String reason) {
        LOG.info("Closed connection to {} : {}. ({}) {}", conn.getRequest().getRemoteAddr(),
                conn.getRequest().getRemotePort(), code, reason);
        connectedSockets.remove(conn);
    }

    protected Message deserializeMessage(String msg) {
        return gson.fromJson(msg, Message.class);
    }

    protected String serializeMessage(Message m) {
        return gson.toJson(m);
    }

    private void sendMsg(ClientSocket conn, Message message) throws IOException {
        conn.send(serializeMessage(message));
    }

}
