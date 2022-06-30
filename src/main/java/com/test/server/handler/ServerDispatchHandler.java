package com.test.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName: DispatchHandler
 * @Description: 业务处理类
 * @date: 2020/2/27 11:23
 * @Version: 1.0
 */
@Slf4j
public class ServerDispatchHandler extends SimpleChannelInboundHandler<String> {


  /**
   * @Discription: 心跳触发器，如果写空闲，就补偿一个心跳。
   */
  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent) {
      IdleStateEvent event = (IdleStateEvent) evt;
      if (event.state() == IdleState.WRITER_IDLE) {
        //Send a heartbeat message to Server every 1 seconds;
        if (log.isDebugEnabled()) {
          log.debug("服务端触发写空闲事件，补发一个写事件...");
        }
        ctx.channel().writeAndFlush("心跳");
      }
    } else {
      super.userEventTriggered(ctx, evt);
    }
  }

  /**
   * @Discription: ip连接。
   */
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    log.info("服务端触发连接事件，由{}连接", ctx.channel().remoteAddress().toString());
    super.channelActive(ctx);
  }


  /**
   * @Discription: ip注销, sendAddress注销
   */
  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    log.info("服务端触发断连事件，来源为[{}]", ctx.channel().remoteAddress().toString());
    super.channelInactive(ctx);
  }

  /**
   * @Discription: 处理登陆
   */
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, String msg) {
    //业务处理
    System.out.println("处理业务：" + msg);
    ctx.channel().writeAndFlush("pong");
  }

}
