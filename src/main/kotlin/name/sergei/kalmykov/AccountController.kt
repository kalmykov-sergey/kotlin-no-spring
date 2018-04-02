package name.sergei.kalmykov

import com.google.gson.Gson
import name.sergei.kalmykov.JSend.Status.FAIL
import name.sergei.kalmykov.JSend.Status.SUCCESS
import spark.Route
import spark.Spark.get
import spark.Spark.post

class AccountController(val accountService: AccountService) {
    private val gson = Gson()

    fun registerRoutes() {
        post("/accounts", jsonType, Route { request, response ->
            val dto = gson.fromJson(request.body(), Account.Dto::class.java)
            val account = accountService.createAccount(dto.amount)
            response.status(201)
            response.header("content-type", jsonType)
            JSend(SUCCESS, account.toDto())
        }, JsonTransformer())

        get("/accounts/:id", jsonType, Route { request, response ->
            val id = request.params("id").toLong()
            val account = accountService.getAccount(id)
            response.header("content-type", jsonType)
            if (account == null) {
                response.status(404)
                return@Route JSend(FAIL, "Account $id not found")
            }
            response.status(200)
            JSend(SUCCESS, account.toDto())
        }, JsonTransformer())

    }
}