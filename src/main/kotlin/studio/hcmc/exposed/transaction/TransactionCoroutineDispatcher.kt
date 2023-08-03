package studio.hcmc.exposed.transaction

import kotlinx.coroutines.*
import java.util.concurrent.*
import kotlin.coroutines.CoroutineContext

class TransactionCoroutineDispatcher private constructor(
    val corePoolSize: Int,
    val maximumPoolSize: Int,
    val keepAliveTime: Long,
    val unit: TimeUnit,
    val workQueue: BlockingQueue<Runnable>,
    val executorService: ExecutorService,
    val coroutineDispatcher: CoroutineDispatcher
) : CoroutineDispatcher() {
    data class Builder(
        var corePoolSize: Int = 0,
        var maximumPoolSize: Int = Runtime.getRuntime().availableProcessors(),
        var keepAliveTime: Long = 60L,
        var unit: TimeUnit = TimeUnit.SECONDS,
        var workQueue: BlockingQueue<Runnable> = SynchronousQueue(),
        var executorService: () -> ExecutorService = { ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue) },
        var coroutineDispatcher: (executorService: ExecutorService) -> CoroutineDispatcher = { it.asCoroutineDispatcher() }
    )

    companion object {
        var dispatcher: CoroutineDispatcher? = null
            set(value) {
                val _field = field
                if (_field != null && _field is ExecutorCoroutineDispatcher) {
                    _field.close()
                }

                field = value
            }

        operator fun invoke(config: Builder.() -> Unit): TransactionCoroutineDispatcher {
            val builder = Builder().apply(config)
            val executorService = builder.executorService()
            val coroutineDispatcher = builder.coroutineDispatcher(executorService)
            return TransactionCoroutineDispatcher(
                corePoolSize = builder.corePoolSize,
                maximumPoolSize = builder.maximumPoolSize,
                keepAliveTime = builder.keepAliveTime,
                unit = builder.unit,
                workQueue = builder.workQueue,
                executorService = executorService,
                coroutineDispatcher = coroutineDispatcher
            )
        }
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        coroutineDispatcher.dispatch(context, block)
    }
}

val Dispatchers.Transaction: CoroutineDispatcher get() = TransactionCoroutineDispatcher.dispatcher ?: IO