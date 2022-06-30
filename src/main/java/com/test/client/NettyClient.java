package com.test.client;

import static com.test.Constants.PORT;

import com.test.client.handler.ClientDispatchHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyClient {

  public static void main(String[] args) {

    EventLoopGroup group = new NioEventLoopGroup();
    ChannelFuture future = null;
    try {
      Bootstrap b = new Bootstrap();
      b.group(group)
          .channel(NioSocketChannel.class)
          .option(ChannelOption.TCP_NODELAY, true)
          .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 500)
          .handler(new LoggingHandler(LogLevel.INFO))
          .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
              ChannelPipeline p = ch.pipeline();
              p
//                  .addLast(
//                      new LengthFieldBasedFrameDecoder(
//                          ByteOrder.LITTLE_ENDIAN,
//                          1024 * 128,
//                          0,
//                          2,
//                          -2,
//                          0,
//                          true))
                  //如果3秒钟没有数据写入，则触发读空闲,触发读空闲就认为断线，就主动断开连接然后触发重连。
                  .addLast(new IdleStateHandler(3, 0, 0, TimeUnit.SECONDS))
                  .addLast("decoder", new StringDecoder())
                  .addLast("encoder", new StringEncoder())
                  .addLast("dispatch", new ClientDispatchHandler());
            }
          });
      future = b.connect("127.0.0.1", PORT).sync();
      future.channel().closeFuture().sync();
    } catch (Exception e) {
      throw new RuntimeException("客户端连接异常...", e);
    } finally {
      //当服务端断线或登陆时无法连接服务器，都会走到这里，去优雅的释放资源。
      //当释放了资源后，如果是主机，则再次连接。如果是备机，则直接退出。
      group.shutdownGracefully();
    }
  }

}
