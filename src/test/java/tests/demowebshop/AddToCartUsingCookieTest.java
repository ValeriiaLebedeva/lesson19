package tests.demowebshop;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import config.AppConfig;
import io.qameta.allure.selenide.AllureSelenide;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.remote.DesiredCapabilities;


import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static filters.CustomLogFilter.customLogFilter;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class AddToCartUsingCookieTest {

    public static AppConfig webShopConfig = ConfigFactory.create(AppConfig.class, System.getProperties());

    @BeforeAll
    static void configureBaseUrl() {
        RestAssured.baseURI = webShopConfig.apiUrl();
        Configuration.baseUrl = webShopConfig.webUrl();
    }
    @BeforeAll
    static void setup() {
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide());
        Configuration.startMaximized = true;
        Configuration.remote = "https://user1:1234@selenoid.autotests.cloud/wd/hub/";

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("enableVNC", true);
        capabilities.setCapability("enableVideo", true);

        Configuration.browserCapabilities = capabilities;
    }

    Integer getItemsCountFromCart(String value) {
        if (value != null) {
            return Integer.parseInt(value.substring(1, value.length() - 1));
        }
        return 0;
    }


    @Test
    void checkCartNumberTest() {

        //Get cookie
        String authorizationCookie =
                given()
                        .filter(customLogFilter().withCustomTemplates())
                        .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                        .formParam("Email", webShopConfig.userLogin())
                        .formParam("Password", webShopConfig.userPassword())
                        .when()
                        .log().all()
                        .post("/login")
                        .then()
                        .log().all()
                        .statusCode(302)
                        .extract()
                        .cookie("NOPCOMMERCE.AUTH");


        //Open minimal content, because cookie can be set when site is opened
        open("/Themes/DefaultClean/Content/images/logo.png");


        //Set cookie to browser
        getWebDriver().manage().addCookie(
                new Cookie("NOPCOMMERCE.AUTH", authorizationCookie));


        //Open cart page
        open("/cart");


        //Get item cart number before adding new item
        String cartItemNumber = $(".cart-qty").getText();


        //Add item to cart using api and check the new number is greater by 1 number before adding the item to cart
        Response response =
                given()
                        .filter(customLogFilter().withCustomTemplates())
                        .cookie("NOPCOMMERCE.AUTH", authorizationCookie)
                        .when()
                        .log().all()
                        .post("addproducttocart/catalog/13/1/1")
                        .then()
                        .log().all()
                        .statusCode(200)
                        .body("success", is(true))
                        .body("message", is("The product has been added to your <a href=\"/cart\">shopping cart</a>"))
                        .body("updatetopcartsectionhtml", is("(" + (getItemsCountFromCart(cartItemNumber) + 1) + ")"))
                        .extract().response();
        System.out.println("Response = " + response.path("updatetopcartsectionhtml"));
        System.out.println(cartItemNumber);

    }
}