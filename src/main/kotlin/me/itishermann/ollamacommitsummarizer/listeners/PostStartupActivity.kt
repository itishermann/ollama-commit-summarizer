package me.itishermann.ollamacommitsummarizer.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import me.itishermann.ollamacommitsummarizer.notifications.Notification
import me.itishermann.ollamacommitsummarizer.notifications.sendNotification
import me.itishermann.ollamacommitsummarizer.settings.UserPreferences

class PostStartupActivity: ProjectActivity {

    private fun showConfigurationNotification(project: Project) {
        val firstTime = UserPreferences.instance.state.shouldShowWelcomeNotification
        if (firstTime) {
            sendNotification(Notification.welcome(project), project)
            UserPreferences.instance.state.shouldShowWelcomeNotification = false
        }
    }

    override suspend fun execute(project: Project) {
        showConfigurationNotification(project)
    }
}
