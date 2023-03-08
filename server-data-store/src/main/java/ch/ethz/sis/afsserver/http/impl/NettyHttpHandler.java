/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ethz.sis.afsserver.http.impl;

import ch.ethz.sis.afsserver.http.HttpResponse;
import ch.ethz.sis.afsserver.http.HttpServerHandler;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

import static io.netty.handler.codec.http.HttpMethod.*;

public class NettyHttpHandler extends ChannelInboundHandlerAdapter
{

    private static final Logger logger = LogManager.getLogger(NettyHttpServer.class);

    private static final byte[] NOT_FOUND = "404 NOT FOUND".getBytes();

    private static final ByteBuf NOT_FOUND_BUFFER = Unpooled.wrappedBuffer(NOT_FOUND);

    private static final Set<HttpMethod> allowedMethods = Set.of(GET, POST, PUT, DELETE);

    private final String uri;

    private final HttpServerHandler httpServerHandler;

    public NettyHttpHandler(String uri, HttpServerHandler httpServerHandler)
    {
        this.uri = uri;
        this.httpServerHandler = httpServerHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        if (msg instanceof FullHttpRequest)
        {
            final FullHttpRequest request = (FullHttpRequest) msg;
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri(), true);
            if (queryStringDecoder.path().equals(uri) &&
                    allowedMethods.contains(request.method()))
            {
                FullHttpResponse response = null;
                ByteBuf content = request.content();
                try
                {
                    byte[] array = new byte[content.readableBytes()];
                    content.readBytes(array);
                    HttpResponse apiResponse = httpServerHandler.process(request.method(),
                            queryStringDecoder.parameters(), array);
                    HttpResponseStatus status = (!apiResponse.isError()) ?
                            HttpResponseStatus.OK :
                            HttpResponseStatus.BAD_REQUEST;
                    response = getHttpResponse(
                            status,
                            apiResponse.getContentType(),
                            Unpooled.wrappedBuffer(apiResponse.getBody()),
                            apiResponse.getBody().length);
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                } finally
                {
                    content.release();
                }
            } else
            {
                FullHttpResponse response = getHttpResponse(
                        HttpResponseStatus.NOT_FOUND,
                        "text/plain",
                        NOT_FOUND_BUFFER,
                        NOT_FOUND.length);
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        } else
        {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception
    {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        logger.catching(cause);
        byte[] causeBytes = cause.getMessage().getBytes();
        FullHttpResponse response = getHttpResponse(
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                "text/plain",
                Unpooled.wrappedBuffer(causeBytes),
                causeBytes.length
        );
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    public FullHttpResponse getHttpResponse(
            HttpResponseStatus status,
            String contentType,
            ByteBuf content,
            int contentLength)
    {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                content
        );
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, contentLength);
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CONNECTION, "close");
        return response;
    }
}
