# DeepSeek Harness for JetBrains IDE

[中文](README.md) | English

![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/33556.svg)](https://plugins.jetbrains.com/plugin/33556-deepseek-harness-webui)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/33556.svg)](https://plugins.jetbrains.com/plugin/33556-deepseek-harness-webui)

Docks the [DeepSeek Harness](https://github.com/deepseek-ai/deepseek-harness) Web UI into a JetBrains IDE tool window, rendered by the IDE's bundled Chromium (JCEF). The full WebUI — reasoning streams, tool-call trajectory, sessions, permission/preset switching — **lives entirely inside the IDE, hideable like the terminal — no browser tab needed**.

![01.png](/docs/screenshots/01.png)
![01.png](/docs/screenshots/02.png)

## Features

- **Web UI inside the IDE** — the DeepSeek Harness web front-end docked as a standard tool window (right side), with the same look and behavior as the browser version.
- **DeepSeek Chat pinned tab** — `chat.deepseek.com` is pinned as a permanent second tab next to the WebUI: always present, cannot be closed. No separate browser tab needed for the DeepSeek web chat.
- **Refresh without restarting the IDE** — restart or rebuild the `dsh` server, then click the reload button in the tool-window header (or gear menu → *Reload Page*) to hard-reload the **currently active tab** (WebUI or DeepSeek Chat), bypassing the browser cache. No IDE restart, ever.
- **Theme-aware icon** — the tool-window icon adapts automatically to the IDE's light/dark theme.

## Quick Start

### 1. Install DeepSeek Harness (with web support)

The plugin renders the official DeepSeek Harness Web UI, so `dsh` with web support must be installed. You only need [Node.js](https://nodejs.org/) (LTS):

```sh
npx @deepseek-ai/dsh web
```

`npx` downloads and runs the official `@deepseek-ai/dsh` package and starts the Web UI, served at `http://127.0.0.1:3080` by default. To run from a source checkout instead, follow the [official README](https://github.com/deepseek-ai/deepseek-harness#run).

> First run? Configure the Web UI once — open **Settings → Models** and add a DeepSeek API key, then choose a **workspace**. This can be done in the browser or directly inside the IDE tool window — both work.

### 2. Install the plugin

- **JetBrains Marketplace** (recommended): <a href="https://plugins.jetbrains.com/plugin/33556-deepseek-harness-webui">DeepSeek Harness WebUI</a> — **Settings | Plugins | Marketplace**.
- **From disk**: download `dsh-web-jetbrains-toolwindow.jar` from [GitHub Releases](https://github.com/Chocomintopia/dsh-jetbrains-plugin/releases), then **Settings | Plugins | ⚙ | Install Plugin from Disk…**.
- **From source**: see [Building](#building).

Requires a JetBrains IDE with bundled JCEF — platform build 262+ (2026.2+), e.g. Rider (verified), IntelliJ IDEA, PyCharm, WebStorm, and more. Installing an update needs one IDE restart, as usual for JetBrains plugins.

### 3. Start the web server in your terminal

```sh
npx @deepseek-ai/dsh web
```

Leave the terminal running — the server listens on `http://127.0.0.1:3080` by default (see [Configuration](#configuration)).

### 4. Open the tool window — and refresh

1. Open the **DeepSeek Harness** tool window on the right edge of the IDE.
2. The Web UI loads inside it — the same page you would see in a browser; next to it a **DeepSeek Chat** tab (`chat.deepseek.com`) is pinned (sign in on first use).
3. Whenever you restart or rebuild the `dsh` server, or the page looks stale: click the **↻ reload button in the tool-window header (top-right)**, or open the gear menu → **Reload Page**. The active tab hard-reloads (cache bypassed) — no IDE restart needed.

## What's inside

### Tool window (JCEF)

`DshWebToolWindowFactory` creates a `JBCefBrowser` pointed at the DSH Web UI and docks it into a standard tool window through the platform content manager; next to it, a **DeepSeek Chat** tab (`chat.deepseek.com`) is pinned and cannot be closed. The live browser is registered per project, so actions can reach it.

### Reload without restart

`ReloadPageAction` hard-reloads the page (`CefBrowser.reloadIgnoreCache`), which makes the "restart server → hit refresh → see the new build" loop instant. Entry points:

- the reload button in the tool-window header;
- *Reload Page* in the tool-window gear menu.

No keyboard shortcut is bound on purpose, so the plugin never hijacks existing keybindings; assign your own in **Settings | Keymap** (action id `dsh.web.reload`) if you like.

## Configuration

| Setting | Description |
|---|---|
| `-Ddsh.web.url` (JVM property) | Web UI address; default `http://127.0.0.1:3080`. Set it in **Help | Edit Custom VM Options…**, e.g. `-Ddsh.web.url=http://127.0.0.1:9000`, then restart the IDE once. |

## Building

No Gradle/Kotlin required — the plugin compiles against a JetBrains IDE's own jars and bundled JDK:

```powershell
.\build.ps1 -IdeRoot "D:\Program Files\JetBrains\Rider 2026.2"
```

Produces `dsh-web-jetbrains-toolwindow.jar` / `dsh-web-jetbrains-toolwindow.zip` in the `releases/` directory.

## License

[MIT](LICENSE) © DeepSeek Harness contributors
