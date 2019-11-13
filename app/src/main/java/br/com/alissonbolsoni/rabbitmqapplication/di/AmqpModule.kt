package br.com.alissonbolsoni.rabbitmqapplication.di

import br.com.alissonbolsoni.rabbitmqapplication.rabbitmq.amqp.AmqpFactory
import br.com.alissonbolsoni.rabbitmqapplication.rabbitmq.amqp.objects.AmqpExchanges
import br.com.alissonbolsoni.rabbitmqapplication.rabbitmq.amqp.objects.AmqpConfig
import org.koin.dsl.module

val amqpConfigModule = module {
    single {
        loadAmqpConfig()
    }
}

private fun loadAmqpConfig(
): AmqpConfig = AmqpFactory.create(
    listOf(
        AmqpExchanges.EX_1.exchange,
        AmqpExchanges.EX_2.exchange,
        AmqpExchanges.EX_3.exchange,
        AmqpExchanges.EX_4.exchange
    )
)