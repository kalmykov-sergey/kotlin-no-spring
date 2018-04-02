package name.sergei.kalmykov

import com.google.gson.Gson
import spark.Route
import spark.Spark

class TransactionController(private val transactionService: TransactionService) {

    private val gson = Gson()

    fun registerRoutes() {
        Spark.put("/transactions", jsonType, Route { request, response ->
            val dto = gson.fromJson(request.body(), Transaction.Dto::class.java)
            response.header("content-type", jsonType)
            try {
                val transaction = transactionService.transferMoney(dto)
                var status = JSend.Status.SUCCESS
                if (transaction.state == Transaction.State.SUCCESS) {
                    response.status(200)
                } else {
                    status = JSend.Status.FAIL
                    response.status(400)
                }

                JSend(status, transaction.toDto())
            } catch (ex: RuntimeException) {
                response.status(400)
                JSend(JSend.Status.FAIL, null)
            }
        }, JsonTransformer())

        Spark.get("/transactions/:id", jsonType, Route { request, response ->
            val id = request.params("id")
            response.header("content-type", jsonType)
            val dto = transactionService.getTransactionDto(id)
            if (dto == null) {
                response.status(404)
                return@Route JSend(JSend.Status.FAIL, "Transaction $id not found")
            }
            response.status(200)
            JSend(JSend.Status.SUCCESS, dto)
        }, JsonTransformer())

    }
}
