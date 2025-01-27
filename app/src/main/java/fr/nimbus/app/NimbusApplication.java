package fr.nimbus.app;

import fr.nimbus.api.annotations.NimbusApp;
import fr.nimbus.core.handlers.HttpServerHandler;
import fr.nimbus.core.managers.MiddlewareManager;
import fr.nimbus.core.managers.ServiceManager;
import fr.nimbus.core.managers.RouteManager;
import fr.nimbus.core.utils.ClassScanner;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class NimbusApplication {
    private static final Logger logger = LogManager.getLogger(NimbusApplication.class);

    public static void run(Class<?> appClass, NimbusConfig config, String[] args) {
        if (appClass.getAnnotation(NimbusApp.class) == null) {
            throw new IllegalArgumentException("Main application class must be annotated with @NimbusApp.");
        }

        try {
            logger.info("Launching Nimbus Application...");

            // Scan the classes in the package
            String basePackage = appClass.getPackageName();
            List<Class<?>> classes = ClassScanner.scan(basePackage);

            // Initialize managers
            ServiceManager serviceManager = new ServiceManager();
            MiddlewareManager middlewareManager = new MiddlewareManager();
            RouteManager routeManager = new RouteManager(middlewareManager);

            serviceManager.registerServices(classes);
            routeManager.registerRoutes(classes); // Registers controllers and routes

            // Start HTTP server with Netty
            startServer(config.getPort(), routeManager, config.isCorsEnabled());

            logger.info("Nimbus Application started on port: {}", config.getPort());
            logger.info("CORS Enabled: {}", config.isCorsEnabled());
        } catch (Exception e) {
            logger.error("Fatal error during Nimbus initialization", e);
            throw new RuntimeException("Nimbus Application failed to start.", e);
        }
    }

    private static void startServer(int port, RouteManager routeManager, boolean corsEnabled) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();

                            if (corsEnabled) {
                                p.addLast(new CorsHandler(CorsConfigBuilder.forAnyOrigin()
                                        .allowedRequestMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                                        .allowedRequestHeaders("Content-Type", "Authorization", "X-Requested-With")
                                        .allowCredentials()
                                        .build()));
                            }

                            p.addLast(new HttpServerCodec());
                            p.addLast(new HttpObjectAggregator(1048576));

                            p.addLast(new HttpServerHandler(routeManager));
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            logger.info("Starting Netty server on port {}", port);

            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}