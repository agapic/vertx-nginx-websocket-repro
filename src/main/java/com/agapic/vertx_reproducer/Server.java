package com.agapic.vertx_reproducer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server extends AbstractVerticle {
    private final Logger logger = LoggerFactory.getLogger(Server.class);
    
    public void start() {
        // To fix the issue:
        // 1. Pass options into createHttpServer()
        // 2. After calling ws.reject() make sure you call ws.close()
        //
        // HttpServerOptions options = new HttpServerOptions()
        // .setWebSocketClosingTimeout(0);

        vertx.createHttpServer().webSocketHandler(ws -> {
            MultiMap headers = ws.headers();
            Optional<String> userAgent = Optional.ofNullable(headers.get("user-agent"));
            Optional<String> requestId = Optional.ofNullable(headers.get("x-request-id"));
            Optional<String> cookies = Optional.ofNullable(headers.get("cookie"));
            Optional<String> forcedResponse = Optional.ofNullable(headers.get("x-rto-test-response"));

            StringBuilder allHeaders = new StringBuilder();
            headers.forEach(x -> allHeaders.append(x.getKey() + ":" + x.getValue() + " "));

            int responseCode = forcedResponse.map(r -> {
                try {
                    return Integer.parseInt(r);
                } catch (Exception e) {
                    logger.info("failure parsing x-rto-test-response", e);
                    return 400;
                }
            }).orElse(200);

            logger.info("Client connected: {} x-request-id:{} user-agent:{} cookies:{} headers:{}",
                    ws.remoteAddress(),
                    requestId.orElse(""),
                    userAgent.orElse(""),
                    cookies.orElse(""),
                    allHeaders.toString().trim());

            if (responseCode == 200) {
                ws.accept();
                context.put("user-agent", userAgent.orElse(""));
                context.put("x-request-id", requestId.orElse(""));
                context.put("cookie", cookies.orElse(""));

                ws.binaryMessageHandler(message -> {
                    logger.info("Binary Message: x-request-id:{} binaryHandlerId:{} length:{}",
                            ws.binaryHandlerID(),
                            context.get("x-request-id"),
                            message.length());
                });

                ws.textMessageHandler(message -> {
                    logger.info("Text Message: x-request-id:{}, textHandlerId:{} message:{}",
                            context.get("x-request-id"),
                            ws.textHandlerID(),
                            message);
                });

                ws.closeHandler(message -> {
                    logger.info(
                            "Client disconnected: x-request-id:{} address:{} binaryHandlerId:{} textHandlerId: "
                                    + "{}",
                            context.get("x-request-id"),
                            ws.remoteAddress(),
                            ws.binaryHandlerID(),
                            ws.textHandlerID());
                });
            } else {
                ws.reject(responseCode);
            }
        }).listen(8081);
    }
}