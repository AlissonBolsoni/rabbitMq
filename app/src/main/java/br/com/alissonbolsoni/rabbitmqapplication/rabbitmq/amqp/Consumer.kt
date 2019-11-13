package br.com.alissonbolsoni.rabbitmqapplication.rabbitmq.amqp

import br.com.alissonbolsoni.rabbitmqapplication.rabbitmq.amqp.objects.AmqpConfig
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import java.util.concurrent.TimeUnit

class Consumer(
    config: AmqpConfig,
    private val mOnReceiveMessageHandler: OnReceiveMessageHandler
) : ConnectToRabbitMQ(config) {

    companion object {
        internal const val FANOUT = "fanout"
    }

    //The Queue name for this consumer
    private var mQueue: String? = null
    private var mDelivery = DeliverCallback { _, delivery ->
        val processMessage =
            mOnReceiveMessageHandler.processMessage(delivery.envelope.exchange, delivery.body)

        if (processMessage)
            ackMessage(delivery.envelope.deliveryTag)
    }

    private var mCancel = CancelCallback {}

    override fun configure(channel: Channel) {
        mQueue = amqpConfig.queueName()
        channel.queueDeclare(mQueue, true, false, false, null)
        channel.basicConsume(mQueue, false, mDelivery, mCancel)
    }

    private fun setupConnectionFactory(): ConnectionFactory {
        val factory = ConnectionFactory()
        factory.isAutomaticRecoveryEnabled = true
        factory.networkRecoveryInterval = 5000
        factory.host = amqpConfig.host
        factory.port = amqpConfig.port
        factory.virtualHost = amqpConfig.virtualHost
        factory.username = amqpConfig.username
        factory.password = amqpConfig.password
        return factory
    }

    @Throws(Exception::class)
    fun publish(exchange: String, message: String) {
        val factory = setupConnectionFactory()
        val connection = factory.newConnection("Send")
        val channel = connection.createChannel()

        channel.exchangeDeclare(exchange, FANOUT, true, false, null)
        channel.basicPublish(exchange, "", null, message.toByteArray())
        channel.close()
        connection.close()
    }

    @Throws(Exception::class)
    fun binding(exchange: String, queue: String) {
        val factory = setupConnectionFactory()
        val connection =
            factory.newConnection("Bind")
        val channel = connection.createChannel()

        channel.exchangeDeclare(
            exchange,
            FANOUT, true, false, null
        )
        channel!!.queueBind(queue, exchange, "")
        channel.close()
        connection.close()
    }
}

