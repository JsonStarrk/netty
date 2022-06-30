package com.test.server;

import static com.test.Constants.PORT;

import com.test.server.handler.ServerDispatchHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServer {

  public static void main(String[] args) {
    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap sbs = new ServerBootstrap()
          .group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .handler(new LoggingHandler(LogLevel.INFO))
          .localAddress(new InetSocketAddress(PORT))
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
              ChannelPipeline pipeline = ch.pipeline();
              pipeline
//                  .addLast(
//                      //拆粘包处理器
//                      new LengthFieldBasedFrameDecoder(
//                          ByteOrder.LITTLE_ENDIAN,
//                          1024 * 128,
//                          0,
//                          2,
//                          -2,
//                          0,
//                          true))
                  //如果1秒钟没有数据写出，则触发写空闲
                  .addLast(new IdleStateHandler(0, 1, 0, TimeUnit.SECONDS))
                  //解码器
                  .addLast("decoder", new StringDecoder())
                  //编码器
                  .addLast("encoder", new StringEncoder())
                  //分配器
                  .addLast("dispatch", new ServerDispatchHandler());
            }
          })
          // Specify the queue can accept the maximum number of links for 128
          .childOption(ChannelOption.SO_BACKLOG, 128)
          .childOption(ChannelOption.TCP_NODELAY, true)
          .childOption(ChannelOption.SO_KEEPALIVE, true);
      // Binding port, to receive incoming connections
      ChannelFuture future = sbs.bind(PORT).sync();
      future.channel().closeFuture().sync();
    } catch (Exception e) {
      log.error("服务器启动错误", e);
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }

}
