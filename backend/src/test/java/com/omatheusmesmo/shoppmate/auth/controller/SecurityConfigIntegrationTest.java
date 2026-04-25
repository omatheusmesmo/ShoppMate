package com.omatheusmesmo.shoppmate.auth.controller;

import com.omatheusmesmo.shoppmate.shared.testcontainers.AbstractIntegrationTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityConfigIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    void csrfToken_IsSetInCookie_OnPostRequest() {
        given().port(port).contentType("application/json")
                .body("{\"email\": \"" + UUID.randomUUID()
                        + "@example.com\", \"fullName\": \"Test\", \"password\": \"Pass123!\"}")
                .when().post("/auth/sign").then().cookie("XSRF-TOKEN");
    }

    @Test
    void csrfToken_RequiredForPost_WithoutToken_ReturnsForbidden() {
        given().port(port).contentType("application/json").body("{\"name\": \"Test\"}").when().post("/category").then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void csrfToken_WithValidToken_DoesNotRejectRequest() {
        // First, sign up a user (CSRF token is set during sign-up response)
        String uniqueEmail = UUID.randomUUID() + "@example.com";

        Response signUpResponse = given().port(port).contentType("application/json")
                .body("{\"email\": \"" + uniqueEmail + "\", \"fullName\": \"Test\", \"password\": \"Pass123!\"}").when()
                .post("/auth/sign");

        // Extract XSRF-TOKEN cookie from the sign-up response
        String xsrfToken = signUpResponse.getDetailedCookie("XSRF-TOKEN").getValue();

        // Login to get JWT token (include CSRF token in request)
        Response loginResponse = given().port(port).contentType("application/json").header("X-XSRF-TOKEN", xsrfToken)
                .cookie("XSRF-TOKEN", xsrfToken)
                .body("{\"email\": \"" + uniqueEmail + "\", \"password\": \"Pass123!\"}").when().post("/auth/login");

        String jwtToken = loginResponse.getBody().asString();

        // Make a request to protected endpoint with both JWT and CSRF tokens
        Response postResponse = given().port(port).contentType("application/json")
                .header("Authorization", "Bearer " + jwtToken).header("X-XSRF-TOKEN", xsrfToken)
                .cookie("XSRF-TOKEN", xsrfToken).body("{\"name\": \"Test\"}").when().post("/category");

        // Should succeed (201 Created), not 403 Forbidden
        assertThat(postResponse.getStatusCode()).as("Request should not be rejected due to CSRF when token is provided")
                .isEqualTo(201);
    }

    @Test
    void csrfProtection_IgnoredForAuthEndpoints() {
        given().port(port).contentType("application/json")
                .body("{\"email\": \"" + UUID.randomUUID()
                        + "@example.com\", \"fullName\": \"Test\", \"password\": \"Pass123!\"}")
                .when().post("/auth/sign").then().statusCode(anyOf(is(200), is(201)));
    }

    @Test
    void corsConfig_AllowedOrigin_ReturnsCorsHeaders() {
        given().port(port).header("Origin", "http://localhost:4200").header("Access-Control-Request-Method", "POST")
                .when().options("/auth/sign").then().statusCode(HttpStatus.OK.value())
                .header("Access-Control-Allow-Origin", "http://localhost:4200")
                .header("Access-Control-Allow-Credentials", "true");
    }

    @Test
    void corsConfig_DisallowedOrigin_DoesNotReturnCorsHeaders() {
        given().port(port).header("Origin", "http://malicious-site.com").header("Access-Control-Request-Method", "POST")
                .when().options("/auth/sign").then().header("Access-Control-Allow-Origin", equalTo(null));
    }

    @Test
    void corsConfig_AllowedMethods_IncludesRequiredMethods() {
        given().port(port).header("Origin", "http://localhost:4200").header("Access-Control-Request-Method", "POST")
                .when().options("/auth/sign").then().statusCode(HttpStatus.OK.value())
                .header("Access-Control-Allow-Methods", notNullValue());
    }

    @Test
    void corsConfig_XsrfTokenHeader_IsAllowed() {
        given().port(port).header("Origin", "http://localhost:4200").header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "X-XSRF-TOKEN, Authorization, Content-Type").when()
                .options("/category").then().statusCode(HttpStatus.OK.value())
                .header("Access-Control-Allow-Headers", notNullValue());
    }

    @Test
    void optionsRequest_IsPermittedForAll() {
        given().port(port).header("Origin", "http://localhost:4200").when().options("/category").then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void unauthenticatedRequest_ToProtectedEndpoint_ReturnsForbidden() {
        given().port(port).when().get("/category").then().statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void swaggerEndpoints_ArePubliclyAccessible() {
        given().port(port).redirects().follow(false).when().get("/swagger-ui.html").then()
                .statusCode(HttpStatus.FOUND.value());
    }
}
