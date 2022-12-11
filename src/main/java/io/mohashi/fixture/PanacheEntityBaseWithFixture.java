package io.mohashi.fixture;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;

public abstract class PanacheEntityBaseWithFixture extends PanacheEntityBase {
    public Uni<Void> deleteAndFlush() {
        return getSession().flatMap(s -> 
            s.remove(this).chain(() -> s.flush()));
    }
}
