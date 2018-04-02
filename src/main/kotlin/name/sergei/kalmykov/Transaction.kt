package name.sergei.kalmykov

data class Transaction(
        val fromAccount: Account,
        val toAccount: Account,
        val externalUniqueId: String,
        val amount: Long,
        var state: State
) {

    data class Dto(
            val id: String,
            val amount: Long,
            val fromAccountId: Long,
            val toAccountId: Long,
            val state: State)

    enum class State {
        SUCCESS, INSUFFICIENT_MONEY, NEW
    }

    fun toDto() = Transaction.Dto(
            id = this.externalUniqueId,
            amount = this.amount,
            fromAccountId = fromAccount.id,
            toAccountId = toAccount.id,
            state = state)
}
