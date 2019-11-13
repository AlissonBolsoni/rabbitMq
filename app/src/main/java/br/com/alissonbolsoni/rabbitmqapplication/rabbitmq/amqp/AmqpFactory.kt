package br.com.alissonbolsoni.rabbitmqapplication.rabbitmq.amqp

import br.com.alissonbolsoni.rabbitmqapplication.rabbitmq.amqp.objects.AmqpConfig

object AmqpFactory {

    fun create(
        listExchange: List<String>
    ) = AmqpConfig(
        Parameters.AMQP_HOST,
        Parameters.AMQP_PORT,
        Parameters.AMQP_USER,
        Parameters.AMQP_PASSWORD,
        Parameters.AMQP_VIRTUALHOST,
        listExchange
    )

}