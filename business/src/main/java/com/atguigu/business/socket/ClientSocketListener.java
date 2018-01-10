
/*
 * Copyright (c) 2018. wuyufei
 */

package com.atguigu.business.socket;


public interface ClientSocketListener {
  void onClose(ClientSocket socket, int code, String message);
  void onOpen(ClientSocket socket);
  void onMessage(ClientSocket socket, String message);
}
