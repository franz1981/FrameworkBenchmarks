package io.quarkus.benchmark.resource;

import io.quarkus.benchmark.model.Fortune;
import io.quarkus.benchmark.repository.FortuneRepository;
import io.quarkus.vertx.web.Route;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.rocker.RockerTemplateEngine;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class FortuneResource extends BaseResource {

    @Inject
    FortuneRepository repository;

    private Comparator<Fortune> fortuneComparator;
    private final RockerTemplateEngine templeEngine;

    private static final String FORTUNES_MAP_KEY = "fortunes" ;
    private static final String FORTUNES_TEMPLATE_FILENAME = "Fortunes.rocker.html" ;
    private static final CharSequence CONTENT_TYPE_HEADER_VALUE = HttpHeaders.createOptimized("text/html; charset=UTF-8");

    public FortuneResource() {
        templeEngine = RockerTemplateEngine.create();
        fortuneComparator = Comparator.comparing(fortune -> fortune.getMessage());
    }

    @Route(path = "fortunes")
    public void fortunes(RoutingContext rc) {
        repository.findAll()
                .subscribe().with(fortunes -> {
                    fortunes.add(new Fortune(0, "Additional fortune added at request time."));
                    fortunes.sort(fortuneComparator);
                    templeEngine.render(Collections.singletonMap(FORTUNES_MAP_KEY, fortunes), FORTUNES_TEMPLATE_FILENAME, res -> {
                        if (res.succeeded()) {
                            rc.response()
                                    .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_HEADER_VALUE)
                                    .end(res.result());
                        } else {
                            rc.fail(res.cause());
                        }
                    });
                },
                t -> handleFail(rc, t));
    }
}
