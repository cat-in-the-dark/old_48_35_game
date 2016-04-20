package com.catinthedark.shapeshift.network

import com.catinthedark.shapeshift.common.Const
import org.zeromq.ZMQ.Socket
import org.zeromq.{ZContext, ZMQ}

class NetworkClientControl(serverAddress: String) extends NetworkControl {
  override def onHello(pushSocket: Socket) = println(s"Connected to $serverAddress")
  
  override def run(): Unit = {
    val ctx = new ZContext()
    val pullSocket = ctx.createSocket(ZMQ.PULL)
    val pushSocket = ctx.createSocket(ZMQ.PUSH)
    pullSocket.connect(s"tcp://$serverAddress:${Const.serverPushPort}")
    pushSocket.connect(s"tcp://$serverAddress:${Const.serverPullPort}")
    pushSocket.send(s"$HELLO_PREFIX:")

    println(s"Start connecting to $serverAddress")
    work(pushSocket, pullSocket)
  }
}
