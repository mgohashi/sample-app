package io.mohashi.resource;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.mohashi.model.User;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class UserResourceTest {

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
    public void testGet() {
        populateDbWithUserList();

        List<User> expected = getExpectedUsers()
            .collect().asList().await().indefinitely();

        given()
        .when()
            .get("/api/v1/user")
        .then()
            //.log().all()
            .statusCode(is(200))
            .assertThat()
                .contentType("application/json")
                .body("size()", equalTo(3))
                .body("[0].id", equalTo(expected.get(0).getId().intValue()))
                .body("[0].name", equalTo(expected.get(0).getName()))
                .body("[0].email", equalTo(expected.get(0).getEmail()));
    }

    private Multi<User> getExpectedUsers() {
        return client
            .query("SELECT id, name, email from users order by id").execute()
            .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
            .onItem().transform(User::from);
    }
}
