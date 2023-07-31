package studio.hcmc.exposed.transaction

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

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
    block: suspend Transaction.() -> R
): R {
    return if (transaction == null) {
        newSuspendedTransaction { block() }
    } else {
        transaction.block()
    }
}