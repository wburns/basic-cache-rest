package org.acme.rest.json;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class FruitResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/v2/caches/test/foo")
          .then()
             .statusCode(404);

        given()
              .when().body("bar").put("/v2/caches/test/foo")
              .then().statusCode(204);

        given()
              .when().get("/v2/caches/test/foo")
              .then()
              .body(is("bar"))
              .statusCode(200);
    }

}