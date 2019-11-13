package br.com.alissonbolsoni.rabbitmqapplication.rabbitmq.amqp

import br.com.alissonbolsoni.rabbitmqapplication.rabbitmq.amqp.objects.AmqpConfig
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory

abstract class ConnectToRabbitMQ(val amqpConfig: AmqpConfig) {

    private var mChannel: Channel? = null
    private var mConnection: Connection? = null

    open fun dispose() {
        try {
            if (mConnection != null) mConnection!!.close()
        } catch (ex: Exception) {
        }
        try {
            if (mChannel != null) mChannel!!.abort()
        } catch (ex: Exception) {
        }
    }

    @Throws(Exception::class)
    open fun connectToRabbitMQ() {
        if (isAlreadyConnected())
            return

        val connectionFactory = ConnectionFactory()
        connectionFactory.host = amqpConfig.host
        connectionFactory.virtualHost = amqpConfig.virtualHost
        connectionFactory.isAutomaticRecoveryEnabled = true
        connectionFactory.username = amqpConfig.username
        connectionFactory.password = amqpConfig.password

        mConnection = connectionFactory.newConnection("Listen")

        mChannel = mConnection!!.createChannel()
        configure(mChannel!!)
    }

    fun ackMessage(deliveryTag: Long) {
        mChannel?.basicAck(deliveryTag, false)
    }

    fun ackMultiMessage(deliveryTag: Long) {
        mChannel?.basicAck(deliveryTag, true)
    }

    abstract fun configure(channel: Channel)

    private fun isAlreadyConnected() = mChannel != null && mChannel!!.isOpen
}
