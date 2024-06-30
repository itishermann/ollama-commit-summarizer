package me.itishermann.ollamacommitsummarizer.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UI
import io.github.amithkoujalgi.ollama4j.core.OllamaAPI
import me.itishermann.ollamacommitsummarizer.services.OllamaService
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import javax.swing.*
/**
 * Supports creating and managing a [JPanel] for the Settings Dialog.
 */
class OllamaSettingsComponent {
    val panel: JPanel
    private val checkApiButton: JButton = JButton("Check API")
    private val refreshModelsButton: JButton = JButton("Refresh models")
    private val loadingLabel: JLabel = JLabel()
    private val loadingModelsLabel: JLabel = JLabel()
    private val reachabilityStatusLabel: JLabel = JLabel("Click 'Check API' to check reachability of the API.")
    private val serverUrlText = JBTextField()
    private val userNameText = JBTextField()
    private val passwordText = JBTextField()
    private val modelNameComboBox = ComboBox<String>()
    private val promptText = JBTextArea()

    init {
        checkApiButton.addActionListener {
            checkApiReachability()
        }
        refreshModelsButton.addActionListener {
            updateModelNames()
        }
        modelNameComboBox.isVisible = false
        modelNameComboBox.addItemListener { event ->
            if (event.stateChange == java.awt.event.ItemEvent.SELECTED) {
                val selectedItem = event.item.toString()
                modelName = selectedItem
            }
        }
        refreshModelsButton.isEnabled = false
        promptText.margin = JBUI.insets(5)
        val promptPanel: JPanel = UI.PanelFactory.panel(promptText).withComment("""
            <p>Plugin is based on <a href="https://ollama.ai/">Ollama</a></p>
            <p>The following variables are available for use in the prompt:</p>
            <ul>
                <li>{{gitDiff}}: The git diff of the changes</li>
                <li>{{fileCount}}: The number of files changed</li>
                <li>{{branchName}}: The name of the current branch, can be empty if there is no git branch</li>
            </ul>
        """.trimIndent())
            .createPanel()
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Server URL:"), serverUrlText, 1, false)
            .addLabeledComponent(JBLabel("Username:"), userNameText, 1, false)
            .addLabeledComponent(JBLabel("Password:"), passwordText, 1, false)
            .addLabeledComponent(JBLabel("Api reachability:"), reachabilityStatusLabel, 1, false)
            .addLabeledComponent(checkApiButton, loadingLabel, 1, false)
            .addLabeledComponent(refreshModelsButton, loadingModelsLabel, 1, false)
            .addLabeledComponent(JBLabel("Model:"), modelNameComboBox, 1, false)
            .addLabeledComponent(JBLabel("Prompt:"), promptPanel, 1, true)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    private fun checkApiReachability() {
        loadingLabel.text = "Checking API..."
        if(serverUrl.isNullOrEmpty()) {
            reachabilityStatusLabel.text = "API is not reachable, server URL is empty"
            loadingLabel.text = ""
            return
        }
        SwingUtilities.invokeLater {
            try {
                val ollamaAPI = OllamaAPI(serverUrl)
                ollamaAPI.setRequestTimeoutSeconds(60*60) // 1 hour
                if(!userName.isNullOrEmpty() && !password.isNullOrEmpty()) {
                    ollamaAPI.setBasicAuth(userName!!, password!!)
                }
                // Comment before deploying
                // ollamaAPI.setVerbose(true)
                val ollamaService = service<OllamaService>()
                ollamaService.setOllamaClient(ollamaAPI)
                val isOllamaServerReachable = ollamaAPI.ping()
                if (isOllamaServerReachable) {
                    reachabilityStatusLabel.text = "API is reachable"
                    refreshModelsButton.isEnabled = true
                    updateModelNames()
                } else {
                    reachabilityStatusLabel.text = "API is not reachable"
                    refreshModelsButton.isEnabled = false
                }
                loadingLabel.text = ""
            } catch (e: Exception){
                loadingLabel.text = "An error occurred: ${e.message}"
                reachabilityStatusLabel.text = "API is not reachable"
            }
        }
    }

    private fun fetchModelNames(): List<String>  {
        if(serverUrl.isNullOrEmpty()) {
            return emptyList<String>()
        }
        val ollamaService = service<OllamaService>()
        val ollamaAPI = ollamaService.getOllamaClient()
        try {
            val modelList = ollamaAPI.listModels()
            loadingModelsLabel.text = "Models fetched successfully"
            return modelList.map { it.name }
        } catch (e: Exception) {
            loadingModelsLabel.text = "Error fetching models: ${e.message}"
            return emptyList()
        }
    }

    private fun updateModelNames() {
        loadingModelsLabel.text = "Fetching models..."
        val modelNames = fetchModelNames()
        modelNameComboBox.removeAllItems()
        modelNames.forEach { modelNameComboBox.addItem(it) }
        // Set the first model as selected
        if(modelNames.isNotEmpty()) {
            modelNameComboBox.isVisible = true
            if(modelName.isNullOrEmpty()){
               modelName = modelNames[0]
            }
        } else {
            modelNameComboBox.isVisible = false
            loadingModelsLabel.text = "No models found"
        }
        loadingModelsLabel.text = "Models fetched successfully"
    }

    val preferredFocusedComponent: JComponent
        get() = serverUrlText

    @get:NotNull
    var serverUrl: String?
        get() = serverUrlText.text
        set(newText) {
            serverUrlText.text = newText
        }

    @get:Nullable
    var userName: String?
        get() = userNameText.text
        set(newText) {
            userNameText.text = newText
        }

    @get:Nullable
    var password: String?
        get() = passwordText.text
        set(newText) {
            passwordText.text = newText
        }

    @get:NotNull
    var modelName: String?
        get() = modelNameComboBox.item ?: ""
        set(newText) {
            modelNameComboBox.selectedItem = newText
        }

    @get:NotNull
    var prompt: String?
        get() = promptText.text
        set(newText) {
            promptText.text = newText
        }
}
