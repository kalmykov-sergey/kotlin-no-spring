package name.sergei.kalmykov

import io.restassured.RestAssured
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import spark.Spark
import java.util.*
import java.util.concurrent.TimeUnit

class TransactionControllerTest {

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
    fun `should charge account`() {
        val serviceAccountId = RestAssured.given().body(Account.Dto(0, 1_000_000_000))
                .`when`().post("/accounts")
                .then().log().all()
                .statusCode(201)
                .extract().path<Int>("data.id").toLong()
        val toId = RestAssured.given().body(Account.Dto(0, 0))
                .`when`().post("/accounts")
                .then().log().all()
                .statusCode(201)
                .body("data.amount", CoreMatchers.equalTo(0))
                .extract().path<Int>("data.id").toLong()
        RestAssured.given().body(Transaction.Dto(UUID.randomUUID().toString(), 10, serviceAccountId, toId, Transaction.State.NEW))
                .`when`().put("/transactions")
                .then().log().all()
                .statusCode(200)
                .body("data.state", CoreMatchers.equalTo(Transaction.State.SUCCESS.name))

        RestAssured.`when`().get("/accounts/{id}", toId)
                .then().log().all()
                .statusCode(200)
                .body("data.amount", CoreMatchers.equalTo(10))

    }

    @Test
    fun `should transfer money`() {
        val toId = RestAssured.given().body(Account.Dto(0, 10))
                .`when`().post("/accounts")
                .then().log().all()
                .statusCode(201)
                .extract().path<Int>("data.id").toLong()
        val fromId = RestAssured.given().body(Account.Dto(0, 90))
                .`when`().post("/accounts")
                .then().log().all()
                .statusCode(201)
                .extract().path<Int>("data.id").toLong()

        RestAssured.given().body(Transaction.Dto(UUID.randomUUID().toString(), 100, fromId, toId, Transaction.State.NEW))
                .`when`().put("/transactions")
                .then().log().all()
                .statusCode(400)
                .body("data.state", CoreMatchers.equalTo(Transaction.State.INSUFFICIENT_MONEY.name))

        RestAssured.given().body(Transaction.Dto(UUID.randomUUID().toString(), 70, fromId, toId, Transaction.State.NEW))
                .`when`().put("/transactions")
                .then().log().all()
                .statusCode(200)
                .body("data.state", CoreMatchers.equalTo(Transaction.State.SUCCESS.name))

        RestAssured.`when`().get("/accounts/{id}", fromId)
                .then()
                .body("data.amount", CoreMatchers.equalTo(20))
        RestAssured.`when`().get("/accounts/{id}", toId)
                .then()
                .body("data.amount", CoreMatchers.equalTo(80))


    }
}