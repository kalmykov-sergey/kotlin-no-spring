package name.sergei.kalmykov

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class AccountService {

    private val sequence = AtomicLong(0)
    private val storage = ConcurrentHashMap<Long, Account>()

    fun createAccount(initialAmount: Long): Account {
        val account = Account(
                id = sequence.incrementAndGet(),
                amount = initialAmount
        )
        storage[account.id] = account
        return account

    }

    fun getAccount(id: Long): Account? {
        return storage[id]
    }
}