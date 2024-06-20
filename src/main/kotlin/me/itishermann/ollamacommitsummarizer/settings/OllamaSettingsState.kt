package me.itishermann.ollamacommitsummarizer.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable


@State(
    name = "me.itishermann.ollamacommitsummarizer.settings.OllamaSettings",
    storages = [Storage("ollama-commit-summarizer-settings.xml")]
)
internal class OllamaSettingsState
    () : PersistentStateComponent<OllamaSettingsState.State> {
    internal class State() {
        var serverUrl: @NotNull @NonNls String? = "http://localhost:11434"
        var userName: @Nullable @NonNls String? = null
        var password: @Nullable @NonNls String? = null
        var modelName: @NotNull @NonNls String? = "codegemma:2b"
        var prompt: @NotNull @NonNls String? = """
            It is the code changes gives by unified view, changed file number is {{fileChangeCount}}:
            {{gitDiff}}
            Please generate commit message with template:
                        
            [Feature/Bugfix]: A brief summary of the changes in this commit (max 50 characters)
                        
            Detailed description of the changes:
            - Description of change #1 (max. 72 characters per line, no period at the end)
            - Description of change #2 (max. 72 characters per line, no period at the end)
            - ... and so on for as many changes as necessary
            """.trimIndent()
    }

    private var myState: State = State()

    override fun getState(): State {
        return myState
    }

    override fun loadState(@NotNull state: State) {
        myState = state
    }

    companion object {
        val instance: OllamaSettingsState
            get() = ApplicationManager.getApplication()
                .getService(OllamaSettingsState::class.java)
    }
}
