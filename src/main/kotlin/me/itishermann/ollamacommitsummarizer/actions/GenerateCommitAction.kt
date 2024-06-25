package me.itishermann.ollamacommitsummarizer.actions

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import com.github.difflib.patch.Patch
import com.github.weisj.jsvg.e
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vcs.CommitMessageI
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.ui.Refreshable
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import io.github.amithkoujalgi.ollama4j.core.OllamaStreamHandler
import io.github.amithkoujalgi.ollama4j.core.utils.OptionsBuilder
import me.itishermann.ollamacommitsummarizer.exceptions.NoChangeToCommitException
import me.itishermann.ollamacommitsummarizer.services.OllamaService
import me.itishermann.ollamacommitsummarizer.settings.OllamaSettingsState
import org.jetbrains.annotations.NotNull
import java.util.*


class GenerateCommitAction: AnAction(), DumbAware {
    private var processing: Boolean = false

    override fun actionPerformed(event: AnActionEvent) {
        val commitPanel: CommitMessageI? = getVcsPanel(event)
        if (commitPanel == null || processing) {
            return
        }
        ProgressManager.getInstance().run(object : Task.Backgroundable(event.project, "Ollama commit summarizer", false) {
            override fun run(@NotNull indicator: ProgressIndicator) {
                indicator.text = "Generating commit message"
                processing = true
                handleEvent(event, commitPanel, indicator)
            }
        })
    }

    private fun handleEvent(e: AnActionEvent, commitPanel: CommitMessageI, indicator: ProgressIndicator) {
        indicator.text = "Building prompt"
        val project = checkNotNull(e.project)
        // get included changes
        val abstractCommitWorkflowHandler =
            e.dataContext.getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER) as AbstractCommitWorkflowHandler<*, *>?
                ?: return
        val includedChanges = abstractCommitWorkflowHandler.ui.getIncludedChanges()
        val baseDir = project.basePath
        try {
            val prompt = buildPrompt(includedChanges, baseDir!!)
            generateCommitMessage(prompt, commitPanel, indicator)
        } catch (e: NoChangeToCommitException) {
            Notifications.Bus.notify(
                Notification(
                    "me.itishermann.ollamacommitsummarizer.default",
                    "No changes to commit",
                    "There are no changes to commit, please include some changes to commit", NotificationType.INFORMATION
                )
            )
        }
    }

    private fun generateCommitMessage(prompt: String, commitPanel: CommitMessageI, indicator: ProgressIndicator) {
        indicator.text = "Preparing inference"
        val client = service<OllamaService>().getOllamaClient()
        val modelName = OllamaSettingsState.instance.state.modelName
        val streamHandler = OllamaStreamHandler { s: String? ->
            ApplicationManager.getApplication().invokeLater {
                indicator.text = "Streaming response"
                indicator.fraction += 0.05
                commitPanel.setCommitMessage(s)
            }
        }
        indicator.text = "Setting temperature and top-k"
        val options = OptionsBuilder().setTemperature(1.5f).setTopP(0.9f).setTopK(40).build()
        try {
            indicator.text = "Inferring"
            client.generate(modelName, prompt, options, streamHandler)
            processing = false
            Notifications.Bus.notify(
                Notification(
                    "me.itishermann.ollamacommitsummarizer.default",
                    "Commit message generated",
                    "The commit message has been generated successfully", NotificationType.INFORMATION
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Notifications.Bus.notify(
                Notification(
                    "me.itishermann.ollamacommitsummarizer.default",
                    "Ollama API error",
                    "An error occured while generating your commit message: ${e.localizedMessage ?: e.message}", NotificationType.ERROR
                )
            )
        } finally {
            processing = false
        }
    }

    private fun buildPrompt(includedChanges: List<Change>, baseDir: String): String {
        val totalUnifiedDiffs: MutableList<String> = ArrayList()
        if(includedChanges.isEmpty()) {
            throw NoChangeToCommitException("No changes to commit")
        }
        for (change in includedChanges) {
            val beforeRevision = change.beforeRevision
            val afterRevision = change.afterRevision
            var beforeContent: String? = ""
            var afterContent: String? = ""
            try {
                beforeContent = if (beforeRevision != null) beforeRevision.content else ""
                afterContent = if (afterRevision != null) afterRevision.content else ""
            } catch (e: VcsException) {
                e.printStackTrace()
            }
            val original = Arrays.stream(beforeContent!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()).toList()
            val revised = Arrays.stream(afterContent!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()).toList()
            val patch: Patch<String> = DiffUtils.diff(original, revised)
            val relativePath = change.virtualFile!!.path.replace(baseDir, "")
            val unifiedDiff: List<String> =
                UnifiedDiffUtils.generateUnifiedDiff(relativePath, relativePath, original, patch, 3)
            totalUnifiedDiffs.addAll(unifiedDiff)
        }
        var prompt = OllamaSettingsState.instance.state.prompt ?: throw IllegalStateException("Prompt is null")
        prompt = prompt.replace("{{gitDiff}}", java.lang.String.join("\n", totalUnifiedDiffs))
        prompt = prompt.replace("{{fileCount}}", includedChanges.size.toString())
        return prompt
    }

    private fun getVcsPanel(e: AnActionEvent?): CommitMessageI? {
        if (e == null) {
            return null
        }
        val data = Refreshable.PANEL_KEY.getData(e.dataContext)
        if (data is CommitMessageI) {
            return data
        }
        return VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(e.dataContext)
    }
}
