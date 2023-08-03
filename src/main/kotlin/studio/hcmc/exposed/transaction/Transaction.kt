package studio.hcmc.exposed.transaction

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.coroutines.CoroutineContext

fun <R> blockingTransaction(
    transaction: Transaction? = null,
    block: Transaction.() -> R
): R {
    return if (transaction == null) {
        org.jetbrains.exposed.sql.transactions.transaction { block() }
    } else {
        transaction.block()
    }
}

suspend fun <R> suspendedTransaction(
    transaction: Transaction? = null,
    context: CoroutineContext? = Dispatchers.Transaction,
    block: suspend Transaction.() -> R
): R {
    return if (transaction == null) {
        newSuspendedTransaction(context) { block() }
    } else {
        transaction.block()
    }
}