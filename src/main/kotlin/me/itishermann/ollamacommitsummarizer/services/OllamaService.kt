package me.itishermann.ollamacommitsummarizer.services

import com.intellij.openapi.components.Service
import io.github.amithkoujalgi.ollama4j.core.OllamaAPI
import io.github.amithkoujalgi.ollama4j.core.utils.Options
import io.github.amithkoujalgi.ollama4j.core.utils.OptionsBuilder
import me.itishermann.ollamacommitsummarizer.exceptions.AiServiceUninitializedException
import me.itishermann.ollamacommitsummarizer.interfaces.AiServiceInterface

@Service
internal class OllamaService: AiServiceInterface {
    private lateinit var client: OllamaAPI

    fun getOllamaClient(): OllamaAPI {
        if(!::client.isInitialized) {
            throw AiServiceUninitializedException("Ollama client is not initialized")
        }
        return client
    }

    fun setOllamaClient(newClient: OllamaAPI) {
        client = newClient
    }

    override fun isServerReachable(): Boolean {
        if(!::client.isInitialized) {
            throw AiServiceUninitializedException("Ollama client is not initialized")
        }
        return client.ping()
    }

    override fun ask(prompt: String, model: String, temperature: Float, topP: Float, topK: Int,): String {
        val options: Options =
            OptionsBuilder()
                .setNumGpu(2)
                .setTemperature(temperature)
                .setTopK(topK)
                .setTopP(topP)
                .build()
        val result = client.generate(prompt, model, options)
        return result.response
    }
}
