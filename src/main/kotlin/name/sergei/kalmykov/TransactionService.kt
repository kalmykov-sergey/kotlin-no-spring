package name.sergei.kalmykov

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class TransactionService(private val accountService: AccountService, private val debug: Boolean = false) {

    private val storage = ConcurrentHashMap<String, Transaction>()
    private val rnd = Random()

    fun transferMoney(dto: Transaction.Dto): Transaction {
        val existingTransaction = getTransaction(dto.id)
        if (existingTransaction != null) {
            return existingTransaction
        }

        val fromAccount = accountService.getAccount(dto.fromAccountId) ?: throw NoAccountException(dto.fromAccountId)
        val toAccount = accountService.getAccount(dto.toAccountId) ?: throw NoAccountException(dto.toAccountId)
        val transaction = Transaction(fromAccount, toAccount, dto.id, dto.amount, Transaction.State.NEW)
        val savedTransaction = storage.putIfAbsent(dto.id, transaction)
        if (savedTransaction != null) {
            return savedTransaction;
        }
        doTransaction(transaction)
        return transaction
    }

    private fun doTransaction(transaction: Transaction) {
        val ordered = listOf(transaction.fromAccount, transaction.toAccount).sortedBy { it.id }

        synchronized(transaction) {
            synchronized(ordered[0]) {
                synchronized(ordered[1]) {
                    if (transaction.fromAccount.amount < transaction.amount) {
                        transaction.state = Transaction.State.INSUFFICIENT_MONEY
                        return
                    }
                    transaction.fromAccount.amount -= transaction.amount
                    if (debug) TimeUnit.MILLISECONDS.sleep(rnd.nextInt(10).toLong())
                    transaction.toAccount.amount += transaction.amount
                    transaction.state = Transaction.State.SUCCESS
                }
            }
        }
    }

    private fun getTransaction(id: String) = storage[id]

    fun getTransactionDto(id: String) = getTransaction(id)?.toDto()
}
