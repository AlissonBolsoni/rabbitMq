package br.com.alissonbolsoni.rabbitmqapplication.rabbitmq

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.util.Log
import br.com.alissonbolsoni.rabbitmqapplication.rabbitmq.amqp.ConsumerAndProducer
import br.com.alissonbolsoni.rabbitmqapplication.rabbitmq.amqp.OnReceiveMessageHandler
import br.com.alissonbolsoni.rabbitmqapplication.rabbitmq.amqp.objects.AmqpExchanges
import br.com.alissonbolsoni.rabbitmqapplication.rabbitmq.amqp.objects.AmqpConfig
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class RabbitMqService : Service() {

    companion object {
        private const val TAG = "RabbitMqService"
    }

    private val serviceJob by lazy { Job() }
    private val serviceScope by lazy { CoroutineScope(Dispatchers.IO + serviceJob) }
    private val handler by lazy {
        runBlocking(Dispatchers.Main) { Handler() }
    }

    private var consumerAndProducer: ConsumerAndProducer? = null
    private val lostMessages = LinkedHashMap<String, String>()
    private val amqpConfig: AmqpConfig by inject()
    lateinit var updateCallback: UpdateCallback

    override fun onBind(intent: Intent?) = RabbitMqService.RabbitMqBinder(this)

    override fun onCreate() {
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        consumerAndProducer?.dispose()
        consumerAndProducer = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (consumerAndProducer == null) {
            init()
        }

        return START_STICKY
    }

    private fun init() {
        Log.d(TAG, "INIT")
        serviceScope.launch {
            try {
                connectAmqp()
                sendLostMessages()
                bindToExchange()
            } catch (ex: Exception) {
                Log.e(TAG, "INIT EXEPTION", ex)
                consumerAndProducer?.dispose()
                handler.postDelayed(Runnable { init() }, 5_000)
            }
        }
    }

    private fun connectAmqp() {
        consumerAndProducer = ConsumerAndProducer(amqpConfig, createOnReceiveMessageHandler())
        consumerAndProducer!!.connectToRabbitMQ()
    }

    private fun createOnReceiveMessageHandler(): OnReceiveMessageHandler {
        return object : OnReceiveMessageHandler {
            override fun processMessage(exchange: String, body: ByteArray): Boolean {
                try {
                    updateCallback.updateView("$exchange -> ${String(body)}")
                    return true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return false
            }
        }
    }

    @Throws(Exception::class)
    private fun bindToExchange() {
        for (exchange in amqpConfig.listExchangeToReceive) {
            consumerAndProducer?.binding(exchange, amqpConfig.queueName())
        }
    }

    private fun sendToAmqp(amqpExchanges: AmqpExchanges, json: String) {
        sendLostMessages()
        sendToAmqp(amqpExchanges.exchange, json)
    }

    private fun sendToAmqp(exchange: String, json: String) = runBlocking(Dispatchers.IO) {
        try {
            consumerAndProducer?.publish(exchange, json)
        } catch (e: Exception) {
            Log.e(TAG, "Fail to publish message -> $json", e)
            lostMessages[exchange] = json
        }
    }

    private fun sendLostMessages() {
        if (lostMessages.isEmpty()) return
        lostMessages.forEach {
            sendToAmqp(it.key, it.value)
        }
    }

    class RabbitMqBinder(private val service: RabbitMqService): Binder() {
        fun setCallback(callback: UpdateCallback){
            service.updateCallback = callback
        }
        fun sendMessage(amqpExchanges: AmqpExchanges, json: String){
            service.sendToAmqp(amqpExchanges, json)
        }
    }
}
