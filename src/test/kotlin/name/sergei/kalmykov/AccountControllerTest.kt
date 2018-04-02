package name.sergei.kalmykov

import io.restassured.RestAssured
import io.restassured.RestAssured.`when`
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import spark.Spark
import java.util.concurrent.TimeUnit

class AccountControllerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            RestAssured.port = 8080
            main(arrayOf(RestAssured.port.toString(), true.toString()))
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            Spark.stop()
            TimeUnit.SECONDS.sleep(1)
        }
    }


    @Test
    fun `test ping`() {
        `when`().get("/ping").then().log().all()
                .statusCode(200)
                .body("data", equalTo("OK"))
    }

    @Test
    fun `should create and read account`() {
        val accountId = given().body(Account.Dto(0, 1_000))
                .`when`().post("/accounts")
                .then().log().all()
                .statusCode(201)
                .body("data.amount", equalTo(1_000))
                .extract().path<Int>("data.id")
        `when`().get("/accounts/{id}", accountId)
                .then().log().all()
                .statusCode(200)
                .body("data.amount", equalTo(1_000))

        `when`().get("/accounts/{id}", 100)
                .then().log().all()
                .statusCode(404)

    }


}