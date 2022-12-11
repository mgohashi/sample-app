package io.mohashi.service;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import io.mohashi.fixture.PanacheEntityBaseWithFixture;
import io.mohashi.model.User;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class UserService {

    public Uni<User> get(Long id) {
        return User.findById(id);
    }

    public Uni<List<User>> list(UserSearch userSearch) {
        return User.findBySearch(userSearch).list();
    }

    public Uni<User> persist(User user) {
        if (user.getId() != null) {
            User persistedUser = (User)User.findById(user.getId()).await().indefinitely();
            persistedUser.setName(user.getName());
            persistedUser.setEmail(user.getEmail());
            user = persistedUser;
        }
        return user.persistAndFlush();
    }

    public Uni<Void> delete(Long id) {
        return User.findById(id)    
            .onItem().ifNull().failWith(new NotFoundException("User has not been found"))
            .onItem().transformToUni(this::callDeleteAndFlush);
    }

    private Uni<Void> callDeleteAndFlush(PanacheEntityBase stored) {
        return ((PanacheEntityBaseWithFixture)stored).deleteAndFlush();
    }
}
