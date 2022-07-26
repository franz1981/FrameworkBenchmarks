package io.quarkus.benchmark.repository;

import javax.inject.Singleton;

import io.quarkus.benchmark.utils.LocalRandom;
import io.quarkus.benchmark.utils.Randomizer;

import io.quarkus.benchmark.model.World;
import io.smallrye.mutiny.Uni;

@Singleton
public class WorldRepository extends BaseRepository {

    /**
     * This method is not required (nor specified) by the benchmark rules,
     * but is quite handy to seed a local database and be able to experiment
     * with the app locally.
     */
    public Uni<Void> createData() {
        return inSession(s -> {
            final LocalRandom random = Randomizer.current();
            int MAX = 10000;
            Uni<Void>[] unis = new Uni[MAX];
            for (int i = 0; i < MAX; i++) {
                final World world = new World();
                world.setId(i + 1);
                world.setRandomNumber(random.getNextRandom());
                unis[i] = s.persist(world).map(v -> null);
            }
            return Uni.combine().all().unis(unis).combinedWith(l -> null)
                    .flatMap(v -> s.flush())
                    .map(v -> null);
        });
    }

    public Uni<World> findStateless() {
        return inStatelessSession(session -> session.get(World.class, Randomizer.current().getNextRandom()));
    }

}
