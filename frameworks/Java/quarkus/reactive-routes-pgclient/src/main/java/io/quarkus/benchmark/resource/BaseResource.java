package io.quarkus.benchmark.resource;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.quarkus.scheduler.Scheduled;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.JacksonCodec;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.PostConstruct;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public abstract class BaseResource {

    // TODO verify how to override/replace io.quarkus.vertx.runtime.jackson.QuarkusJacksonFactory in io.vertx.core.spi.JsonFactory
    private static final JacksonCodec JACKSON_CODEC = new JacksonCodec();
    private static final CharSequence SERVER_HEADER_VALUE = HttpHeaders.createOptimized("Quarkus");
    private CharSequence date;

    @PostConstruct
    public void init() {
        updateDate();
    }

    @Scheduled(every = "1s")
    void updateDate() {
        date = HttpHeaders.createOptimized(DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()));
    }

    void addDefaultheaders(final RoutingContext rc) {
        final var headers = rc.response().headers();
        headers.add(HttpHeaders.SERVER, SERVER_HEADER_VALUE);
        headers.add(HttpHeaders.DATE, date);
    }

    void sendJson(final RoutingContext rc, final JsonObject json) {
        addDefaultheaders(rc);
        var response = rc.response();
        response.headers().add(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        response.end(JACKSON_CODEC.toBuffer(json, false), null);
    }

    void sendJson(final RoutingContext rc, final JsonArray json) {
        addDefaultheaders(rc);
        var response = rc.response();
        response.headers().add(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        response.end(JACKSON_CODEC.toBuffer(json, false), null);
    }

    Void handleFail(final RoutingContext rc, final Throwable t) {
        rc.response().setStatusCode(500).end(t.toString());
        return null;
    }

}