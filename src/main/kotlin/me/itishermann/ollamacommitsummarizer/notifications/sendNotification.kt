package me.itishermann.ollamacommitsummarizer.notifications

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project

private const val IMPORTANT_GROUP_ID = "me.itishermann.ollamacommitsummarizer.important"
private const val DEFAULT_GROUP_ID = "me.itishermann.ollamacommitsummarizer.default"

fun sendNotification(notification : Notification, project : Project? = null) {
    val groupId = when(notification.type) {
        Notification.Type.PERSISTENT -> IMPORTANT_GROUP_ID
        Notification.Type.EPHEMERAL -> DEFAULT_GROUP_ID
    }

    val notificationManager = NotificationGroupManager
        .getInstance()
        .getNotificationGroup(groupId)

    val intellijNotification = notificationManager.createNotification(
        notification.title ?: "",
        notification.message,
        NotificationType.INFORMATION
    )

    notification.actions.forEach { action ->
        intellijNotification.addAction(DumbAwareAction.create(action.title) {
            action.run() {
                intellijNotification.expire()
            }
        })
    }

    intellijNotification.notify(project)
}
