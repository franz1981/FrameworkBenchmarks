package io.quarkus.benchmark.resource;

import com.fizzed.rocker.Rocker;
import com.fizzed.rocker.RockerOutput;
import io.quarkus.benchmark.model.Fortune;
import io.quarkus.benchmark.repository.FortuneRepository;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpHeaders;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collections;
import java.util.Comparator;

@Path("/fortunes")
public class FortuneResource  {

    @Inject
    FortuneRepository repository;

    private Comparator<Fortune> fortuneComparator;

    private static final String FORTUNES_MAP_KEY = "fortunes";
    private static final String FORTUNES_TEMPLATE_FILENAME = "Fortunes.rocker.html";

    public FortuneResource() {
        fortuneComparator = Comparator.comparing(fortune -> fortune.getMessage());
    }

    @Produces("text/html; charset=UTF-8")
    @GET
    public Uni<String> fortunes() {
        return repository.findAll()
                .map(fortunes -> {
                    fortunes.add(new Fortune(0, "Additional fortune added at request time."));
                    fortunes.sort(fortuneComparator);
                    RockerOutput output = Rocker.template(FORTUNES_TEMPLATE_FILENAME)
                            .bind(Collections.singletonMap(FORTUNES_MAP_KEY, fortunes))
                            .render();

                    return output.toString();
                });
    }
}
