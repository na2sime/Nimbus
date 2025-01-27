package fr.nimbus.core.handlers;

import fr.nimbus.api.middleware.MiddlewareResult;
import fr.nimbus.api.middleware.RequestContext;
import fr.nimbus.core.managers.MiddlewareManager;
import fr.nimbus.core.managers.RouteManager;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final RouteManager routeManager;
    private final MiddlewareManager middlewareManager;

    public HttpServerHandler(RouteManager routeManager) {
        this.routeManager = routeManager;
        this.middlewareManager = new MiddlewareManager();

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        RequestContext requestContext = new RequestContext(request);
        MiddlewareResult middlewareResult;

        // Middleware execution BEFORE the controller
        middlewareResult = middlewareManager.executeBefore(requestContext);
        if (!middlewareResult.shouldProceed()) {
            // Send an immediate response if any middleware blocks the request
            sendResponse(ctx, middlewareResult.getResponse());
            return;
        }

        // Controller logic
        Object response;
        try {
            response = routeManager.handleRequest(request.method().name(), request.uri());
            if (response == null) {
                response = "404 - Not Found";
            }
        } catch (Exception e) {
            response = "500 - Internal Server Error: " + e.getMessage();
        }

        // Middleware execution AFTER the controller
        response = middlewareManager.executeAfter(response, requestContext);

        // Send the response
        sendResponse(ctx, response);
    }

    private void sendResponse(ChannelHandlerContext ctx, Object response) {
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                ctx.alloc().buffer().writeBytes(response.toString().getBytes())
        );
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
    }
}