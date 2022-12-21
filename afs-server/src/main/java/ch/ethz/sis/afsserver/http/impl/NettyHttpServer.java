package ch.ethz.sis.afsserver.http.impl;

import ch.ethz.sis.afsserver.http.HttpServerHandler;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.Logger;
import ch.ethz.sis.afsserver.http.HttpServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.Future;

public class NettyHttpServer implements HttpServer {

    private static final Logger logger = LogManager.getLogger(NettyHttpServer.class);

    private static final int MAX_QUEUE_LENGTH_FOR_INCOMING_CONNECTIONS = 128;

    private final EventLoopGroup masterGroup;

    private final EventLoopGroup slaveGroup;

    private ChannelFuture channelFuture;

    public NettyHttpServer() {
        masterGroup = new NioEventLoopGroup();
        slaveGroup = new NioEventLoopGroup();
    }

    public void start(int port, int maxContentLength, String uri, HttpServerHandler httpServerHandler) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(true)));

        try {
            final ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(masterGroup, slaveGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(final SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("codec", new HttpServerCodec());
                            ch.pipeline().addLast("aggregator", new HttpObjectAggregator(maxContentLength));
                            ch.pipeline().addLast("request", new NettyHttpHandler(uri, httpServerHandler));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, MAX_QUEUE_LENGTH_FOR_INCOMING_CONNECTIONS)
                    .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                    .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);
            channelFuture = bootstrap.bind(port).sync();
        } catch (final Exception ex) {
            logger.catching(ex);
        } finally {
            masterGroup.shutdownGracefully();
            slaveGroup.shutdownGracefully();
        }
    }

    public void shutdown(boolean gracefully) {
        try {
            channelFuture.channel().close();
        } catch (Exception ex) {
            logger.catching(ex);
        }

        try {
            if (gracefully) {
                Future slaveShutdown = slaveGroup.shutdownGracefully();
                slaveShutdown.await();
            } else {
                slaveGroup.shutdown();
            }
        } catch (Exception ex) {
            logger.catching(ex);
        }

        try {
            if (gracefully) {
                Future masterShutdown = masterGroup.shutdownGracefully();
                masterShutdown.await();
            } else {
                masterGroup.shutdown();
            }
        } catch (Exception ex) {
            logger.catching(ex);
        }
    }
}