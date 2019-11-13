package br.com.alissonbolsoni.rabbitmqapplication.rabbitmq.amqp

interface OnReceiveMessageHandler {
    fun processMessage(exchange: String, body: ByteArray): Boolean
}