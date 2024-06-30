package me.itishermann.ollamacommitsummarizer.notifications

import com.intellij.ide.browsers.BrowserLauncher
import com.intellij.openapi.project.Project
import me.itishermann.ollamacommitsummarizer.UiTextBundle.uiTextBundleProperty
import me.itishermann.ollamacommitsummarizer.UiTextBundle.openGithubRepository
import me.itishermann.ollamacommitsummarizer.UiTextBundle.openPluginSettings

import java.net.URI

data class Notification(
    val title: String? = null,
    val message: String,
    val actions: Set<NotificationAction> = setOf(),
    val type: Type = Type.EPHEMERAL
) {
    enum class Type {
        PERSISTENT,
        EPHEMERAL
    }

    companion object {
        private val DEFAULT_TITLE = uiTextBundleProperty("notifications.title")

        fun welcome(project: Project?) = Notification(
            message = uiTextBundleProperty("notifications.welcome"),
            type = Type.EPHEMERAL,
            actions = setOf(
                NotificationAction.settings(project!!, uiTextBundleProperty("actions.configure-plugin")),
                NotificationAction.doNotAskAgain {}
            )
        )

        fun star() = Notification(
            message = """
                Finding Ollama Commit Summarizer useful? Show your support 💖 and ⭐ the repository 🙏.
            """.trimIndent(),
            actions = setOf(
                NotificationAction.openRepository {},
                NotificationAction.doNotAskAgain {}
            )
        )

        fun noCommitMessageField() = Notification(DEFAULT_TITLE, message = uiTextBundleProperty("notifications.no-field"))

        fun emptyDiff() = Notification(DEFAULT_TITLE, message = uiTextBundleProperty("notifications.empty-diff"))

        fun promptTooLarge() = Notification(DEFAULT_TITLE, message = uiTextBundleProperty("notifications.prompt-too-large"))

        fun unsuccessfulRequest(bundleProperty: String) = Notification(
            message = uiTextBundleProperty("notifications.unsuccessful-request", bundleProperty)
        )

        fun noCommitMessage(): Notification = Notification(message = uiTextBundleProperty("notifications.no-commit-message"))

        fun unableToSaveCredentials() = Notification(message = uiTextBundleProperty("notifications.unable-to-save-credentials"))

        fun usedPrompt(diff: String) = Notification (
            message = uiTextBundleProperty("notifications.uses-prompt", diff)
        )
    }

    fun isEphemeral() = type == Type.EPHEMERAL

    fun isPersistent() = !isEphemeral()
}

data class NotificationAction(val title: String, val run: (dismiss: () -> Unit) -> Unit) {
    companion object {
        fun settings(project: Project, title: String = uiTextBundleProperty("settings.title")) = NotificationAction(title) { dismiss ->
            dismiss()
            openPluginSettings(project)
        }

        fun openRepository(onComplete: () -> Unit) = NotificationAction(uiTextBundleProperty("actions.sure-take-me-there")) { dismiss ->
            openGithubRepository()
            dismiss()
            onComplete()
        }

        fun doNotAskAgain(onComplete: () -> Unit) = NotificationAction(uiTextBundleProperty("actions.do-not-ask-again")) { dismiss ->
            dismiss()
            onComplete()
        }

        fun openUrl(url: URI, title: String = uiTextBundleProperty("actions.take-me-there")) = NotificationAction(title) { dismiss ->
            dismiss()
            BrowserLauncher.instance.open(url.toString())
        }
    }
}
