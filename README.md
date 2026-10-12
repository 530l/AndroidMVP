# AndroidMVP - 现代化 Android Clean Architecture 实战项目

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024-green.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material-3-purple.svg)](https://m3.material.io)
[![Hilt](https://img.shields.io/badge/Hilt-2.51-orange.svg)](https://dagger.dev/hilt/)
[![Tests](https://img.shields.io/badge/Unit%20Tests-28%2F28%20Passed-brightgreen.svg)]()
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)]()

本项目是一套遵循 Google 官方现代 Android 应用架构指南（Guide to app architecture）的商业级 Clean Architecture 实战模板与组件库。项目专注于 **Jetpack Compose 现代化响应式 UI**、**单向数据流 (MVI/MVVM)**、**反金字塔扁平化组件编码**、**复杂嵌套滑动冲突治理** 以及 **物理流体动画引擎**。

---

## 目录
- [一、整体架构设计 (Clean Architecture)](#一整体架构设计-clean-architecture)
- [二、模块目录全景](#二模块目录全景)
- [三、核心架构与技术亮点](#三核心架构与技术亮点)
  - [1. UI 编码规范：消灭金字塔与巨石类](#1-ui-编码规范消灭金字塔与巨石类)
  - [2. 复杂手势与滑动冲突治理 (CollapsibleStickyTabPager)](#2-复杂手势与滑动冲突治理-collapsiblestickytabpager)
  - [3. 轮播图与指示器子系统 (Banner & DotsIndicator)](#3-轮播图与指示器子系统-banner--dotsindicator)
  - [4. 数据层：单一可信数据源 (SSOT) 与离线优先](#4-数据层单一可信数据源-ssot-与离线优先)
  - [5. 领域用例层 (Domain UseCase) 务实治理：拒绝形式主义](#5-领域用例层-domain-usecase-务实治理拒绝形式主义)
  - [6. 导航系统：AndroidX Navigation 3 + 预测性转场](#6-导航系统androidx-navigation-3--预测性转场)
  - [7. 全局吐司与提示反馈系统 (Toaster & AppToast)](#7-全局吐司与提示反馈系统-toaster--apptoast)
- [四、指示器样式矩阵 (BannerIndicatorStyle)](#四指示器样式矩阵-bannerindicatorstyle)
- [五、测试覆盖与质量保证](#五测试覆盖与质量保证)
- [六、构建与运行指南](#六构建与运行指南)

---

## 一、整体架构设计 (Clean Architecture)

项目严格执行分层解耦原则，依赖关系由外向内单向流动，隔离业务与 Android 框架生命周期：

```
                    ┌──────────────────────────────────────────┐
                    │        UI Layer (feature/*)              │
                    │   MVI / MVVM + BaseViewModel + UDF       │
                    └────────────────────┬─────────────────────┘
                                         │ 状态监听 / 事件派发
                    ┌────────────────────▼─────────────────────┐
                    │      Domain Layer (domain/*)             │
                    │   Pure Kotlin Models / Business UseCases │
                    └────────────────────┬─────────────────────┘
                                         │ 业务契约抽象
                    ┌────────────────────▼─────────────────────┐
                    │        Data Layer (data/*)               │
                    │   Repositories (SSOT) / DTOs / Mappers   │
                    └────────────────────┬─────────────────────┘
                                         │ 基础能力支撑
                    ┌────────────────────▼─────────────────────┐
                    │        Core Layer (core/*)               │
                    │   Database / Network / DesignSystem      │
                    └──────────────────────────────────────────┘
```

1. **`core/` 基础支撑层**：
   - **`network`**：Retrofit + OkHttp + `SafeApiCall` 统一密封结果建模（`AppResult`），隔离网络与解析异常。
   - **`database`**：Room 持久化（实体、DAO、Migration 版本演进机制完备）。
   - **`datastore`**：Jetpack DataStore 负责用户偏好设置持久化。
   - **`designsystem`**：沉淀通用基础组件、Haze 毛玻璃效果、`banner/` 轮播图子系统，以及基于 [getActivity/Toaster](https://github.com/getActivity/Toaster) 的全局吐司调度系统（`AppToast` / `AppToastMessages`）。
2. **`domain/` 业务用例层**：
   - 保持 100% 纯 Kotlin 模块，不包含任何 Android 平台依赖。
   - 包含真正承载业务流程与状态机的核心用例（如 `ToggleFavoriteUseCase` 收藏反转与跨 VM 复用、`SaveRepoRemarkUseCase` 防御性校验与脏检查）。遵循 Google 官方务实设计：纯数据流直接面向 `RepoRepository`，杜绝单行透传空壳用例。
3. **`data/` 数据流转层**：
   - 实现单一可信数据源（Single Source of Truth），`RepoRepositoryImpl` 协调本地 Room 缓存与远程接口。
   - 网络 DTO 与领域模型通过独立 Mapper 双向解耦映射。
4. **`feature/` 业务展示层**：
   - 按照业务垂直领域拆分模块（`home`、`profile`、`detail`、`settings`、`video`、`message`、`main`）。
   - 采用统一 `BaseViewModel` + MVI 单向数据流（`UiState`、`UiEvent`、`UiEffect`）。
5. **`navigation/` 路由层**：
   - 深度接入 Google 官方最新 AndroidX Navigation 3，封装 `AppNavHost` 与类型安全目的地 `Destinations`，配备手势平滑转场动画。

---

## 二、模块目录全景

```
com.lyf.androidmvp
├── AndroidApp.kt                         // 应用程序入口 (Hilt 容器配置与 Toaster 吐司初始化)
├── MainActivity.kt                       // 单 Activity 架构入口 (沉浸式状态栏与导航宿主)
├── core
│   ├── common                           // 通用基类 (AppResult / AppException / UiMessage / Dispatchers)
│   ├── database                         // Room 数据库 (AppDatabase / DAOs / Entities / Migrations)
│   ├── datastore                        // DataStore 偏好设置管理
│   ├── designsystem
│   │   └── component
│   │       ├── banner                   // ★ 商业级通用轮播图子系统 (Banner / Indicator / ImageBanner)
│   │       ├── paging                   // 分页组件 (LoadMoreState / Prefetch / PagingLayout)
│   │       ├── AppComponents.kt         // 全局公用轻量状态组件 (TopBar / Empty / Error / Loading)
│   │       ├── AppHaze.kt               // Haze 磨砂毛玻璃封装
│   │       ├── AppToast.kt              // ★ Toaster 全局吐司调度与 UiMessage 消费通道
│   │       └── CollapsibleStickyTabPager.kt // ★ 四重嵌套滑动冲突与吸顶调度容器
│   └── network                          // 网络引擎 (Retrofit / OkHttp / Interceptors / SafeApiCall)
├── data                                 // 数据仓储实现、DTO 模型、映射转换器
├── domain                               // 纯业务领域模型、核心业务用例 (ToggleFavorite / SaveRepoRemark)、仓储接口契约
├── feature
│   ├── home                             // 首页模块 (平铺解耦单文件组件)
│   ├── profile                          // 个人中心模块 (吸顶联动与功能网格)
│   ├── detail                           // 详情落地页
│   ├── settings                         // 偏好设置模块
│   ├── video / message / main           // 主导航容器与其他 Tab 功能页
└── navigation                           // Navigation 3 路由导航宿主与自定义预测性转场
```

---

## 三、核心架构与技术亮点

### 1. UI 编码规范：消灭金字塔与巨石类
- **告别千行大文件**：彻底消灭了以往将所有零碎 Composable 杂糅进单一 `XXXComponents.kt` 的臃肿模式。
- **细粒度扁平拆分**：
  - 首页细化为 `HomeTopBar`、`HomeSearchBar`、`HomeHeaderSection`、`HomeStickyTabs`、`HomePagerSection`、`ExploreMenuGrid` 等十余个单一职责独立文件。
  - 个人中心细化为 `ProfileHeaderSection`、`ProfileStatsRow`、`ProfileActionsRow`、`ProfileTabLayout` 等独立组件。
- **浅层嵌套**：控制 Composable 内部逻辑深度在 3~4 层以内，消除难以阅读的横向“金字塔缩进”。

### 2. 复杂手势与滑动冲突治理 (`CollapsibleStickyTabPager`)
针对常见的“可折叠头 + 吸顶 Tab + 横向 Pager + 纵向 `LazyColumn`”四重嵌套滑动冲突进行底层治理：
- **自定义 `NestedScrollConnection`**：精确截断并分配 Pre-scroll 与 Post-scroll 滚动手势位移与抛掷速度（Velocity），彻底解决“滑动吸顶后无法滑回、只能强制刷新”的交互死锁。
- **状态栏透明度与安全边距**：组件正确消费 `WindowInsets.statusBars`，杜绝吸顶后 Tab 延伸遮挡系统状态栏时间与电池图标。
- **Tab 靠左对齐**：使用 `ScrollableTabRow(edgePadding = 0.dp)` 替换默认居中排版，与内容流左侧边距严格对其。
- **外层响应式下拉刷新**：将 `PullToRefresh` 手势层级提至最外层，屏幕任意位置下拉均可自然调起刷新，贴合主流交互习惯。

### 3. 轮播图与指示器子系统 (`Banner` & `DotsIndicator`)
深度融合知名开源库 [ComposeViews](https://github.com/ltttttttttttt/ComposeViews) 与 [dotsindicator](https://github.com/tommybuonomo/dotsindicator) 的精髓并进行架构升华：
- **虚拟大页数无限循环**：无需手工维护环形数组下标，实现正反向无界滑动。
- **手势感知防打扰**：监听 `PressInteraction.Press` 与 `DragInteraction.Start`，用户按下或拖拽的瞬间**立即暂停倒计时**，松手后自动重新计时，杜绝“正在看图却被强行切走”。
- **画廊视差 (Gallery Effect)**：开启 `galleryEffect = true` 即享居中卡片 1.0x 聚焦放大、两侧未选中卡片平滑窥视缩放与透明度渐变。
- **实时手势位移透传**：指示器插槽直通 `currentPageOffsetFraction`，实现 0 延迟手势跟随。
- **环形闭环拓扑算法**：针对无限轮播场景推导自研 `computeContinuousOffset` 与 `circularDistance`，消除了传统指示器从“最后一页到第 0 页”时的跨屏穿透破绽。

### 4. 数据层：单一可信数据源 (SSOT) 与离线优先
- **缓存策略**：`RepoRepositoryImpl` 优先向上游抛出本地 Room Flow，同时静默触发网络拉取并刷新数据库，支持断网离线浏览与无缝同步。
- **健壮映射**：DTO 与 Domain Model 严格分离，网络层字段变更不会污染 Domain 和 UI 展现层。

### 5. 领域用例层 (Domain UseCase) 务实治理：拒绝形式主义
严格贯彻 Google 现代 Android 架构指南（MAD）与小团队敏捷 Clean 架构理念，彻底告别单行透传的“形式主义 UseCase”：
- **不用 UseCase 的场景（数据直达）**：无业务加工、无输入校验、无状态机流转的纯数据流（如首页列表流 `getRepos()`、详情只读 `getRepoById()`、下拉刷新 `refreshRepos()`、分页 `loadMoreRepos()`），ViewModel 直接依赖并调用 `RepoRepository`。消灭了无增值的 `GetReposUseCase` 样板代码。
- **必须使用 UseCase 的场景（业务充血与规则收敛）**：
  - **`ToggleFavoriteUseCase`**：闭环封装“项目存在性校验 $\rightarrow$ 原子状态反转 $\rightarrow$ 持久化 $\rightarrow$ 返回最新状态”，消灭了 `HomeViewModel` 与 `DetailViewModel` 重复编写的 10 行业务反转逻辑。
  - **`SaveRepoRemarkUseCase`**：封装领域不变量约束（空白字符裁剪、200 字上限强校验）与**脏检查（Dirty Checking，内容未变短路跳过数据库 I/O）**。
- **业务收益**：ViewModel 职责保持轻薄（专注 UI 状态与事件映射），核心业务逻辑与防御性规则 100% 由纯 Kotlin 单元测试秒级覆盖（脱离 Android 框架依赖）。

### 6. 导航系统：AndroidX Navigation 3 + 预测性转场
- 统一使用 `AppNavHost` 编排路由，支持类型安全的目的地参数传递。
- 在 `NavTransitions.kt` 中封装了现代 Material 风格的水平滑动推入与淡入淡出动效。

### 7. 全局吐司与提示反馈系统 (Toaster & AppToast)
将所有零散的 Snackbar / 弹窗提示升级为成熟稳定的 [getActivity/Toaster](https://github.com/getActivity/Toaster) 吐司体系：
- **疑难杂症底层防御**：彻底解决 Android 7.1.1 `BadTokenException` 崩溃、Android 11+ 后台静默丢弃 Toast，以及系统通知关闭后的吐司展示失效。
- **防止积压与即时反馈**：自动取消排队堆叠，以最新的微拟态半透明黑胶囊样式（`BlackToastStyle`）呈现，轻快即时。
- **UDF 单向数据流闭环**：通过 `AppToastMessages` 观察 ViewModel 的 `UiState.messages`，展示瞬间立即分发安全消费回调，根治切屏与重组时的消息重放。
- **全面瘦身**：剔除各业务界面冗余的 `SnackbarHostState` 与 `AppSnackbarHost` 样板代码，提升布局可读性。

### 8. 前后台切换与页面状态保活机制 (State Persistence & Lifecycle Defense)
彻底治理了 Android 应用在切回桌面/后台后，再次唤起时页面丢失位置或重新初始化重组的顽疾：
- **根 Activity 多重实例根治**：
  - 在 `AndroidManifest.xml` 中为 `MainActivity` 显式声明 `android:launchMode="singleTask"` 与 `android:alwaysRetainTaskState="true"`。
  - 在 `MainActivity.onCreate()` 中加入经典防御守则（检测非任务根节点 `!isTaskRoot` 的 Launcher 意图并立即 `finish()`），彻底杜绝从系统桌面再次点击图标时生成新 Activity 实例覆盖原有任务栈的 bug。
  - 声明 `android:configChanges="orientation|screenSize|screenLayout|keyboardHidden|uiMode"`，防止系统深浅色或配置变化导致 Activity 无故销毁重建。
- **Compose 滚动位移持久化 (`rememberSaveable`)**：
  - 折叠联动头部组件 `CollapsibleStickyTabPager` 的核心滑移变量 `headerOffsetPx` 与 `headerHeightPx` 全面采用 `rememberSaveable` 保存，无论前后台切换或组件临时挂起，折叠进度永不丢失还原。
  - 首页关键列表 `LazyListState` 提升至容器层（State Hoisting），与搜索/数据刷新平级解耦，杜绝列表加载与空态分支切换时重置滚动位置。

---

## 四、指示器样式矩阵 (`BannerIndicatorStyle`)

在 `BannerIndicatorStyle` 中提供了 7 种指示器样式，支持一行代码无缝切换：

| 样式策略 | 核心动效与力学原理 | 适用场景 |
| :--- | :--- | :--- |
| **`Worm`** | **真实物理双阶段蠕动流体形变**（前 50% 头部伸展拉长，后 50% 尾部收缩追赶） | 沉浸式图文卡片、主推活动 |
| **`Shift`** (当前首页默认) | **动态宽度对称涨缩**（当前项 3 倍胶囊拉伸，拖拽时与邻近圆点反比例平滑过渡） | 商业推广、电商活动 Banner |
| **`Spring`** | **平滑悬浮弹簧滑块**（浮动圆点在底轨各圆点间随手势平滑弹动） | 极简现代化界面、Tab 导航 |
| **`Balloon`** | **气球膨胀与缩小**（圆点以 GPU 图层随滑动实时缩放） | 引导页、生动交互场景 |
| **`Dots`** | **经典平滑圆点**（20% 动态缩放 + 品牌色实时渐变） | 经典简约轮播 |
| **`PillNumber`** | **数字药丸角标**（小红书/电商风格，如 `1/3`） | 大图展示、空间受限场景 |
| **`Lines`** | **极简进度线条** | 科技感与极简资讯流 |
| **`None`** | **无指示器** | 纯净内容卡片 |

### 快速接入示例：
```kotlin
// 最简开箱即用用法
ImageBanner(
    items = bannerList,
    onItemClick = { banner -> navigateTo(banner.linkUrl) },
    indicatorStyle = BannerIndicatorStyle.Shift(shiftFactor = 3f), // 自由切换上述任一样式
    modifier = Modifier.padding(horizontal = 16.dp)
)
```

---

## 五、测试覆盖与质量保证

项目配备了完善的单元测试套件，当前全量 **34 / 34 个单元测试全部通过 (100% Passed)**：

```bash
./gradlew testDebugUnitTest
```

### 测试矩阵概览：
- **`BannerIndicatorTest`**：验证无限轮播环形连续位移计算（`computeContinuousOffset`）及圆周最短拓扑距离（`circularDistance`）在各临界点下的数学准确性。
- **`LoadMoreStateTest`**：验证分页列表在 `Idle`、`Loading`、`NoMore`、`Error` 下的流转状态，以及复杂滑动场景下的预加载缓冲区计算。
- **`AppResultTest` & `UiMessageTest`**：验证统一结果类型转换与 UI 消息通道单次消费消费机制。
- **`WanArticleMapperTest` & `WanAndroidApiLiveTest`**：验证数据转换器的字段完备性与 API 数据契约解析。
- **`ToggleFavoriteUseCaseTest`**：验证领域收藏状态机翻转、前置非法 ID 拦截与持久化逻辑。
- **`SaveRepoRemarkUseCaseTest`**：验证领域备注 200 字防御性超限拦截、首尾空白裁剪及**脏检查（内容未变更零 I/O 短路）**。
- **`NavTransitionsTest` & `AppPermissionsTest`**：验证导航预测性动效计算与系统权限动态请求策略。

---

## 六、构建与运行指南

### 环境要求
- **Android Studio**：Ladybug (2024.2.1) 或更高版本
- **JDK**：JDK 17 或 JDK 21
- **Gradle**：8.10+
- **最低支持系统**：Android 7.0 (API Level 24+)
- **目标编译版本**：Android 15 (API Level 35)

### 常用命令
```bash
# 1. 编译 Debug APK
./gradlew assembleDebug

# 2. 执行全量单元测试
./gradlew testDebugUnitTest

# 3. 构建并直接安装至已连接的测试设备
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
```