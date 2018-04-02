package name.sergei.kalmykov

import com.google.gson.Gson
import name.sergei.kalmykov.JSend.Status.SUCCESS
import spark.Request
import spark.Response
import spark.ResponseTransformer
import spark.Route
import spark.Spark.get
import spark.Spark.port

const val jsonType = "application/json"

fun main(args: Array<String>) {
    val port = if (args.isEmpty()) 8080 else args[0].toInt()
    val debug = if (args.size < 2) false else args[1].toBoolean()
    port(port)

    val accountService = AccountService()
    val transactionService = TransactionService(accountService, debug)
    val accountController = AccountController(accountService)
    val transactionController = TransactionController(transactionService)
    accountController.registerRoutes()
    transactionController.registerRoutes()

    get("/ping", Route { _: Request, response: Response ->
        response.header("content-type", jsonType)
        JSend(SUCCESS, "OK")
    }, JsonTransformer()
    )
}

class JsonTransformer : ResponseTransformer {

    private val gson = Gson()

    override fun render(model: Any): String {
        return gson.toJson(model)
    }
}