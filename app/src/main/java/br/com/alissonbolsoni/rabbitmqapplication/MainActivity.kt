package br.com.alissonbolsoni.rabbitmqapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.core.content.ContextCompat
import br.com.alissonbolsoni.rabbitmqapplication.rabbitmq.RabbitMqService
import br.com.alissonbolsoni.rabbitmqapplication.rabbitmq.UpdateCallback
import br.com.alissonbolsoni.rabbitmqapplication.rabbitmq.amqp.objects.AmqpExchanges
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), UpdateCallback, ServiceConnection {

    private lateinit var rabbitServiceBinder: RabbitMqService.RabbitMqBinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ContextCompat.startForegroundService(
            this,
            Intent(this, RabbitMqService::class.java)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startService(Intent(this, RabbitMqService::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        bindService(Intent(this, RabbitMqService::class.java), this, Context.BIND_AUTO_CREATE)
    }

    override fun onPause() {
        super.onPause()
        unbindService(this)
    }

    private fun sendMessagesToRabbit() {
        rabbitServiceBinder.sendMessage(AmqpExchanges.EX_1, "message 1")
        rabbitServiceBinder.sendMessage(AmqpExchanges.EX_3, "message 2")
        rabbitServiceBinder.sendMessage(AmqpExchanges.EX_2, "message 3")
        rabbitServiceBinder.sendMessage(AmqpExchanges.EX_4, "message 4")
    }

    override fun updateView(message: String) {
        runOnUiThread {
            var text = messages.text.toString()
            text += "$message \n"
            messages.text = text
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {}

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (service is RabbitMqService.RabbitMqBinder) {
            rabbitServiceBinder = service
            rabbitServiceBinder.setCallback(this)

            sendMessagesToRabbit()
        }
    }
}
