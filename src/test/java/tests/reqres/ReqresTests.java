package tests.reqres;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static filters.CustomLogFilter.customLogFilter;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasKey;

public class ReqresTests {
    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "https://reqres.in/";
    }


    @Test
    void singleUserNotFound() {
        // https://reqres.in/api/users/23
        //     404

        given()
                .filter(new AllureRestAssured())
                .when()
                .log().all()
                .get("api/users/23")
                .then()
                .log().all()
                .statusCode(404);
    }

    @Test
    void listResource() {
        // https://reqres.in/api/unknown

        //     "support": {
        //        "url": "https://reqres.in/#support-heading",
        //     200


        given()
                .filter(customLogFilter().withCustomTemplates())
                .when()
                .log().all()
                .get("api/unknown")
                .then()
                .log().all()
                .statusCode(200)
                .body("support", hasKey("url"))
                .body("support.url", is("https://reqres.in/#support-heading"));
    }

    @Test
    void createUser() {
        // https://reqres.in/api/users
        /*
            {
               "name": "morpheus",
               "job": "leader"
}
         */
        //     "name": "morpheus",
        //     201

        String data = "{ \"name\": \"morpheus\", \"job\": \"leader\" }";

        given()
                .filter(customLogFilter().withCustomTemplates())
                .contentType(JSON)
                .body(data)
                .when()
                .log().all()
                .post("api/users")
                .then()
                .log().all()
                .statusCode(201)
                .body("name", is("morpheus"));
    }

    @Test
    void registerSuccessful() {
        // https://reqres.in/api/register
        /*
            {
                "email": "eve.holt@reqres.in",
                "password": "pistol"
            }
         */
        //     "token": "QpwL5tke4Pnpja7X4",
        //     200

        String data = "{ \"email\": \"eve.holt@reqres.in\", \"password\": \"pistol\" }";

        given()
                .filter(customLogFilter().withCustomTemplates())
                .contentType(JSON)
                .body(data)
                .when()
                .log().all()
                .post("api/register")
                .then()
                .log().all()
                .statusCode(200)
                .body("token", is("QpwL5tke4Pnpja7X4"));
    }

    @Test
    void registerUnsuccessful() {
        // https://reqres.in/api/register
        /*
            {
                 "email": "sydney@fife"
            }
         */
        //     "error": "Missing password"
        //     400

        String data = "{ \"email\": \"sydney@fife\" }";

        given()
                .filter(customLogFilter().withCustomTemplates())
                .contentType(JSON)
                .body(data)
                .when()
                .log().all()
                .post("api/register")
                .then()
                .log().all()
                .statusCode(400)
                .body("error", is("Missing password"));
    }
}