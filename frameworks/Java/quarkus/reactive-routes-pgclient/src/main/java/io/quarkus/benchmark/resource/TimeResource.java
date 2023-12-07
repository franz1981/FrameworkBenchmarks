package io.quarkus.benchmark.resource;

import io.quarkus.scheduler.Scheduled;
import io.vertx.core.http.HttpHeaders;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Singleton
public class TimeResource {

    CharSequence date;

    @PostConstruct
    public void init() {
        updateDate();
    }

    @Scheduled(every = "1s")
    void updateDate() {
        date = HttpHeaders.createOptimized(DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()));
    }

}
