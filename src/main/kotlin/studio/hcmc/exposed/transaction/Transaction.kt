package studio.hcmc.exposed.transaction

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import kotlin.coroutines.CoroutineContext

fun <R> blockingTransaction(
    transaction: Transaction? = null,
    block: Transaction.() -> R
): R {
    return if (transaction == null) {
        transaction { block() }
    } else {
        transaction(transaction.transactionIsolation, transaction.readOnly, transaction.db) { transaction.block() }
    }
}

/**
 * 트랜잭션 수행
 * @param transaction 부모 [Transaction]
 * @param context 이 트랜잭션을 수행할 대상 [CoroutineContext]
 * @param block 트랜잭션에서 수행할 내용
 */
suspend fun <R> suspendedTransaction(
    transaction: Transaction? = null,
    context: CoroutineContext? = Dispatchers.Transaction,
    block: suspend Transaction.() -> R
): R {
    return if (transaction == null) {
        newSuspendedTransaction(context) { block() }
    } else {
        if (context != null) {
            withContext(context) { transaction.block() }
        } else {
            transaction.block()
        }
    }
}