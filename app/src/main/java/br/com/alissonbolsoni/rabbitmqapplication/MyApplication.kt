package br.com.alissonbolsoni.rabbitmqapplication

import android.app.Application
import br.com.alissonbolsoni.rabbitmqapplication.di.amqpConfigModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MyApplication:Application() {

    override fun onCreate() {
        super.onCreate()
        configureDI()
    }

    private fun configureDI() {
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.INFO)
            androidContext(this@MyApplication)
            modules(
                listOf(
                    amqpConfigModule
                )
            )
        }
    }
}