package me.itishermann.ollamacommitsummarizer

import io.github.amithkoujalgi.ollama4j.core.OllamaAPI

object OllamaClientManager {
    private lateinit var client: OllamaAPI

    fun getOllamaClient(): OllamaAPI {
        if(!::client.isInitialized) {
            throw IllegalStateException("Ollama client is not initialized")
        }
        return client
    }

    fun setOllamaClient(newClient: OllamaAPI) {
        client = newClient
    }
}
