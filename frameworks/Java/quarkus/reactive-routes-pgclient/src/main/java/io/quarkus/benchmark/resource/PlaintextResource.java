package io.quarkus.benchmark.resource;

import java.nio.charset.StandardCharsets;

import javax.enterprise.context.ApplicationScoped;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.quarkus.vertx.web.Route;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class PlaintextResource {
    private static final String HELLO_WORLD = "Hello, world!";
    private static final Buffer HELLO_WORLD_BUFFER;

    private static final CharSequence CONTENT_TYPE_HEADER_NAME = HttpHeaders.createOptimized("Content-Type");
    private static final CharSequence CONTENT_TYPE_HEADER_VALUE = HttpHeaders.createOptimized("text/plain");

    static {
        ByteBuf nettyBuffer = ByteBufAllocator.DEFAULT.directBuffer();
        nettyBuffer.writeBytes(HELLO_WORLD.getBytes(StandardCharsets.UTF_8));
        HELLO_WORLD_BUFFER = Buffer.buffer(nettyBuffer);
    }

    @Route(path = "plaintext")
    public void plaintext(RoutingContext rc) {
        rc.response().putHeader(CONTENT_TYPE_HEADER_NAME, CONTENT_TYPE_HEADER_VALUE);
        rc.response().end(HELLO_WORLD_BUFFER);
    }
}
