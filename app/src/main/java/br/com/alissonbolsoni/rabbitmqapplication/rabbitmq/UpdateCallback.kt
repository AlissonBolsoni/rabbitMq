package br.com.alissonbolsoni.rabbitmqapplication.rabbitmq

interface UpdateCallback {
    fun updateView(message: String)
}