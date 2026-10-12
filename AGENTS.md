# AGENTS.md - AndroidMVP 智能体协作与开发规范

> 本文件是所有参与 `AndroidMVP` 项目的 AI 智能体（Agents）以及开发者的**最高行为准则与架构契约**。任何在本项目中进行代码编写、重构或架构调整的智能体均须严格遵守本规范。

---

## 一、项目愿景与技术栈基线

- **项目定性**：商业级现代化 Android 单 Module Clean Architecture 架构实战与组件库。
- **开发语言**：Kotlin 2.2+ (JVM 目标版本 21 / 24)
- **UI 框架**：Jetpack Compose (BOM 2024+), Material 3
- **架构模式**：Clean Architecture + MVI (单向数据流 UDF) + BaseViewModel
- **依赖注入**：Dagger Hilt 2.51+
- **数据持久化**：Jetpack Room (SSOT) + Jetpack DataStore Preferences
- **网络通信**：Retrofit 2.11+ / OkHttp 4.12+ / 统一密封结果建模 (`AppResult<T>`)
- **路由导航**：AndroidX Navigation 3 (类型安全契约导航 + 预测性动画转场)

---

## 二、架构分层与依赖契约 (Clean Architecture)

```
UI Layer (feature/*) ──► Domain Layer (domain/*) ◄── Data Layer (data/*)
                                  │
                                  ▼
                         Core Layer (core/*)
```

### 1. 核心依赖原则
- **依赖倒置**：`domain/` 层是业务核心，**严禁依赖任何 Android 平台 API**（纯 Kotlin）。
- **契约归属**：所有仓储接口（`RepoRepository`）定义在 `domain/repository/` 中，其实现类（`RepoRepositoryImpl`）严格封存在 `data/repository/`。
- **模型解耦**：网络/数据库 DTO 模型严禁直接透传至 UI 层，必须通过 `data/mapper/` 显式转换为领域模型（Domain Model）。

---

## 三、Domain UseCase 黄金准则（严禁形式主义）

本工程严格贯彻 Google 官方现代 Android 应用架构指南（MAD）与敏捷 Clean 架构理念：

### 1. 【严禁】编写 1:1 透传的空壳 UseCase (Anti-Pattern)
- ❌ **禁止反模式**：
  ```kotlin
  // 严禁此类毫无业务价值的单行包装
  class GetReposUseCase(private val repo: RepoRepository) {
      operator fun invoke() = repo.getRepos()
  }
  ```
- **判定准则**：如果一个用例中**没有业务校验、没有状态机计算、没有跨数据源编排**，直接由 ViewModel 调用 `RepoRepository`。

### 2. 【必须】编写 UseCase 的四大法定场景
智能体仅在满足以下任一条件时创建/使用 UseCase：
1. **业务流程闭环（Business Workflow）**：包含前置校验、异步流程、状态持久化与结果派发。
   - *案例*：[`ToggleFavoriteUseCase`](file:///Users/a530/Desktop/kmpcmp/AndroidMVP/app/src/main/java/com/lyf/androidmvp/domain/usecase/ToggleFavoriteUseCase.kt) 负责查询实体、原子状态反转、持久化并返回最新状态。
2. **领域不变量与防御性规则（Invariants & Defensive Checks）**：
   - *案例*：[`SaveRepoRemarkUseCase`](file:///Users/a530/Desktop/kmpcmp/AndroidMVP/app/src/main/java/com/lyf/androidmvp/domain/usecase/SaveRepoRemarkUseCase.kt) 负责首尾空格裁剪、200 字超限防御拦截与**脏检查（Dirty Checking 拦截无意义 I/O）**。
3. **跨多个 Repository 的数据编排聚合（Multi-Repository Orchestration）**。
4. **跨 ViewModel 的高频业务复用**（杜绝各 ViewModel 复制粘贴相同业务规则）。

### 3. 【严禁】双重依赖异味（No Dual Injection Smell）
- 同一个 ViewModel 对同一业务实体，不能同时既调 UseCase 又私自绕开调 Repo 相同的逻辑。
- 业务操作走 UseCase，纯只读流走 Repo，职责清晰划分。

---

## 四、Jetpack Compose 界面编码铁律

### 1. 消灭金字塔缩进（Anti-Pyramid Rule）
- 单个 Composable 函数的代码嵌套深度**不得超过 3~4 层**。
- 复杂的布局块必须提取为平级专属的小型私有或独立 Composable 函数。

### 2. 严禁巨石文件（Anti-God-File Rule）
- 严禁将成百上千行的 Composable 杂糅在单一组件大文件中。
- 业务 Feature 界面必须扁平化拆分到独立单文件（如 `HomeTopBar.kt`、`HomeHeaderSection.kt`、`ExploreMenuGrid.kt`）。

### 3. 状态提升与单一事实源（State Hoisting）
- UI 组件必须是无状态（Stateless）的纯展示容器，只接收 `UiState` 数据，并通过 Lambda 向上派发用户事件。
- 业务状态管理必须收敛于 `ViewModel`，通过 `uiState: StateFlow<T>` 单向暴露。

### 4. 统一提示反馈铁律（Toaster & AppToast）
- **严禁私自声明冗余的 SnackbarHost 或原生 Android Toast**：项目统一使用基于 `getActivity/Toaster` 封装的 `AppToast` / `AppToastMessages`，杜绝系统版本兼容性问题（如 7.1.1 崩溃与 11+ 后台丢弃）。
- **UDF 单向数据流闭环**：ViewModel 产生的单次提示 `UiMessage`，统一在 Screen 容器层由 `AppToastMessages(state.messages, viewModel::dismissMessage)` 安全消费；快捷轻量级 UI 操作可直接调用 `AppToast.show(text)`。
- **消灭 Scaffold 样板冗余**：业务界面的 `Scaffold` 不得再挂载无意义的 `SnackbarHostState` 与 `AppSnackbarHost`。

### 5. 前后台切换与页面状态保活铁律（Lifecycle & State Persistence）
- **任务栈与启动模式基线**：主入口 Activity 必须配置 `android:launchMode="singleTask"` 与 `android:alwaysRetainTaskState="true"`，且在 `onCreate` 加入 `!isTaskRoot` 防御，杜绝用户退回后台再由桌面点击图标唤起时重复实例化覆盖已有任务栈。
- **滚动与折叠状态持久化**：所有动态计算的手势位移（如折叠头部 `headerOffsetPx`）必须使用 `rememberSaveable`，严禁仅用 `remember` 导致切后台或重组时被重置展开。
- **列表滚动状态提升（State Hoisting）**：核心列表的 `LazyListState` 提升至容器层管理，防止数据加载/空态/错误态切换时销毁滚动位置。

---

## 五、代码注释与文档完整性规范

1. **类级与核心业务必须具备完整 KDoc**：
   - 必须阐述 **Why**（为什么这样设计）与 **What**（核心业务职责），而非简单复述函数名。
   - 所有新增或重构的 UseCase 必须注明其业务规则与校验逻辑。
2. **保护既有注释（Documentation Integrity）**：
   - 重构代码时，必须保留原有业务背景、数学物理推导（如指示器算法）以及架构决策注释，严禁随意清空。
3. **语言规范**：
   - 代码标识符、类名、变量名全英文。
   - 业务逻辑、架构约束与注释推荐使用规范清晰的中文或英文。

---

## 六、构建验证与测试守则（Agent 必测项）

在结束任何代码生成或重构任务前，**智能体必须在本地执行并通过测试验证**：

### 1. 必跑命令
```bash
# 1. 运行全量单元测试（必须 100% 通过，0 Failure, 0 Error）
./gradlew testDebugUnitTest

# 2. 确保项目 Debug 编译无阻断报错
./gradlew assembleDebug

# 3. 如连接有测试机，验证部署运行
./gradlew installDebug
```

### 2. 测试规范
- 任何业务逻辑重构（如 UseCase 改造）必须同步更新或新增纯 JVM 单元测试（使用 `kotlinx-coroutines-test`）。
- 杜绝针对 Getter/Setter 编写形式主义单测，重点覆盖**边界条件、超限拦截、状态机反转与脏检查**。

---

## 七、智能体行为守则（Checklist）

- [ ] **拒绝过度设计**：不在小项目里随意拆分几十个子模块，坚持单 Module + 包内 Clean。
- [ ] **坚持平铺拆分**：不堆砌金字塔 Compose 代码。
- [ ] **执行业务充血**：UseCase 只为真实业务服务。
- [ ] **保障单测全绿**：提交任何改动前验证 `./gradlew testDebugUnitTest`。
- [ ] **同步更新文档**：重构若涉及对外特性或架构演进，同步维护 `README.md` 与本 `AGENTS.md`。
