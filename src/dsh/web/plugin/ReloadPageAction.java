package dsh.web.plugin;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.jcef.JBCefBrowser;
import org.jetbrains.annotations.NotNull;

/**
 * Hard-reloads the DeepSeek Harness Web UI inside its tool window, bypassing
 * the browser cache, so changes (e.g. after the DSH web server is rebuilt or
 * restarted) show up without restarting the IDE.
 *
 * Reachable via the tool-window header button or the gear menu. No keyboard
 * shortcut is bound by default, so no IDE keybinding is taken over; users can
 * still assign one themselves via Settings | Keymap (action dsh.web.reload).
 */
public final class ReloadPageAction extends AnAction {

    public ReloadPageAction() {
        super("Reload Page",
                "Reload the DeepSeek Harness Web UI, bypassing the cache",
                AllIcons.Actions.Refresh);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        JBCefBrowser browser = DshWebToolWindowFactory.browserFor(e.getProject());
        if (browser != null) {
            browser.getCefBrowser().reloadIgnoreCache();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(DshWebToolWindowFactory.browserFor(e.getProject()) != null);
    }
}
