package dsh.web.plugin;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefBrowser;
import org.jetbrains.annotations.NotNull;

/**
 * Hard-reloads the currently active tab of the DeepSeek Harness tool window
 * (the WebUI or the DeepSeek Chat tab), bypassing the browser cache, so
 * changes (e.g. after the DSH web server is rebuilt or restarted) show up
 * without restarting the IDE.
 *
 * Reachable via the tool-window header button or the gear menu. No keyboard
 * shortcut is bound by default, so no IDE keybinding is taken over; users can
 * still assign one themselves via Settings | Keymap (action dsh.web.reload).
 */
public final class ReloadPageAction extends AnAction {

    public ReloadPageAction() {
        super("Reload Page",
                "Reload the active DeepSeek Harness tab, bypassing the cache",
                AllIcons.Actions.Refresh);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        JBCefBrowser browser = DshWebToolWindowFactory.activeBrowser(project);
        if (browser != null) {
            browser.getCefBrowser().reloadIgnoreCache();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabled(project != null
                && DshWebToolWindowFactory.activeBrowser(project) != null);
    }
}
