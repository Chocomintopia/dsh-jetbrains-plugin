# DeepSeek Harness for JetBrains IDE

中文 | [English](README.en.md)

![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/33556.svg)](https://plugins.jetbrains.com/plugin/33556-deepseek-harness-webui)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/33556.svg)](https://plugins.jetbrains.com/plugin/33556-deepseek-harness-webui)

将 [DeepSeek Harness](https://github.com/deepseek-ai/deepseek-harness) 的 Web UI 嵌入 JetBrains IDE 的工具窗口，由 IDE 自带的 Chromium（JCEF）渲染。完整的 WebUI —— 推理流、工具调用轨迹、会话、权限/预设切换 —— **全部在 IDE 内部，像终端一样可以随时隐藏/显示，不需要再开一个浏览器标签页**。

![01.png](/docs/screenshots/01.png)

## 功能特性

- **Web UI 内嵌 IDE** —— DeepSeek Harness 的 Web 前端以标准工具窗口的形式停靠在右侧，与浏览器版本观感、行为完全一致。
- **无需重启 IDE 即可刷新** —— 重启或重建 `dsh` 服务后，点击工具窗口标题栏的刷新按钮（或齿轮菜单 → *Reload Page*）即可硬刷新页面，绕过浏览器缓存。再也不需要重启 IDE。
- **图标随主题切换** —— 工具窗口图标自动适配 IDE 的深色/浅色主题。

## 快速开始

### 1. 安装 DeepSeek Harness（支持 web 功能）

插件渲染的是官方 DeepSeek Harness Web UI，因此需要安装带 web 支持的 `dsh`。只需要 [Node.js](https://nodejs.org/)（LTS）：

```sh
npx @deepseek-ai/dsh web
```

`npx` 会自动下载并运行官方 `@deepseek-ai/dsh` 包，启动 Web UI，默认服务地址为 `http://127.0.0.1:3080`。如果要从源码运行，请参照[官方 README](https://github.com/deepseek-ai/deepseek-harness#run)。

> 首次使用？请先配置一次 Web UI —— 打开 **Settings → Models** 添加 DeepSeek API key，然后选择 **workspace**。在浏览器或 IDE 工具窗口里配置都可以。

### 2. 安装插件

- **JetBrains Marketplace**（推荐）：<a href="https://plugins.jetbrains.com/plugin/33556-deepseek-harness-webui">DeepSeek Harness WebUI</a> —— **Settings | Plugins | Marketplace**。
- **磁盘安装**：从 [GitHub Releases](https://github.com/Chocomintopia/dsh-jetbrains-plugin/releases) 下载 `dsh-web-jetbrains-toolwindow.jar`，然后 **Settings | Plugins | ⚙ | Install Plugin from Disk…**。
- **源码构建**：见[构建](#构建)。

要求：带 JCEF 的 JetBrains IDE —— 平台 build 262+（2026.2+），如 Rider（已验证）、IntelliJ IDEA、PyCharm（已验证）、WebStorm 等。安装更新需要重启一次 IDE（JetBrains 插件惯例）。

### 3. 在终端启动 web 服务

```sh
npx @deepseek-ai/dsh web
```

终端保持运行 —— 服务默认监听 `http://127.0.0.1:3080`（见[配置](#配置)）。

### 4. 打开工具窗口并刷新

1. 在 IDE 右侧边缘打开 **DeepSeek Harness** 工具窗口。
2. Web UI 会加载进来 —— 与浏览器里看到的是同一个页面。
3. 每当重启/重建 `dsh` 服务，或页面看起来过期时：点击**工具窗口标题栏右上角的 ↻ 刷新按钮**，或打开齿轮菜单 → **Reload Page**。页面会硬刷新（绕过缓存）——无需重启 IDE。

## 项目内容

### 工具窗口（JCEF）

`DshWebToolWindowFactory` 创建指向 DSH Web UI 的 `JBCefBrowser`，并通过平台 content manager 将其停靠为标准工具窗口。每个项目的活动浏览器会被注册下来，方便动作（action）访问。

### 无需重启的刷新

`ReloadPageAction` 硬刷新页面（`CefBrowser.reloadIgnoreCache`），让「重启服务 → 点刷新 → 看到新版本」这个循环变得即时。入口有两个：

- 工具窗口标题栏的刷新按钮；
- 工具窗口齿轮菜单中的 *Reload Page*。

刻意不绑定任何键盘快捷键，避免占用/冲突你现有的键位；如果愿意，可以在 **Settings | Keymap** 中自行分配（动作 id `dsh.web.reload`）。

## 配置

| 设置项 | 说明 |
|---|---|
| `-Ddsh.web.url`（JVM 属性） | Web UI 地址；默认 `http://127.0.0.1:3080`。在 **Help | Edit Custom VM Options…** 中设置，例如 `-Ddsh.web.url=http://127.0.0.1:9000`，然后重启一次 IDE。 |

## 构建

无需 Gradle/Kotlin —— 插件直接使用 JetBrains IDE 自带的 jar 和 JDK 编译：

```powershell
.\build.ps1 -IdeRoot "D:\Program Files\JetBrains\Rider 2026.2"
```

在 `releases/` 目录生成 `dsh-web-jetbrains-toolwindow.jar` / `dsh-web-jetbrains-toolwindow.zip`。

## License

[MIT](LICENSE) © DeepSeek Harness contributors
