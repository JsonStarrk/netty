# netty
netty的简单实现。

登陆时会向服务器发送 ping
服务器收到会返回 pong

然后 服务器有心跳功能 如果超过3秒没收到客户端消息，就会给客户端发送 心跳 消息。

基于此 可以做 超时重试功能。
拆粘包功能在LengthFieldBasedFrameDecoder处理
