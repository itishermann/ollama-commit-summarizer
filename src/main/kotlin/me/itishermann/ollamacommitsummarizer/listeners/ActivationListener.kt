package me.itishermann.ollamacommitsummarizer.listeners

import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.wm.IdeFrame
import io.github.amithkoujalgi.ollama4j.core.OllamaAPI
import me.itishermann.ollamacommitsummarizer.exceptions.AiServiceUninitializedException
import me.itishermann.ollamacommitsummarizer.services.OllamaService
import me.itishermann.ollamacommitsummarizer.settings.OllamaSettingsState

internal class ActivationListener : ApplicationActivationListener {

    override fun applicationActivated(ideFrame: IdeFrame) {
        try {
            val client = service<OllamaService>().getOllamaClient()
            client.ping()
            thisLogger().info("API is reachable")
        } catch (e: AiServiceUninitializedException) {
            // initialize the service
            val serverUrl = OllamaSettingsState.instance.state.serverUrl
            val userName = OllamaSettingsState.instance.state.userName
            val password = OllamaSettingsState.instance.state.password
            val ollamaAPI = OllamaAPI(serverUrl)
            ollamaAPI.setRequestTimeoutSeconds(60*60) // 1 hour
            if(!userName.isNullOrEmpty() && !password.isNullOrEmpty()) {
                ollamaAPI.setBasicAuth(userName, password)
            }
            // Comment before deploying
            // ollamaAPI.setVerbose(true)
            val ollamaService = service<OllamaService>()
            ollamaService.setOllamaClient(ollamaAPI)
            val isOllamaServerReachable = ollamaAPI.ping()
            if(isOllamaServerReachable) {
                thisLogger().info("API is reachable and initialized successfully")
            } else {
                thisLogger().warn("API is not reachable")
            }
        } catch (e: Exception) {
            thisLogger().error("An error occurred: ${e.message}")
        }
    }
}
