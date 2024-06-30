package me.itishermann.ollamacommitsummarizer
import com.intellij.DynamicBundle
import com.intellij.ide.browsers.BrowserLauncher
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.net.URL

@NonNls
private const val BUNDLE = "properties.ui-text"

object UiTextBundle : DynamicBundle(BUNDLE) {
    val BUG_REPORT_URL = URL("https://github.com/itishermann/ollama-commit-summarizer/issues")

    @Suppress("SpreadOperator")
    @JvmStatic
    fun uiTextBundleProperty(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
        getMessage(key, *params)

    @Suppress("SpreadOperator", "unused")
    @JvmStatic
    fun messagePointer(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
        getLazyMessage(key, *params)

    fun openPluginSettings(project: Project) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, uiTextBundleProperty("settings.title"))
    }

    fun openGithubRepository() {
        BrowserLauncher.instance.open("https://github.com/itishermann/ollama-commit-summarizer");
    }

    fun openBugReportUrl() {
        BrowserLauncher.instance.open(BUG_REPORT_URL.toString());
    }

    fun plugin() = PluginManagerCore.getPlugin(PluginId.getId("me.itishermann.ollamacommitsummarizer"))
}
