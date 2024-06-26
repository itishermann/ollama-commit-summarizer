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
        var modelName: @NotNull @NonNls String? = "mistral:7b"
        var prompt: @NotNull @NonNls String? = """
            From the following git diff, generate a commit message respecting the Conventional Commits specification.
            Your response strictly have to be only the commit message.
            Here is the git diff: {{gitDiff}}
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
