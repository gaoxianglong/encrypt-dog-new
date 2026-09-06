## Context

`AbstractOperationTemplate.execute()` 的结构：try-with-resources 内打开源/目标流，之后才构建 `EncryptContext`；catch 块无条件调用 `onFailure(encryptContext)`。打开流失败时 `encryptContext` 仍为 null → `onFailure` 内 `setResult` 空指针 → 失败事件链（`ResultEvent`）不触发 → 该文件的 DashboardViewState 停留在 RUNNING、计数漏算、GUI 行永远卡住（实况：2026-09-05 加密批次 39 个 `._` 文件、2026-09-06 解密批次 `._JJY-20241229.mov.dog` 均命中）。

根因机制（已实验证实）：exFAT 卷上 `unlink` 主文件会连带删除其 `._` AppleDouble 伴生文件；带"删除源文件"的批次中，主文件先完成并删除源文件，伴生的 `._` 文件随之消失，其任务的打开流即失败。

状态机约束（关键）：`EncryptStateSceneEnum` 仅允许 WAITING→RUNNING 与 RUNNING→FINISHED 两种迁移，WAITING→FINISHED 非法。失败路径 `onFailure` 直接做 setState(FINISHED)——从 WAITING 出发会抛 `StateException` 并被 execute 的空 catch 静默吞掉。因此补建上下文的路径必须显式先迁到 RUNNING。

失败原因展示现状：`DashboardViewState.errorMsg` 在 core 中没有任何写入方（默认 "-"），GUI 失败图标悬停显示 "-"。本变更仅在"前置失败"路径写入原始错误消息；常规失败路径的 errorMsg 不可见属于既有独立缺口，不在本变更范围（在提案 Impact 中已注明）。

`FileNameParser` 为终端与 GUI 共享（GUI 防腐层 `EncryptCoreFacade.parseFiles` 调用同一实现），`._` 过滤放在解析器内即可两端一致。

## Goals / Non-Goals

**Goals:**

- 打开流失败的单个文件任务：无未捕获异常、必达 FAILED 终态、失败计数正确、GUI 悬停可见原始错误原因。
- `._` AppleDouble 文件不进入操作列表（终端 + GUI、加密 + 解密一致），消除其制造的竞态与垃圾产物。

**Non-Goals:**

- 不改变常规失败路径（parseHeader/store 等阶段失败）的既有错误展示语义（errorMsg 仍为 "-"）。
- 不扩展状态机场景矩阵（沿用两段迁移，不改枚举）。
- 不引入任务重试或文件存在性预检（仍为"先解析后执行"，竞态由解析过滤消减；残余竞态由失败终态兜底）。

## Decisions

### 决策 1：catch 内补建最小上下文 + 两段状态迁移

在 `execute()` 的 catch 中，当 `encryptContext == null` 时：

1. 补建最小 `EncryptContext`：`resultContext = context`、`operationVO = operationVO`、`defaultCapacity = 0`、`sourceFileCapacity` 沿用已取值。这些字段是 `EncryptProcessStateImpl.setState`（取 key、查 viewState）与 `onFailure`（setResult、deleteTargetFile 读取 operationVO）的必需输入。
2. 将原始错误消息写入该文件视图状态的 `errorMsg`（`context.getDashboardViewStates().get(sourceFilePath).setErrorMsg(e.getMessage())`），GUI 失败图标悬停即展示真实原因。
3. 显式先迁移 `WAITING → RUNNING`（该场景不下发事件），再调用 `onFailure`：`RUNNING → FINISHED` 合法迁移并下发 `ResultEvent` → `ResultListener` 递增失败计数、写入结果。两段迁移避免了 WAITING→FINISHED 的 `StateException`。
4. `onFailure` 内 `deleteTargetFile` 对未创建的目标文件是无害 no-op，无需特判。

- 替代方案 1：扩展 `EncryptStateSceneEnum` 增加 WAITING→FINISHED 场景。改动共享状态机语义、影响面大，且破坏了"RUNNING 必现"的既有状态轨迹假设，不选。
- 替代方案 2：`onFailure` 判空直接返回。虽免 NPE，但失败事件链仍不触发——行依然卡在 RUNNING、计数漏算，未达到需求，不选。

### 决策 2：解析器内过滤 `._` 前缀

在 `FileNameParser` 中增加文件名前缀判断（basename 以 `._` 开头即忽略），两处生效：`parseRule` 的显式文件分支与 `parseFiles` 的目录递归分支。加密/解密、终端/GUI 因共享解析器天然一致；后缀校验（suffixValidate）在过滤之后执行，互不干扰。

- 过滤范围仅 `._` 前缀（AppleDouble 专用），不过滤所有点开头文件（`.env` 等用户可能有意处理的文件不受影响）。
- 若过滤后列表为空，维持既有 `ParseException("No files available")` 路径（parse 内 `files.isEmpty()` 检查天然覆盖）。
- 替代方案：仅在 GUI 侧过滤。终端与 GUI 行为分裂，且终端用户同样会被 `._` 文件噪音与竞态影响，不选。

### 决策 3：目标文件不存在时结果大小显示 `-`

`ResultEvent` 构建时 `Utils.getFileCapacity(targetFilePath)` 对不存在的目标返回 -1，`capacityFormat(-1)` 得到 `-0.00MB`。改为容量 < 0 时目标大小取 "-"，与"完成前为 -"的既有展示语义一致。仅影响前置失败路径（常规失败路径在删除目标前取值，不受影响）。

## Risks / Trade-offs

- [补建上下文遗漏字段 → 状态机/事件链再次失败] → 最小字段集为 setState 与 onFailure 的读取路径逐一核对得出（resultContext/operationVO 为必需），实现时以单测覆盖"打开失败 → 行 FAILED → 计数+1 → 无异常"。
- [显式指定 `._` 文件被静默跳过 → 用户困惑] → 属于 macOS 惯例（Finder 不可见）；需求场景已声明该行为，且全部被过滤时有明确报错。
- [前置失败行 After Size 显示 -] → 决策 3 修正为 "-"，与等待/未完成语义一致。
- [残余竞态（非 `._` 文件在解析后消失）] → 仍会发生，但由决策 1 兜底为一行带原因的 FAILED，不再产生 NPE 与卡死行。
- [errorMsg 仅前置失败路径可读，常规失败仍为 "-"] → 既有独立缺口，本变更不扩大范围；后续可单独立变更统一。

## Migration Plan

无需迁移。行为收敛：失败文件从"卡 RUNNING + NPE 噪音"变为"FAILED + 可见原因"；`._` 文件不再出现在操作列表。回滚方式为 revert 本次提交。
