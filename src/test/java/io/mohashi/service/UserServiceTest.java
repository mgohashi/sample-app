package io.mohashi.service;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.mohashi.model.User;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.mutiny.pgclient.PgPool;

@QuarkusTest
public class UserServiceTest {
    @Inject
    UserService userService;
    @Inject
    PgPool client;
    
    private void populateDbWithUserList() {
        client.query("DELETE FROM users").execute()
            .flatMap(r -> client.query("INSERT INTO users (id, name, email) VALUES (nextval('user_id_seq'),'John', 'john@test.com')").execute())
            .flatMap(r -> client.query("INSERT INTO users (id, name, email) VALUES (nextval('user_id_seq'),'Maria', 'maria@test.com')").execute())
            .flatMap(r -> client.query("INSERT INTO users (id, name, email) VALUES (nextval('user_id_seq'),'Monty', 'monty@test.com')").execute())
            .await().indefinitely();
    }

    @AfterEach
    public void deleteAllData() {
        client.query("DELETE FROM users").execute().await().indefinitely();
    }

    @Test
    public void testFind() {
        populateDbWithUserList();
        List<User> expected = getExpectedUsers().collect()
            .asList().await().indefinitely();
        
        Uni<List<User>> uni = userService.list();
        UniAssertSubscriber<List<User>> subs = uni
            .onFailure().invoke(t -> t.printStackTrace())
            .subscribe().withSubscriber(UniAssertSubscriber.create());
        
        subs.awaitItem().assertCompleted().assertItem(expected);
    }

    private Multi<User> getExpectedUsers() {
        return client
            .query("SELECT id, name, email from users").execute()
            .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
            .onItem().transform(User::from);
    }
}
