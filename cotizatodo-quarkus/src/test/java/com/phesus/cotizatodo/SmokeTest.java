package com.phesus.cotizatodo;

import com.phesus.cotizatodo.entity.User;
import com.phesus.cotizatodo.entity.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.filter.cookie.CookieFilter;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * Smoke test end-to-end: páginas públicas, login form-auth, guardado de
 * cotización, generación de PDF y las correcciones de IDOR.
 */
@QuarkusTest
class SmokeTest {

    @Inject
    UserRepository users;

    @BeforeEach
    @Transactional
    void seed() {
        if (users.findByUsername("owner") == null) {
            User u = new User("owner", BcryptUtil.bcryptHash("pw"), true);
            u.setEmail("owner@e.com");
            u.setCountry("MX");
            u.addUserRole("ROLE_USER");
            users.persist(u);
        }
        if (users.findByUsername("intruder") == null) {
            User u = new User("intruder", BcryptUtil.bcryptHash("pw"), true);
            u.setEmail("intruder@e.com");
            u.setCountry("MX");
            u.addUserRole("ROLE_USER");
            users.persist(u);
        }
    }

    @Test
    void publicPagesAreReachable() {
        given().when().get("/").then().statusCode(200);
        given().when().get("/login").then().statusCode(200);
        given().when().get("/quotes/new").then().statusCode(200);
    }

    @Test
    void protectedPageRedirectsToLogin() {
        given().redirects().follow(false)
                .when().get("/quotes")
                .then().statusCode(302);
    }

    @Test
    void loginSaveQuoteAndRenderPdf() {
        CookieFilter session = new CookieFilter();

        given().filter(session).redirects().follow(false)
                .formParam("username", "owner").formParam("password", "pw")
                .when().post("/j_security_check")
                .then().statusCode(302);

        given().filter(session).redirects().follow(false)
                .multiPart("layout", "")
                .multiPart("title", "COTIZACION")
                .multiPart("targetName", "Cliente")
                .multiPart("description[]", "Item")
                .multiPart("quantity[]", "1")
                .multiPart("unitPrice[]", "50.00")
                .multiPart("rowTotal[]", "50.00")
                .multiPart("total", "50.00")
                .multiPart("taxesDescription", "IVA")
                .when().post("/quotes/save")
                .then().statusCode(303);

        given().filter(session)
                .when().get("/quotes/JSON?length=10&start=0&draw=1")
                .then().statusCode(200)
                .body("recordsFiltered", greaterThanOrEqualTo(1))
                .body("data[0].title", equalTo("COTIZACION"));

        given().filter(session)
                .multiPart("layout", "first_magenta")
                .multiPart("title", "COTIZACION")
                .multiPart("targetName", "Cliente")
                .multiPart("description[]", "Item")
                .multiPart("quantity[]", "1")
                .multiPart("unitPrice[]", "50.00")
                .multiPart("rowTotal[]", "50.00")
                .multiPart("total", "50.00")
                .multiPart("taxesDescription", "IVA")
                .when().post("/quotes/print/pdf")
                .then().statusCode(200)
                .contentType("application/pdf");
    }

    @Test
    void intruderCannotAccessOthersQuote() {
        // owner crea una cotización
        CookieFilter owner = new CookieFilter();
        given().filter(owner).redirects().follow(false)
                .formParam("username", "owner").formParam("password", "pw")
                .when().post("/j_security_check").then().statusCode(302);
        given().filter(owner).redirects().follow(false)
                .multiPart("layout", "").multiPart("title", "SECRETA")
                .multiPart("targetName", "X").multiPart("description[]", "I")
                .multiPart("quantity[]", "1").multiPart("unitPrice[]", "1")
                .multiPart("rowTotal[]", "1").multiPart("total", "1")
                .multiPart("taxesDescription", "IVA")
                .when().post("/quotes/save").then().statusCode(303);

        // id real de una cotización de owner
        int quoteId = given().filter(owner)
                .when().get("/quotes/JSON?length=1&start=0&draw=1")
                .then().statusCode(200)
                .extract().path("data[0].id");

        // intruder inicia sesión y busca la cotización ajena
        CookieFilter intruder = new CookieFilter();
        given().filter(intruder).redirects().follow(false)
                .formParam("username", "intruder").formParam("password", "pw")
                .when().post("/j_security_check").then().statusCode(302);

        given().filter(intruder).when().get("/quote/" + quoteId).then().statusCode(403);
        given().filter(intruder).when().get("/quotes/viewLogo/" + quoteId).then().statusCode(403);
    }
}
