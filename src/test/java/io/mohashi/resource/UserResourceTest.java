package io.mohashi.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.mohashi.model.User;
import io.mohashi.service.ImmutableUserSearch;
import io.mohashi.service.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;

@QuarkusTest
public class UserResourceTest {

    @InjectMock
    UserService userService;

    @Test
    public void testGet() {
        List<User> expected = Arrays.asList(
            new User(1l, "John", "john@test.com"),
            new User(2l, "Maria", "maria@test.com"),
            new User(3l, "Faye", "faye@test.com"));

        Mockito.when(
            userService.list(ImmutableUserSearch.builder().build()))
                .thenReturn(Uni.createFrom().item(expected));

        List<User> actual = given()
            .accept(ContentType.JSON)
        .when()
            .get("/api/v1/user")
        .then()
            //.log().all()
            .assertThat()
                .statusCode(is(200))
                .contentType(ContentType.JSON)
                .body("size()", equalTo(3))
            .extract()
                .body().jsonPath().getList(".", User.class);

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void testGetByName() {
        List<User> expected = Arrays.asList(
            new User(1l, "John", "john@test.com"));

        Mockito.when(
            userService.list(ImmutableUserSearch.builder().name("John").build()))
                .thenReturn(Uni.createFrom().item(expected));

        List<User> actual = given()
            .accept(ContentType.JSON)
            .param("name", "John")
        .when()
            .get("/api/v1/user")
        .then()
            //.log().all()f
            .assertThat()
                .statusCode(is(200))
                .contentType(ContentType.JSON)
                .body("size()", equalTo(1))
            .extract()
                .body().jsonPath().getList(".", User.class);

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void testGetUser() {
        User expected = new User(1l, "John", "john@test.com");

        Mockito.when(userService.get(expected.getId())).thenReturn(Uni.createFrom().item(expected));

        User actual = given()
            .accept(ContentType.JSON)
        .when()
            .get("/api/v1/user/1")
        .then()
        .assertThat()
            .statusCode(is(200))
            .contentType(ContentType.JSON)
        .extract()
            .body().as(User.class);

        assertThat(actual, is(expected));
    }

    @Test
    public void testPost() {
        User expected = new User(1l, "John", "john@test.com");
        
        Mockito.when(userService.persist(expected)).thenReturn(Uni.createFrom().item(expected));

        User actual = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(expected)
        .when()
            .post("/api/v1/user")
        .then()
            // .log().all()
            .assertThat()
                .statusCode(equalTo(200))
            .extract().as(User.class);
        
        expected.setId(actual.getId());

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void testPut() {
        User expected = new User(1l, "John", "john@test.com");

        Mockito.when(userService.persist(expected)).thenReturn(Uni.createFrom().item(expected));

        User actual = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(expected)
        .when()
            .put("/api/v1/user")
        .then()
            // .log().all()
            .assertThat()
                .statusCode(equalTo(200))
            .extract().as(User.class);
        
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void testDelete() {
        Mockito.when(userService.delete(1l)).thenReturn(Uni.createFrom().voidItem());

        given()
        .when()
            .delete("/api/v1/user/1")
        .then()
            .assertThat()
                .statusCode(equalTo(200));
    }
}
