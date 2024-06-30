package me.itishermann.ollamacommitsummarizer.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "me.itishermann.ollamacommitsummarizer.settings.preferences",
    storages = [Storage("ollama-commit-summarizer-preferences.xml")]
)

internal class UserPreferences() : PersistentStateComponent<UserPreferences.State> {
    internal class State() {
        var shouldShowWelcomeNotification: Boolean = true
        var shouldShowOllamaConfigurationNotification: Boolean = true
        var shouldShowStarNotification: Boolean = true
    }

    private var myState: State = State()

    override fun getState(): State {
        return myState
    }

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        val instance: UserPreferences
            get() = ApplicationManager.getApplication()
                .getService(UserPreferences::class.java)
    }
}
