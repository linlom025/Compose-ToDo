# ll-todo

一个基于 Jetpack Compose 的安卓待办应用，当前版本聚焦四象限任务管理与日历视图，适合个人任务规划和日常记录。

## 功能概览

- 四象限任务看板（重要/紧急）
- 首页直接创建任务
- 日历页按日期查看任务
- 任务详情支持时间与备注编辑
- 深浅主题切换

## 技术栈

- Kotlin + Jetpack Compose
- Coroutines + Flow
- Room + DataStore
- Navigation + ViewModel
- Hilt 依赖注入

## 环境要求

- Min SDK 21
- Target SDK 35
- Java 17
- Kotlin 2.1.10
- Android SDK（`platforms;android-35`、`build-tools;35.0.0`、`platform-tools`）

## 快速开始

```bash
git clone https://github.com/linlom025/Compose-ToDo.git
cd Compose-ToDo
```

## 构建与安装

- 构建 Debug APK：`./gradlew assembleDebug`
- 运行单元测试：`./gradlew testDebug`
- 安装到已连接设备：`./gradlew installDebug`

Windows 一键安全构建脚本：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\safe-debug-build.ps1
```

## 文档

- [开发说明（中文）](doc/开发说明.md)
- [Development Guide](doc/DEVELOPMENT_GUIDE.md)
- [项目结构](doc/project-structure.md)
- [架构说明](doc/architecture.md)

## 说明

本仓库为持续迭代中的项目版本，功能和 UI 会随开发继续更新。
