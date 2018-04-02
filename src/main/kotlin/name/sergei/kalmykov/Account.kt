package name.sergei.kalmykov

data class Account(
        val id: Long,
        var amount: Long
) {
    data class Dto(
            val id: Long,
            val amount: Long
    )

    fun toDto() = Account.Dto(id = this.id, amount = this.amount)

}