package me.itishermann.ollamacommitsummarizer.interfaces

interface AiServiceInterface {

    fun isServerReachable(): Boolean

    fun ask(prompt: String, model: String, temperature: Float = 1.5f, topP: Float = 0.9f, topK: Int = 40): String
}
