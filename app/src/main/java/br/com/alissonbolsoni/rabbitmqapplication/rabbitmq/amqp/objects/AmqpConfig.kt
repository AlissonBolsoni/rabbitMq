package br.com.alissonbolsoni.rabbitmqapplication.rabbitmq.amqp.objects

class AmqpConfig internal constructor(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val virtualHost: String,
    val listExchangeToReceive: List<String>) {

    fun queueName() = "QUEUE_NAME"
}
