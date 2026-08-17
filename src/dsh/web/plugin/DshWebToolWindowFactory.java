package dsh.web.plugin;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
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
 * A second tab, "DeepSeek Chat" (chat.deepseek.com), is pinned next to the
 * WebUI tab: it is always present and cannot be closed. The reload button
 * always reloads the currently active tab.
 *
 * Reloading without an IDE restart: the tool-window header shows a reload
 * button and the gear menu offers "Reload Page" (hard reload, cache bypassed) -
 * e.g. after rebuilding/restarting the DSH web server. No keyboard shortcut
 * is bound by default, so no IDE keybinding is taken over.
 */
public final class DshWebToolWindowFactory implements ToolWindowFactory {

    /** Tool window id, as declared in plugin.xml. */
    public static final String TOOL_WINDOW_ID = "DeepSeek Harness";

    /** System property overriding the Web UI address. */
    public static final String URL_PROPERTY = "dsh.web.url";

    /** Default Web UI address (the local DSH web server). */
    public static final String DEFAULT_URL = "http://127.0.0.1:3080";

    /** Address of the DeepSeek web chat, pinned as the second tab. */
    public static final String CHAT_URL = "https://chat.deepseek.com/";

    /** Display name of the pinned chat tab. */
    public static final String CHAT_TAB_NAME = "DeepSeek Chat";

    /** Project -> live WebUI browser, so actions (reload, ...) can reach the page. */
    private static final Map<Project, JBCefBrowser> BROWSERS = new ConcurrentHashMap<>();

    /** Project -> live chat (chat.deepseek.com) browser of the pinned tab. */
    private static final Map<Project, JBCefBrowser> CHAT_BROWSERS = new ConcurrentHashMap<>();

    /** Returns the live WebUI browser of the given project's tool window, if any. */
    @Nullable
    public static JBCefBrowser browserFor(@Nullable Project project) {
        return project == null ? null : BROWSERS.get(project);
    }

    /**
     * Returns the browser of the currently selected tab of the tool window
     * (the chat tab if it is selected, otherwise the WebUI tab), if any.
     */
    @Nullable
    public static JBCefBrowser activeBrowser(@NotNull Project project) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
        if (toolWindow != null) {
            Content selected = toolWindow.getContentManager().getSelectedContent();
            JBCefBrowser chat = CHAT_BROWSERS.get(project);
            if (selected != null && chat != null && selected.getComponent() == chat.getComponent()) {
                return chat;
            }
        }
        return BROWSERS.get(project);
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

        // Pinned chat tab: always present next to the WebUI, not closeable.
        JBCefBrowser chat = new JBCefBrowser(CHAT_URL);
        CHAT_BROWSERS.put(project, chat);
        Disposer.register(project, () -> CHAT_BROWSERS.remove(project));
        Content chatContent = ContentFactory.getInstance()
                .createContent(chat.getComponent(), CHAT_TAB_NAME, false);
        chatContent.setCloseable(false);
        toolWindow.getContentManager().addContent(chatContent);

        // Header button + gear menu: reload the active tab.
        ReloadPageAction reload = new ReloadPageAction();
        toolWindow.setTitleActions(List.of(reload));
        toolWindow.setAdditionalGearActions(new DefaultActionGroup(reload));
    }
}
