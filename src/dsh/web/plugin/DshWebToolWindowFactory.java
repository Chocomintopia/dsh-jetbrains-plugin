package dsh.web.plugin;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.jcef.JBCefBrowser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Docks the DeepSeek Harness Web UI into a standard IDE tool window using the
 * bundled JCEF (Chromium) browser, so the full WebUI - reasoning streams,
 * tool-call trajectory, sessions, permission/preset switching - lives inside
 * the IDE and can be hidden/shown like any other tool window (terminal, etc).
 *
 * The URL is configurable via the JVM system property {@code dsh.web.url}
 * (default {@code http://127.0.0.1:3080}); set it in
 * Help | Edit Custom VM Options, e.g. {@code -Ddsh.web.url=http://127.0.0.1:3080}.
 *
 * Reloading without an IDE restart: the tool-window header shows a reload
 * button and the gear menu offers "Reload Page" (hard reload, cache bypassed) -
 * e.g. after rebuilding/restarting the DSH web server. No keyboard shortcut
 * is bound by default, so no IDE keybinding is taken over.
 */
public final class DshWebToolWindowFactory implements ToolWindowFactory {

    /** System property overriding the Web UI address. */
    public static final String URL_PROPERTY = "dsh.web.url";

    /** Default Web UI address (the local DSH web server). */
    public static final String DEFAULT_URL = "http://127.0.0.1:3080";

    /** Project -> live browser, so actions (reload, ...) can reach the page. */
    private static final Map<Project, JBCefBrowser> BROWSERS = new ConcurrentHashMap<>();

    /** Returns the live browser of the given project's tool window, if any. */
    @Nullable
    public static JBCefBrowser browserFor(@Nullable Project project) {
        return project == null ? null : BROWSERS.get(project);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        String url = System.getProperty(URL_PROPERTY, DEFAULT_URL);
        JBCefBrowser browser = new JBCefBrowser(url);
        BROWSERS.put(project, browser);
        Disposer.register(project, () -> BROWSERS.remove(project, browser));

        Content content = ContentFactory.getInstance()
                .createContent(browser.getComponent(), "DeepSeek Harness", false);
        toolWindow.getContentManager().addContent(content);

        // Reload entry points: header button, gear menu, global shortcut.
        ReloadPageAction reload = new ReloadPageAction();
        toolWindow.setTitleActions(List.of(reload));
        toolWindow.setAdditionalGearActions(new DefaultActionGroup(reload));
    }
}
