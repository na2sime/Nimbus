package fr.nimbus.core.handlers;

import fr.nimbus.core.managers.RouteManager;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A handler for processing incoming HTTP requests in a Netty server.
 * Extends {@link SimpleChannelInboundHandler} to manage {@link FullHttpRequest} messages.
 *
 * This class utilizes a {@link RouteManager} to delegate request routing to appropriate controller methods
 * based on HTTP method and URI path.
 *
 * Responsibilities include:
 * - Logging incoming HTTP requests.
 * - Delegating request handling to the {@link RouteManager}.
 * - Generating appropriate HTTP responses based on the request's routing outcome or errors.
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LogManager.getLogger(HttpServerHandler.class);

    private final RouteManager routeManager;

    public HttpServerHandler(RouteManager routeManager) {
        this.routeManager = routeManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        String httpMethod = request.method().name();
        String requestPath = request.uri();

        logger.info("Received request: {} {}", httpMethod, requestPath);

        FullHttpResponse response;

        try {
            // Delegate request handling to RouteManager
            Object result = routeManager.handleRequest(httpMethod, requestPath);

            if (result != null) {
                response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        ctx.alloc().buffer().writeBytes(result.toString().getBytes())
                );
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            } else {
                response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.NOT_FOUND
                );
            }
        } catch (Exception e) {
            logger.error("Error processing the request", e);
            response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    ctx.alloc().buffer().writeBytes(("Internal Server Error: " + e.getMessage()).getBytes())
            );
        }

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
