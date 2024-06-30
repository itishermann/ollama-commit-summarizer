package me.itishermann.ollamacommitsummarizer.actions

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import com.github.difflib.patch.Patch
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
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
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import me.itishermann.ollamacommitsummarizer.notifications.Notification
import me.itishermann.ollamacommitsummarizer.notifications.sendNotification
import me.itishermann.ollamacommitsummarizer.settings.UserPreferences
import java.util.*

class GenerateCommitAction: AnAction(), DumbAware {
    private var processing: Boolean = false

    override fun actionPerformed(event: AnActionEvent) {
        val commitPanel: CommitMessageI? = getVcsPanel(event)
        if (commitPanel == null || processing) {
            return
        }
        ProgressManager.getInstance().run(object : Task.Backgroundable(event.project, "Generating commit message", false) {
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
        val currentBranch = getCurrentBranchName(project)
        try {
            val prompt = buildPrompt(includedChanges, baseDir, currentBranch)
            generateCommitMessage(prompt, commitPanel, indicator)
        } catch (e: NoChangeToCommitException) {
            processing = false
            sendNotification(Notification.emptyDiff())
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
            val canShowNotification = UserPreferences.instance.state.shouldShowCommitMessageSuccessGeneration
            if(canShowNotification) sendNotification(Notification.successfulGeneration())
        } catch (e: Exception) {
            e.printStackTrace()
            sendNotification(Notification.unsuccessfulRequest(e.localizedMessage ?: e.message))
        } finally {
            processing = false
        }
    }

    private fun buildPrompt(includedChanges: List<Change>, baseDir: String?, branchName: String?): String {
        val totalUnifiedDiffs: MutableList<String> = ArrayList()
        if(includedChanges.isEmpty()) {
            processing = false
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
            val relativePath = change.virtualFile!!.path.replace(baseDir?:"", "")
            val unifiedDiff: List<String> =
                UnifiedDiffUtils.generateUnifiedDiff(relativePath, relativePath, original, patch, 3)
            totalUnifiedDiffs.addAll(unifiedDiff)
        }
        var prompt = OllamaSettingsState.instance.state.prompt ?: throw IllegalStateException("Prompt is null")
        prompt = prompt.replace("{{gitDiff}}", java.lang.String.join("\n", totalUnifiedDiffs))
        prompt = prompt.replace("{{fileCount}}", includedChanges.size.toString())
        prompt = prompt.replace("{{branchName}}", branchName?:"")
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

    private fun getCurrentBranchName(project: Project): String? {
        // Get the GitRepositoryManager for the project
        val repositoryManager = GitRepositoryManager.getInstance(project)
        // Get the list of repositories in the project
        val repositories = repositoryManager.repositories
        // If there are repositories available, get the current branch of the first repository
        return if (repositories.isNotEmpty()) {
            // TODO: Handle multiple repositories
            val repository: GitRepository = repositories[0]
            repository.currentBranch?.name
        } else {
            null
        }
    }
}
