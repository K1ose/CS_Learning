---
title: Electron - 01 Tutorial
top: false
comment: false
lang: zh-CN
date: 2023-10-29 08:56:05
tags:
categories:
  - Full Stack
  - Electron
---

# Electron

## 简介

Electron 是一个使用 JavaScript、HTML 和 CSS 构建桌面应用程序的框架，它将 Chromium 和 Node.js 嵌入到了二进制文件中，使得一次coding便能实现支持 Windows、macOS 和 Linux 的跨平台应用。

Electron 是一个网页应用的原生包装层，在 Node.js 环境中。

作为一个教程，目标是指导使用Electron开发一个桌面应用，并使用Electron Forge 打包分发给终端用户使用。如果希望使用单命令样板开始项目，建议使用 Electron Forge 的 `create-elctron-app` 命令。

## 一些准备

工欲善其事必先利其器，下面列出了一些所需的工具：

- 代码编辑器：VS Code, Vim ...
- 命令行工具：Windows PowerShell, macOS Terminal ...
- Git and Github
- Node.js and npm

## 第一个应用程序

在使用 Windows 系统时，需要避免使用 WSL ，否则在尝试运行程序的时候可能会遇到问题。

### 初始化 npm 项目

Electron基于 npm 搭建，以 package.json 文件作为入口点，使用 npm 命令来初始化项目。

```bash
mkdir my-electron-app && cd my-electron-app
npm init
```

在进行初始化的时候，会被问到几个问题：

```
package name: (my-electron-app)
version: (1.0.0)
description: my first electron app
entry point: (index.js) main.js
test command:
git repository:
keywords:
author: K1ose
license: (ISC)
About to write to D:\coding\Project\my-electron-app\package.json:

{
  "name": "my-electron-app",
  "version": "1.0.0",
  "description": "my first electron app",
  "main": "main.js",
  "scripts": {
    "test": "echo \"Error: no test specified\" && exit 1"
  },
  "author": "K1ose",
  "license": "ISC"
}


Is this OK? (yes)

```

这里面有几条规则需要遵循：

- 入口点应当是 `main.js` 
- author、licnse 和 description 可以是任意值，但是 scripts 是必须要填写的，这是应用打包的相关配置信息。事实上，打包后的应用本身会包含 Electron 的二进制文件，所以 Electron 是 Dev Dependency 的，不需要将其作为生产环境的依赖。

接下来安装 Electron：

```bash
npm install electron --save-dev
```

结果发现报错了：

```
npm ERR! code 1
npm ERR! command failed
npm ERR! RequestError: read ECONNRESET
```

最后通过在项目根目录新建 `.npmrc` 文件，并写入：

```
electron_mirror=https://npmmirror.com/mirrors/electron/
```

也可以尝试官方的解决方案：[安装指南](https://www.electronjs.org/zh/docs/latest/tutorial/installation)

重新 install 后，成功安装。

```
> npm install electron --save-dev

added 75 packages, and audited 76 packages in 48s

20 packages are looking for funding
  run `npm fund` for details

found 0 vulnerabilities
```

此时的目录结构如下：

```
Mode                 LastWriteTime         Length Name
----                 -------------         ------ ----
d-----        2023/10/29      9:52                node_modules
-a----        2023/10/29      9:48             56 .npmrc
-a----        2023/10/29      9:52          31116 package-lock.json
-a----        2023/10/29      9:52            290 package.json
```

`node_modules` 文件夹包含了 Electron 可执行文件，而 `package-lock.json` 则指定了各个依赖的确切版本。

现在应当使用 `.gitignore` 来避免将 `node_modules` 文件夹提交到版本控制系统中。这是一份 Github 的 [Node.js gitignore 模板](https://github.com/github/gitignore/blob/main/Node.gitignore) ，可以 copy 到根目录使用。

### 运行程序

在 `package.json` 中，我们指定了 `main.js` 作为应用的入口，这个文件控制 main process ，运行在 Node.js 环境中，可以执行任意 Node.js 代码，甚至作为交互解释器 REPL 使用。它负责控制应用的生命周期、显示原生界面、执行特殊操作并管理 renderer processes 。

现在创建 `main.js` 并写入如下代码：

```javascript
// file: main.js
console.log('Hello from Electron')
```

 要执行脚本，还需要在 package.json 的 `scripts` 字段中添加一个 `start` 命令，内容为 `electron .` ，用来告诉 Electron 在当前目录下寻找主脚本，以开发模式运行。

```json
// file: package.json
{
  "name": "my-electron-app",
  "version": "1.0.0",
  "description": "my first electron app",
  "main": "main.js",
  "scripts": {
    "test": "echo \"Error: no test specified\" && exit 1",
    "start": "electron ."
  },
  "author": "K1ose",
  "license": "ISC",
  "devDependencies": {
    "electron": "^27.0.2"
  }
}
```

最后使用 npm 运行指定的 scripts 。

```bash
npm run start
```

输出了一行 `Hello from Electron` 。

### BrowserWindow

在 Electron 中，每个窗口站是一个页面，后者可以来自本地的HTML，也可以来自远程的URL。在根目录中创建一个 `index.html` ，内容如下：

```html
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <!-- https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP -->
    <meta
      http-equiv="Content-Security-Policy"
      content="default-src 'self'; script-src 'self'"
    />
    <meta
      http-equiv="X-Content-Security-Policy"
      content="default-src 'self'; script-src 'self'"
    />
    <title>Hello from Electron renderer!</title>
  </head>
  <body>
    <h1>Hello from Electron renderer!</h1>
    <p>👋</p>
  </body>
</html>
```

接着修改 `main.js` 中的代码：

```javascript
const { app, BrowserWindow } = require('electron')

const createWindow = () => {
  const win = new BrowserWindow({
    width: 800,
    height: 600
  })

  win.loadFile('index.html')
}

app.whenReady().then(() => {
  createWindow()
})
```

- Line 1: 该语句使用 CommonJS 语法，导入了两个 Electron 模块（为了在写 TypeScript 代码时更快速的检查，可以选择从 `electron/main` 中导入模块）：
  - app: 负责应用应用程序的事件生命周期；
  - BrowserWindow: 负责创建和管理应用窗口；

- Line 3-10: 该语句将可复用的函数写入实例化窗口：
  - `createWindow()` 函数将页面加载到新的 BrowserWindow 实例中；

- Line 12-14: 在应用准备就绪时，调用函数：
  - Electron 许多核心模块都是 Node.js 的事件触发器，遵循 Node.js 的异步事件驱动架构，app 模块就是其中之一；
  - app的ready 事件触发后，才能构建 BrowserWindow 实例。可以使用 `app.whenReady()` 这个API来监听此事件，在其成功后调用 `createWindow()` 方法；

> 通常使用 `.on` 函数来监听 Node.js 事件，但是 Electron 给出 `app.whenReady()` 方法，作为 ready 事件专用的监听器，能避免直接建通 `.on` 事件带来的问题。

此时，运行 `start` 命令应该能成功地打开一个包含您网页内容的窗口！

您应用中的每个页面都在一个单独的进程中运行，我们称这些进程为 渲染器 (renderer\) 。 渲染进程使用与常规Web开发相同的JavaScript API和工具，例如使用 [webpack](https://webpack.js.org/)来打包和压缩您的代码，或使用 [React](https://reactjs.org/) 构建用户界面。

### 管理应用的窗口生命周期

应用窗口在不同操作系统中的行为也不同。 Electron 允许您自行实现这些行为来遵循操作系统的规范，而不是采用默认的强制执行。 您可以通过监听 app 和 BrowserWindow 模组的事件，自行实现基础的应用窗口规范。

通过检查 Node.js 的 [`process.platform`](https://nodejs.org/api/process.html#process_process_platform) 变量，我们可以针对特定平台运行特定代码。 请注意，Electron 目前只支持三个平台：`win32` (Windows), `linux` (Linux) 和 `darwin` (macOS) 。

#### 关闭所有窗口时退出应用 (Windows & Linux)

在 Windows 和 Linux 上，我们通常希望在关闭一个应用的所有窗口后让它退出。 要在您的Electron应用中实现这一点，您可以监听 app 模块的 [`window-all-closed`](https://www.electronjs.org/zh/docs/latest/api/app#event-window-all-closed) 事件，并调用 [`app.quit()`](https://www.electronjs.org/zh/docs/latest/api/app#appquit) 来退出您的应用程序。此方法不适用于 macOS。

```js
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit()
})
```

#### 如果没有窗口打开则打开一个窗口 (macOS)

与前二者相比，即使没有打开任何窗口，macOS 应用通常也会继续运行。 在没有窗口可用时调用 app 会打开一个新窗口。

为了实现这一特性，可以监听模组的 [`activate`](https://www.electronjs.org/zh/docs/latest/api/app#event-activate-macos) 事件，如果没有任何活动的 BrowserWindow，调用 `createWindow()` 方法新建一个。

因为窗口无法在 `ready` 事件前创建，你应当在你的应用初始化后仅监听 `activate` 事件。 要实现这个，仅监听 `whenReady()` 回调即可。

```js
app.whenReady().then(() => {
  createWindow()

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})
```

### VS Code 调试

如果您希望使用 VS Code 调试您的程序，您需要让 VS Code 监听主进程 (main process) 和渲染器进程 (renderer process) 。 下面为您提供了一个简单的配置文件。 请在根目录新建一个 `.vscode` 文件夹，然后在其中新建一个 launch.json 配置文件并填写如下内容。

```json
{
  "version": "0.2.0",
  "compounds": [
    {
      "name": "Main + renderer",
      "configurations": ["Main", "Renderer"],
      "stopAll": true
    }
  ],
  "configurations": [
    {
      "name": "Renderer",
      "port": 9222,
      "request": "attach",
      "type": "chrome",
      "webRoot": "${workspaceFolder}"
    },
    {
      "name": "Main",
      "type": "node",
      "request": "launch",
      "cwd": "${workspaceFolder}",
      "runtimeExecutable": "${workspaceFolder}/node_modules/.bin/electron",
      "windows": {
        "runtimeExecutable": "${workspaceFolder}/node_modules/.bin/electron.cmd"
      },
      "args": [".", "--remote-debugging-port=9222"],
      "outputCapture": "std",
      "console": "integratedTerminal"
    }
  ]
}
```



保存后，当您选择侧边栏的 “运行和调试”，将会出现一个 "Main + renderer" 选项。然后您便可设置断点，并跟踪主进程和渲染器进程中的所有变量。

上文中我们在 `launch.json` 所做的其实是创建三个配置项：

- `Main` 用来运行主程序，并且暴露出 9222 端口用于远程调试 (`--remote-debugging-port=9222`) 。 我们将把调试器绑定到那个端口来调试 `renderer` 。 因为主进程是 Node.js 进程，类型被设置为 `node`。
- `Renderer` 用来调试渲染器进程。 因为后者是由主进程创建的，我们要把它 “绑定” 到主进程上 ()`"request": "attach"`，而不是创建一个新的。 渲染器是 web 进程，因此要选择 `chrome` 调试器。
- `Main + renderer` 是一个 [复合任务](https://code.visualstudio.com/Docs/editor/tasks#_compound-tasks)，可以同时执行上述任务。

## 使用预加载脚本

Electron 的主进程是一个拥有着完全操作系统访问权限的 Node.js 环境。 除了 [Electron 模组](https://www.electronjs.org/zh/docs/latest/api/app) 之外，您也可以访问 [Node.js 内置模块](https://nodejs.org/dist/latest/docs/api/) 和所有通过 npm 安装的包。 另一方面，出于安全原因，渲染进程默认跑在网页页面上，而并非 Node.js里。

为了将 Electron 的不同类型的进程桥接在一起，我们需要使用被称为 **预加载** 的特殊脚本。

### 使用预加载脚本增强渲染器

BrowserWindow 的预加载脚本运行在具有 HTML DOM 和 Node.js、Electron API 的有限子集访问权限的环境中。

>  **info 预加载脚本沙盒化**
>
> 从 Electron 20 开始，预加载脚本默认 **沙盒化** ，不再拥有完整 Node.js 环境的访问权。 实际上，这意味着你只拥有一个 polyfilled 的 `require` 函数，这个函数只能访问一组有限的 API。
>
> | 可用的 API            | 详细信息                                                     |
> | --------------------- | ------------------------------------------------------------ |
> | Electron 模块         | 渲染进程模块                                                 |
> | Node.js 模块          | [`events`](https://nodejs.org/api/events.html)、[`timers`](https://nodejs.org/api/timers.html)、[`url`](https://nodejs.org/api/url.html) |
> | Polyfilled 的全局模块 | [`Buffer`](https://nodejs.org/api/buffer.html)、[`process`](https://www.electronjs.org/zh/docs/latest/api/process)、[`clearImmediate`](https://nodejs.org/api/timers.html#timers_clearimmediate_immediate)、[`setImmediate`](https://nodejs.org/api/timers.html#timers_setimmediate_callback_args) |
>
> 有关详细信息，请阅读 [沙盒进程](https://www.electronjs.org/zh/docs/latest/tutorial/sandbox) 教程。

与 Chrome 扩展的[内容脚本](https://developer.chrome.com/docs/extensions/mv3/content_scripts/)（Content Script）类似，预加载脚本在渲染器加载网页之前注入。 如果你想为渲染器添加需要特殊权限的功能，可以通过 [contextBridge](https://www.electronjs.org/zh/docs/latest/api/context-bridge) 接口定义 [全局对象](https://developer.mozilla.org/en-US/docs/Glossary/Global_object)。

为了演示这一概念，你将会创建一个将应用中的 Chrome、Node、Electron 版本号暴露至渲染器的预加载脚本

新建一个 `preload.js` 文件。该脚本通过 `versions` 这一全局变量，将 Electron 的 `process.versions` 对象暴露给渲染器。
