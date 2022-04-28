package io.quarkus.benchmark.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.inject.Singleton;

import org.hibernate.reactive.mutiny.Mutiny;

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
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            int MAX = 10000;
            Uni<Void>[] unis = new Uni[MAX];
            for (int i=0; i<MAX; i++) {
                final World world = new World();
                world.setId(i + 1);
                world.setRandomNumber(1 + random.nextInt(10000));
                unis[i] = s.persist(world).map(v -> null);
            }
            return Uni.combine().all().unis(unis).combinedWith(l -> null)
                    .flatMap(v -> s.flush())
                    .map(v -> null);
        });
    }

    public Uni<World> findStateless(int id) {
        return inStatelessSession(session -> session.get(World.class, id));
    }

    public Uni<List<World>> update(Mutiny.Session session, List<World> worlds) {
        return session
                .setBatchSize(worlds.size())
                .flush()
                .map(v -> worlds);
    }

    public Uni<List<World>> findStateless(Mutiny.StatelessSession s, int[] ids) {
        //The rules require individual load: we can't use the Hibernate feature which allows load by multiple IDs
        // as one single operation as Hibernate is too smart and will switch to use batched loads automatically.
        // Hence, use this awkward alternative:
        List<Uni<World>> l = new ArrayList<>(ids.length);
        for (Integer id : ids) {
            l.add(s.get(World.class, id));
        }
        return Uni.join().all(l).andFailFast();
    }

    public Uni<List<World>> findManaged(Mutiny.Session s, int[] ids) {
        final List<World> worlds = new ArrayList<>(ids.length);
        //The rules require individual load: we can't use the Hibernate feature which allows load by multiple IDs
        // as one single operation as Hibernate is too smart and will switch to use batched loads.
        // But also, we can't use "Uni#join" as we did in the above method as managed entities shouldn't use pipelining -
        // so we also have to avoid Mutiny optimising things by establishing an explicit chain:
        Uni<Void> loopRoot = Uni.createFrom().voidItem();
        for (Integer id : ids) {
            loopRoot = loopRoot.chain(() -> s.find(World.class, id).invoke(word -> worlds.add(word)).replaceWithVoid());
        }
        return loopRoot.map(v -> worlds);
    }

    public Uni<List<World>> findStateless(int[] ids) {
        return inStatelessSession(session -> findStateless(session, ids));
    }

}
