package com.dk3k.framework.server.nettyServer;

import com.dk3k.framework.server.nettyServer.support.RequestParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by lilin on 2016/7/26.
 */
public class ServletNettyHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ServletNettyHandler.class);
    private static final String CHARSET = "UTF-8";

    private final Servlet servlet;
    private final ServletContext servletContext;

    private MockHttpServletRequest servletRequest;
    private MockHttpServletResponse servletResponse;

    public ServletNettyHandler(Servlet servlet) {
        this.servlet = servlet;
        this.servletContext = servlet.getServletConfig().getServletContext();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            logger.debug(request.getUri());

            if (!request.getDecoderResult().isSuccess()) {
                sendError(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }

            servletRequest = createServletRequest(request);
            servletResponse = new MockHttpServletResponse();
            servletResponse.setContentType("application/json;charset=UTF-8");

            this.servlet.service(servletRequest, servletResponse);

        }

        if (msg instanceof HttpContent) {
            //HttpContent content = (HttpContent) msg;
            //ByteBuf buf = content.content();

            HttpResponseStatus status = HttpResponseStatus.valueOf(servletResponse.getStatus());
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);

            for (String name : servletResponse.getHeaderNames()) {
                for (Object value : servletResponse.getHeaderValues(name)) {
                    response.headers().add(name, value);
                }
            }

            // Write the initial line and the header.
            ctx.write(response);

            InputStream contentStream = new ByteArrayInputStream(servletResponse.getContentAsByteArray());

            // Write the content.
            ChannelFuture writeFuture = ctx.write(new ChunkedStream(contentStream));
            writeFuture.addListener(ChannelFutureListener.CLOSE);

            /*HttpResponseStatus status = HttpResponseStatus.valueOf(servletResponse.getStatus());
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,Unpooled.wrappedBuffer(servletResponse.getContentAsByteArray()));

            for (String name : servletResponse.getHeaderNames()) {
                for (Object value : servletResponse.getHeaderValues(name)) {
                    response.headers().add(name, value);
                }
            }

            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);*/

        }


    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private MockHttpServletRequest createServletRequest(HttpRequest httpRequest) {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest(this.servletContext);
        servletRequest.setMethod(httpRequest.getMethod().name());
        servletRequest.setCharacterEncoding(CHARSET);

        try {
            String uri = httpRequest.getUri();
            String protocolName = httpRequest.getProtocolVersion().protocolName();
            for (Entry<String, String> entry : httpRequest.headers().entries()) {
                servletRequest.addHeader(entry.getKey(), entry.getValue());

                if ("host".equalsIgnoreCase(entry.getKey())) {
                    uri = protocolName + "://" + entry.getValue() + uri;
                }
            }

            UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(uri).build();
            servletRequest.setRequestURI(uriComponents.getPath());
            servletRequest.setPathInfo(uriComponents.getPath());
            if (uriComponents.getScheme() != null) {
                servletRequest.setScheme(uriComponents.getScheme());
            }
            if (uriComponents.getHost() != null) {
                servletRequest.setServerName(uriComponents.getHost());
                servletRequest.setRemoteAddr(uriComponents.getHost());
            }
            if (uriComponents.getPort() != -1) {
                servletRequest.setServerPort(uriComponents.getPort());
            }
            if (uriComponents.getQuery() != null) {
                String query = UriUtils.decode(uriComponents.getQuery(), CHARSET);
                servletRequest.setQueryString(query);
            }

        }catch(Exception e){
            logger.error("shouldn't happen", e);
        }

        if (httpRequest instanceof FullHttpRequest) {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) httpRequest;
            try {
                Map<String, String> params = RequestParser.parseParams(fullHttpRequest);
                for (Entry<String, String> entry : params.entrySet()) {
                    servletRequest.addParameter(UriUtils.decode(entry.getKey(), CHARSET), UriUtils.decode(entry.getValue(), CHARSET));
                }

            } catch (IOException e) {
                logger.error("shouldn't happen", e);
            }

            ByteBuf buf = fullHttpRequest.content();

            if (buf.hasArray()) {
                servletRequest.setContent(buf.array());
            } else {
                /*String content = buf.toString(io.netty.util.CharsetUtil.UTF_8);
                System.out.println(content);
                servletRequest.setContent(content.getBytes(io.netty.util.CharsetUtil.UTF_8));*/

                int len = buf.readableBytes();
                byte[] arr = new byte[len];
                buf.getBytes(0, arr);
                servletRequest.setContent(arr);
            }
            buf.release();

        }

        return servletRequest;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        ByteBuf content = Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8);
        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content, false);
        response.headers().add(HttpHeaders.Names.CONTENT_TYPE, "text/plain;charset=UTF-8");

        // Close the connection as soon as the error message is sent.
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
    }

}
