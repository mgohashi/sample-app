package io.mohashi.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import javax.inject.Inject;

import org.hamcrest.core.IsNull;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.mohashi.model.User;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

@QuarkusTest
public class UserServiceTest {
    @Inject
    UserService userService;
    @Inject
    Mutiny.SessionFactory sf;
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
    public void testFindAllUsers() {
        populateDbWithUserList();

        List<User> expected = getExpectedUsers4FindUsers().collect()
            .asList().await().indefinitely();
        
        Uni<List<User>> uni = userService.list(ImmutableUserSearch.builder().build());
        UniAssertSubscriber<List<User>> subs = uni
            .onFailure().invoke(t -> t.printStackTrace())
            .subscribe().withSubscriber(UniAssertSubscriber.create());
        
        subs.awaitItem().assertCompleted().assertItem(expected);
    }

    private Multi<User> getExpectedUsers4FindUsers() {
        return client
            .query("SELECT id, name, email from users").execute()
            .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
            .onItem().transform(User::from);
    }

    @Test
    public void testFindAllByName() {
        populateDbWithUserList();

        List<User> expected = getExpectedUsers4FindByName("John").collect()
            .asList().await().indefinitely();

        UserSearch userSearch = ImmutableUserSearch.builder().name("John").build();
        
        Uni<List<User>> uni = userService.list(userSearch);
        UniAssertSubscriber<List<User>> subs = uni
            .onFailure().invoke(t -> t.printStackTrace())
            .subscribe().withSubscriber(UniAssertSubscriber.create());
        
        subs.awaitItem().assertCompleted().assertItem(expected);
    }

    private Multi<User> getExpectedUsers4FindByName(String name) {
        return client
            .preparedQuery("SELECT id, name, email from users WHERE name like $1")
                .execute(Tuple.of("%"+name+"%"))
            .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
            .onItem().transform(User::from);
    }

    @Test
    public void testFindAllByEmail() {
        populateDbWithUserList();

        List<User> expected = getExpectedUsers4FindByEmail("maria@test").collect()
            .asList().await().indefinitely();

        UserSearch userSearch = ImmutableUserSearch.builder().email("maria@test").build();
        
        Uni<List<User>> uni = userService.list(userSearch);
        UniAssertSubscriber<List<User>> subs = uni
            .onFailure().invoke(t -> t.printStackTrace())
            .subscribe().withSubscriber(UniAssertSubscriber.create());
        
        subs.awaitItem().assertCompleted().assertItem(expected);
    }

    private Multi<User> getExpectedUsers4FindByEmail(String email) {
        return client
            .preparedQuery("SELECT id, name, email from users WHERE email like $1")
                .execute(Tuple.of("%"+email+"%"))
            .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
            .onItem().transform(User::from);
    }

    @Test
    public void testInsertUser() {
        User expected = new User(null, "John", "john@test.com");
        User user = new User(null,"John", "john@test.com");
        
        Long id = userService.persist(user).await().indefinitely().getId();

        expected.setId(id);

        UniAssertSubscriber<User> subs = getPersistedUser(id)
            .onFailure().invoke(t -> t.printStackTrace())
            .subscribe().withSubscriber(UniAssertSubscriber.create());
        subs.awaitItem().assertCompleted().assertItem(expected);
    }

    private Uni<User> getPersistedUser(Long id) {
        return client.preparedQuery("SELECT id, name, email FROM users WHERE id = $1").execute(Tuple.of(id))
            .onItem().transform(RowSet::iterator)
            .onItem().transform(it -> it.hasNext() ? User.from(it.next()) : null);
    }

    @Test
    public void testUpdateUser() {
        User user = new User(null, "Johnny", "johnny@test.com");
        User expected = createUserInDB(user).await().indefinitely();
        expected.setName("John");
        expected.setEmail("john@test.com");
        userService.persist(user).await().indefinitely();
        User actual = getPersistedUser(expected.getId()).await().indefinitely();
        assertThat(actual, equalTo(expected));
    }

    private Uni<User> createUserInDB(User user) {
        return client.preparedQuery("INSERT INTO users (id, name, email) VALUES (nextval('user_id_seq'), $1, $2)").execute(Tuple.of(user.getName(), user.getEmail()))
            .chain(() -> client.preparedQuery("SELECT MAX(id) as id FROM users").execute()
            .map(RowSet::iterator)
            .flatMap(it -> { 
                if (it.hasNext()) {
                    user.setId(it.next().getLong("id"));
                    System.out.println(user.getId());
                    return Uni.createFrom().item(user);
                }
                return Uni.createFrom().nullItem();
            }));
    }

    @Test
    public void testDeleteUser() {
        populateDbWithUserList();

        User user = new User(getStoredId().await().indefinitely(), null, null);

        userService.delete(user.getId())
            .onFailure().invoke(t -> t.printStackTrace())
            .await().indefinitely();

        UniAssertSubscriber<User> subs = getPersistedUser(user.getId())
            .onFailure().invoke(t -> t.printStackTrace())
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        User foundUser = subs.awaitItem().assertCompleted().getItem();
        assertThat(foundUser, IsNull.nullValue(User.class));
    }

    private Uni<Long> getStoredId() {
        return client.query("SELECT max(id) as id FROM users").execute()
            .map(RowSet::iterator)
            .map(it -> it.hasNext() ? it.next().getLong("id") : null);
    }

}
