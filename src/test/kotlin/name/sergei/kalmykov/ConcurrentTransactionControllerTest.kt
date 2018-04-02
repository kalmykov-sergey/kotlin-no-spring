package name.sergei.kalmykov

import io.restassured.RestAssured
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import spark.Spark
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class ConcurrentTransactionControllerTest {

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

    private val executorService = Executors.newFixedThreadPool(50)
    private val successful = AtomicLong()
    private val failed = AtomicLong()

    class TransferTask(val from: Long, val to: Long, val amount: Long) : Runnable {
        override fun run() {
            val transaction = Transaction.Dto(UUID.randomUUID().toString(), amount, from, to, Transaction.State.NEW)
            val state = Transaction.State.valueOf(RestAssured.given().body(transaction)
                    .`when`().put("/transactions")
                    .then()
                    .extract().path<String>("data.state"))
//            when(state) {
//                Transaction.State.SUCCESS -> successful.incrementAndGet()
//                Transaction.State.INSUFFICIENT_MONEY -> failed.incrementAndGet()
//                else -> println("Unreachable case")
//            }
        }
    }

    @Test
    fun `should be consistent and deadlock-free`() {

        val to = RestAssured.given().body(Account.Dto(0, 10))
                .`when`().post("/accounts")
                .then().log().all()
                .statusCode(201)
                .body("data.amount", CoreMatchers.equalTo(10))
                .extract().path<Int>("data.id").toLong()

        val from = RestAssured.given().body(Account.Dto(0, 90))
                .`when`().post("/accounts")
                .then().log().all()
                .statusCode(201)
                .body("data.amount", CoreMatchers.equalTo(90))
                .extract().path<Int>("data.id").toLong()

        val cycles = 10000
        val seconds = 90L
        for (i in 1..cycles) {
            executorService.submit(TransferTask(from, to, Random().nextInt(10).toLong()))
            executorService.submit(TransferTask(to, from, Random().nextInt(10).toLong()))
        }

        executorService.shutdown()
        if (!executorService.awaitTermination(seconds, TimeUnit.SECONDS)) {
            executorService.shutdownNow()
            while (!executorService.isTerminated) {
                TimeUnit.SECONDS.sleep(1)
            }
        }
        // now all transactions finished or interrupted
        // so account amounts are fixed

        val fromAmount = RestAssured.`when`().get("/accounts/{id}", from)
                .then().log().all()
                .extract().path<Int>("data.amount").toLong()

        val toAmount = RestAssured.`when`().get("/accounts/{id}", to)
                .then().log().all()
                .extract().path<Int>("data.amount").toLong()

        Assertions.assertEquals(100, fromAmount + toAmount)
        val interrupted = (cycles * 2) - successful.get() - failed.get()
        println("Transactions in $seconds secs: $successful successful, $failed failed and $interrupted interrupted")
    }

}