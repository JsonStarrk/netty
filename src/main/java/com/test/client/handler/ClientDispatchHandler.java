package com.test.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ClientDispatchHandler extends SimpleChannelInboundHandler<String> {


  /**
   * @Discription: 业务处理
   */
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
    //业务处理
    System.out.println("处理业务:" + msg);

  }

  /**
   * @Discription: 注册channel
   */
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    log.info("登陆:来自{}", ctx.channel().remoteAddress().toString());
    //登陆后向服务器发送一条ping。
    ctx.channel().writeAndFlush("ping");
    super.channelActive(ctx);
  }

  /**
   * @Discription: 心跳机制，触发读空闲时，主动关闭链接。
   */
  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent) {
      IdleStateEvent event = (IdleStateEvent) evt;
      if (event.state() == IdleState.READER_IDLE) {
        //When Server has no data message, it will be send a heartbeat message every 1 seconds,
        //so i set Client read_idle time 3s,
        //Once trigger Client read_idle, means lost connect with Server ,
        //So let the channel closed, and then it will trigger reconnect 3 times;
        log.error("三秒没有收到服务端写回的消息，触发读空闲事件，客户端主动断开当前连接");
//        ctx.channel().close();
      }
    } else {
      super.userEventTriggered(ctx, evt);
    }
  }

  /**
   * @Discription: 注销channel
   */
  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    super.channelInactive(ctx);
  }
}
