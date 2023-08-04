package studio.hcmc.exposed.transaction

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import studio.hcmc.kotlin.coroutines.ExecutorCoroutineDispatcherConfig
import studio.hcmc.kotlin.coroutines.createExecutorService
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

class TransactionCoroutineDispatcher private constructor(
    val executorCoroutineDispatcherConfig: ExecutorCoroutineDispatcherConfig,
    val executorService: ExecutorService,
    val coroutineDispatcher: CoroutineDispatcher
) : CoroutineDispatcher() {
    data class Builder(
        var executorCoroutineDispatcherConfig: ExecutorCoroutineDispatcherConfig.Builder.() -> Unit = {},
        var executorService: (config: ExecutorCoroutineDispatcherConfig) -> ExecutorService = { it.createExecutorService() },
        var coroutineDispatcher: (executorService: ExecutorService) -> CoroutineDispatcher = { it.asCoroutineDispatcher() }
    )

    companion object {
        private val transactionPoolNumber = AtomicInteger(1)
        private val defaultThreadFactory: () -> ThreadFactory = {
            object : ThreadFactory {
                private val poolNumber = transactionPoolNumber.getAndIncrement()
                private val group = Thread.currentThread().threadGroup
                private val threadNumber = AtomicInteger(1)

                override fun newThread(r: java.lang.Runnable): Thread {
                    val threadNumber = threadNumber.getAndIncrement()
                    return Thread(group, r, "transaction-$poolNumber-$threadNumber", 0)
                }
            }
        }

        private val defaultConfig = ExecutorCoroutineDispatcherConfig {
            val availableProcessors = Runtime.getRuntime().availableProcessors()
            corePoolSize = (availableProcessors shr 1) + 1
            maximumPoolSize = availableProcessors
            workQueue = LinkedBlockingQueue()
            threadFactory = defaultThreadFactory()
        }

        val logger by lazy { LoggerFactory.getLogger(TransactionCoroutineDispatcher::class.java) }

        var dispatcher: CoroutineDispatcher? = null
            set(value) {
                val _field = field
                if (_field != null && _field is ExecutorCoroutineDispatcher) {
                    _field.close()
                }

                field = value
            }

        operator fun invoke(configuration: Builder.() -> Unit = {}): TransactionCoroutineDispatcher {
            val builder = Builder().apply(configuration)
            val executorCoroutineDispatcherConfig = ExecutorCoroutineDispatcherConfig(defaultConfig, builder.executorCoroutineDispatcherConfig)
            val executorService = builder.executorService(executorCoroutineDispatcherConfig)
            val coroutineDispatcher = builder.coroutineDispatcher(executorService)
            return TransactionCoroutineDispatcher(
                executorCoroutineDispatcherConfig = executorCoroutineDispatcherConfig,
                executorService = executorService,
                coroutineDispatcher = coroutineDispatcher
            )
        }
    }

    init {
        logger.info("Initialized with config: ${executorCoroutineDispatcherConfig.description}")
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        coroutineDispatcher.dispatch(context, block)
    }
}

val Dispatchers.Transaction: CoroutineDispatcher get() = TransactionCoroutineDispatcher.dispatcher ?: IO