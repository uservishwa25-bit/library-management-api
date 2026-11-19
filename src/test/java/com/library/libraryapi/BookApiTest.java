package com.library.libraryapi;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookApiTest {

    @Value("${local.server.port}")
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    private int createBook() {
        return given().contentType(ContentType.JSON)
                .body("{\"title\":\"Clean Code\",\"author\":\"Robert Martin\",\"isbn\":\"111\"}")
                .when().post("/books")
                .then().statusCode(201)
                .extract().path("id");
    }

    @Test
    void createBook_shouldReturn201() {
        given().contentType(ContentType.JSON)
            .body("{\"title\":\"Clean Code\",\"author\":\"Robert Martin\",\"isbn\":\"111\"}")
        .when().post("/books")
        .then().statusCode(201)
            .body("title", equalTo("Clean Code"))
            .body("author", equalTo("Robert Martin"))
            .body("id", notNullValue());
    }

    @Test
    void getAllBooks_shouldReturn200() {
        createBook();
        given().when().get("/books")
        .then().statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    void getBookById_shouldReturnBook() {
        int id = createBook();
        given().when().get("/books/" + id)
        .then().statusCode(200)
            .body("id", equalTo(id))
            .body("isbn", equalTo("111"));
    }

    @Test
    void getBookById_notFound_shouldReturn404() {
        given().when().get("/books/99999")
        .then().statusCode(404);
    }

    @Test
    void updateBook_shouldChangeTitle() {
        int id = createBook();
        given().contentType(ContentType.JSON)
            .body("{\"title\":\"Clean Architecture\",\"author\":\"Robert Martin\",\"isbn\":\"111\",\"available\":false}")
        .when().put("/books/" + id)
        .then().statusCode(200)
            .body("title", equalTo("Clean Architecture"))
            .body("available", equalTo(false));
    }

    @Test
    void deleteBook_shouldReturn204_thenGet404() {
        int id = createBook();
        given().when().delete("/books/" + id).then().statusCode(204);
        given().when().get("/books/" + id).then().statusCode(404);
    }
}