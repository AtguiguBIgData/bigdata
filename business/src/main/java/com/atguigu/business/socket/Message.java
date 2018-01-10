
/*
 * Copyright (c) 2018. wuyufei
 */

package com.atguigu.business.socket;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;


public class Message {
  /**
   * Representation of event type.
   */
  public static enum OP {
    PING,
  }

  private static final Gson gson = new Gson();
  public static final Message EMPTY = new Message(null);
  
  public OP op;
  public Map<String, Object> data = new HashMap<>();

  public Message(OP op) {
    this.op = op;
  }

  public Message put(String k, Object v) {
    data.put(k, v);
    return this;
  }

  public Object get(String k) {
    return data.get(k);
  }

  public <T> T getType(String key) {
    return (T) data.get(key);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Message{");
    sb.append("data=").append(data);
    sb.append(", op=").append(op);
    sb.append('}');
    return sb.toString();
  }

  public String toJson() {
    return gson.toJson(this);
  }

  public static Message fromJson(String json) {
    return gson.fromJson(json, Message.class);
  }
}
