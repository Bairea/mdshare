# MdShare

Android 端 Markdown 分享卡片工具。目标是把系统分享或手动粘贴的 Markdown 内容，快速生成适合微信传播的单张长图，重点保证表格完整显示、代码块可读、操作链路尽量短。

## 当前交付物

- 调试安装包：`dist/mdshare-debug.apk`
- 原始构建产物：`app/build/outputs/apk/debug/app-debug.apk`
- AndroidTest 安装包：`app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`

说明：

- 当前仓库是 Android 应用工程，不会直接产出原生 Windows `exe`。
- 如果后续确实需要 `exe`，那属于另一个交付方向：要么做 Windows 桌面版，要么做一个 Windows 安装/分发壳来辅助安装 `apk` 到设备。
- 本次已先打包出可安装的 Android `apk`，并把项目积累整理到本 README。

## 项目目标

- 从系统 `ACTION_SEND` / `ACTION_PROCESS_TEXT` 接收 Markdown 或普通文本
- 在编辑页中快速调整内容
- 在预览页检查标题、正文、表格、代码块效果
- 本地生成单张长图
- 支持保存到相册与系统分享图片

需求基线来自：

- 设计稿：`docs/superpowers/specs/2026-05-09-markdown-share-card-android-design.md`
- 执行计划：`.trae/documents/2026-05-11-windows-android-mvp-tdd-plan.md`

## 当前能力

- 分享入口
  - 支持 `ACTION_SEND` / `ACTION_PROCESS_TEXT`
  - 能从系统分享文本进入编辑页
- 编辑页
  - 支持加载初始 Markdown
  - 支持标题提取、清空、预览
  - 编辑区已切换为原生 `EditText`，并在模拟器上验证了长文本滚动行为
- 预览页
  - 使用 `WebView` 预览本地渲染结果
  - 生成图片时优先复用当前预览页已有的 `WebView`
- 结果页
  - 展示导出图片
  - 支持保存到相册
  - 支持系统分享图片
  - 支持返回重新编辑
- 渲染能力
  - `flexmark` 负责 Markdown 转 HTML
  - `highlight.js` 负责代码高亮
  - 本地 CSS/JS 负责卡片样式、表格紧凑策略、代码块样式

## 代码结构

- `app/src/main/java/com/example/mdshare/MainActivity.kt`
  - 应用入口
- `app/src/main/java/com/example/mdshare/navigation/AppNavGraph.kt`
  - 编辑 -> 预览 -> 结果主流程
- `app/src/main/java/com/example/mdshare/feature/editor/`
  - 编辑页、状态与 ViewModel
- `app/src/main/java/com/example/mdshare/feature/preview/`
  - 预览页 `WebView`
- `app/src/main/java/com/example/mdshare/feature/result/`
  - 结果页与保存/分享操作
- `app/src/main/java/com/example/mdshare/render/`
  - Markdown 转 HTML、模板、主题、图片导出
- `app/src/main/java/com/example/mdshare/export/`
  - 相册保存、图片分享
- `app/src/main/assets/render/`
  - `render.css` / `render.js` / `highlight.min.js`
- `sample/`
  - 表格、代码、混合内容样例 Markdown

## 本轮累计改动摘要

### 1. 工程与环境

- 补齐 `Gradle Wrapper`
- 修复 Android 主题缺失与构建依赖问题
- 统一工程运行环境为 `JDK 17 + Android SDK 34`

### 2. MVP 主链路

- 将占位导航改成真实主流程
- 打通编辑页、预览页、结果页
- 新增保存相册和系统分享模块

### 3. Markdown 显示策略

- 将正文、代码块、表格样式分离
- 代码块单独使用更紧凑的等宽字体
- 表格增加普通表格与紧凑表格两档
- 固定分享画布思路仍保留在 HTML 渲染链路中

### 4. 编辑区滚动问题

- 之前出现过多个阶段性问题：
  - 外层布局吃掉按钮
  - 输入前可动、输入后不可动
  - 键盘弹出/收起后手势与布局错乱
- 当前实现已改为固定可视高度的原生 `EditText`
- 已在模拟器上补了设备级回归测试，验证长文本在编辑区内可滚动

### 5. 预览与导出

- 预览页会持有当前已加载完成的 `WebView`
- 点击“生成图片”优先复用预览中的 `WebView` 抓图，而不是重新走另一套页面加载链路

## 测试与验证

### 已补的自动化测试

- 单元测试
  - `EditorViewModelTest`
  - `MarkdownPipelineTest`
  - `RenderAssetsTest`
  - `ImageExporterTest`
  - `ShareImageUseCaseTest`
  - `EditorRouteLayoutTest`
- 仪器测试
  - `MainActivityIntentTest`
  - `EditorRouteTest`
  - `EditorScrollBehaviorTest`
  - `WebViewImageRendererTest`

### 已执行验证

- 本地通过：
  - `.\gradlew.bat testDebugUnitTest`
  - `.\gradlew.bat :app:assembleDebug`
  - `.\gradlew.bat :app:assembleDebugAndroidTest`
- 设备/模拟器侧已验证：
  - 编辑区长文本滚动行为通过 `EditorScrollBehaviorTest`

## 已知问题

目前最顽固、尚未彻底收口的问题是：

- 预览页点击“生成图片”时，仍可能出现：
  - `Timed out while rendering HTML in WebView`

说明：

- 这个问题不是用户个例，之前已经在模拟器跑设备测试时复现过 `WebViewImageRenderer` 相关超时。
- 编辑区滚动这条线已经进入设备级验证闭环。
- 图片导出超时这条线仍需要继续拆解主流程中的 `WebView` 渲染/截图时机。

## 构建方式

### 构建调试包

```powershell
.\gradlew.bat :app:assembleDebug
```

### 安装到设备/模拟器

```powershell
.\gradlew.bat :app:installDebug
```

如果需要先卸载旧包：

```powershell
adb uninstall com.example.mdshare
.\gradlew.bat :app:installDebug
```

### 运行测试

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:assembleDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

### 单独运行编辑滚动设备测试

```powershell
adb shell am instrument -w -e class com.example.mdshare.feature.editor.EditorScrollBehaviorTest com.example.mdshare.test/androidx.test.runner.AndroidJUnitRunner
```

## 使用方式

### 方式 1：系统分享进入

1. 在外部 App 中复制或分享一段 Markdown / 文本
2. 选择“分享至 MdShare”
3. 在编辑页检查内容
4. 点击“预览”
5. 在预览页点击“生成图片”
6. 在结果页保存或分享

### 方式 2：手动粘贴

1. 打开 App
2. 在编辑区粘贴 Markdown
3. 点击“预览”
4. 后续同上

## 样例文件

- `sample/ai-answer-table.md`
- `sample/ai-answer-code.md`
- `sample/ai-answer-mixed.md`

## 后续建议

优先级最高的是继续收口以下问题：

1. `WebView` 生成图片超时
2. 主流程级导出回归测试
3. 代码块与宽表在真实导出图中的可读性细调
4. 调试包之外的正式发布包策略

如果后续需要“Windows exe”，建议单独立项决定方向：

- 方案 A：继续保持 Android App，只提供 Windows 安装辅助工具
- 方案 B：新增 Windows 桌面版渲染器
- 方案 C：做一个 Electron / Compose Desktop 包装层，再输出 `exe`
