package me.itishermann.ollamacommitsummarizer.settings

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.Nullable
import java.util.*
import javax.swing.JComponent


/**
 * Provides controller functionality for application settings.
 */
internal class OllamaSettingsConfigurable: Configurable {
    private lateinit var ollamaSettingsComponent: OllamaSettingsComponent

   fun preferredFocusedComponent(): JComponent {
       return ollamaSettingsComponent.preferredFocusedComponent
   }

    @Nullable
    override fun createComponent(): JComponent {
        ollamaSettingsComponent = OllamaSettingsComponent()
        return ollamaSettingsComponent.panel
    }

    override fun isModified(): Boolean {
        val state: OllamaSettingsState.State =
            Objects.requireNonNull(OllamaSettingsState.instance.state)
        return !ollamaSettingsComponent.serverUrl.equals(state.serverUrl) ||
                !ollamaSettingsComponent.userName.equals(state.userName) ||
                !ollamaSettingsComponent.password.equals(state.password) ||
                !ollamaSettingsComponent.modelName.equals(state.modelName) ||
                !ollamaSettingsComponent.prompt.equals(state.prompt)
    }

    override fun apply() {
        val state: OllamaSettingsState.State =
            Objects.requireNonNull(OllamaSettingsState.instance.state)
        state.serverUrl = ollamaSettingsComponent.serverUrl
        state.userName = ollamaSettingsComponent.userName
        state.password = ollamaSettingsComponent.password
        state.modelName = ollamaSettingsComponent.modelName
        state.prompt = ollamaSettingsComponent.prompt
    }

    override fun reset() {
        val state: OllamaSettingsState.State =
            Objects.requireNonNull(OllamaSettingsState.instance.state)
        ollamaSettingsComponent.serverUrl = state.serverUrl
        ollamaSettingsComponent.userName = state.userName
        ollamaSettingsComponent.password = state.password
        ollamaSettingsComponent.modelName = state.modelName
        ollamaSettingsComponent.prompt = state.prompt
    }

    override fun getDisplayName(): @Nls(capitalization = Nls.Capitalization.Title) String? {
        return "Ollama Commit Summarizer"
    }
}
