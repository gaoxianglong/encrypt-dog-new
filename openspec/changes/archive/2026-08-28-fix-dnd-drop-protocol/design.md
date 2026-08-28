# Design: 修复拖拽框 drop 处理协议违规

## Context

动机与问题描述见 proposal.md「Why」。当前相关事实:

- `DropFilePanel` 只有一个 `DndListener`,被面板与文件列表两个 `DropTarget` 共用,修复一处即覆盖两个入口。
- 现行 `drop()` 流程:stopPulse → flavor 检查(此处 `rejectDrop()` 合法)→ `acceptDrop(COPY)` → `getTransferData(javaFileListFlavor)` → `onFilesDropped`(向 `DefaultListModel` 加路径)→ `startRipple` → `dropComplete(true)`;`catch (Throwable)` 中调用 `rejectDrop()`。
- java.awt.dnd 契约:`rejectDrop()` 仅在 `acceptDrop()` 之前合法;`acceptDrop()` 之后唯一合法的收尾是 `dropComplete(success)`;监听器抛出的异常不得穿出 `drop()`,否则原生拖拽会话(尤其 macOS)收不到完成响应而卡死,后续拖拽全部失效。
- 实测失败链:从 Finder 文件夹拖入真实文件时,JDK 原生层打印 "Looked for URLs on the pasteboard, but found none."(`CDataTransferer.m`),`getTransferData` 返回 null → `EncryptFormPanel.onFilesDropped` 的 for 循环 NPE → 被 `catch (Throwable)` 吞掉 → 对已 accept 的 drop 调 `rejectDrop()` 抛 `InvalidDnDOperationException`(用户看到的异常)。
- 日志约定:项目使用 Lombok `@Slf4j` + slf4j 占位符(`AbstractOperationTemplate` 已有先例),logback FILE appender,`com.gxl` 级别 INFO。

## Goals / Non-Goals

**Goals:**

- `drop()` 严格遵循 DnD 状态机:accept 前才 reject,accept 后无论成败必 `dropComplete`,任何异常不穿出监听器。
- 真实失败原因(异常 + 当时可用的 DataFlavor 列表)落日志,替代被吞掉的 `Throwable`。
- 拖放数据为 null/空时安全处理,不产生 NPE。
- 成功路径(拖入文件/文件夹添加列表、涟漪动画、拖拽完成)行为与修复前完全一致。

**Non-Goals:**

- 不改变 `dragEnter/dragOver/dragExit` 的既有 acceptDrag/rejectDrag 与脉冲逻辑(无协议问题)。
- 不尝试支持非文件拖放内容(文本/图片仍被拒绝)。
- 不改变文件添加后的校验时机(目录递归解析、后缀过滤仍在提交阶段执行)。
- 不涉及终端模式、加解密核心与文件格式。

## Decisions

**1. drop 收尾结构:accept 后 try/catch/finally,`dropComplete` 放 finally**

```
flavor 不支持 → rejectDrop()(accept 前,合法)→ return
acceptDrop(COPY)
success = false
try {
    取数 → 判空 → 回调 → 涟漪 → success = true
} catch (Exception e) {
    log.warn(异常 + 可用 flavors)
} finally {
    dropComplete(success)   // 无论成败、无论异常都执行
}
```

- **为什么**:完成动作在 `finally` 中保证,即使回调抛 `Error` 级别的异常,drop 会话也能收尾;这是对"任何异常不得穿出"的最强保证形式。
- **备选**:仅在 catch 中 `dropComplete(false)`——被否决,一旦出现未预料的异常类型(如 `Error`)或新增代码路径忘写完成,原生会话再次卡死。
- **备选**:catch 里补调 `rejectDrop()`——被否决,这正是本次要修的协议违规。

**2. 取数结果判空,按失败 drop 处理**

`getTransferData` 返回 null 或空列表时:记录 WARN 日志,不触发回调、不播涟漪,`success=false` 走 `dropComplete(false)`。

- **为什么**:macOS 上 `isDataFlavorSupported(javaFileListFlavor)` 通过 ≠ 数据真的可获取(本次实测即是);null 直接进入回调是 NPE 元凶。`false` 向拖拽源如实反馈"本次放下未生效"。
- **备选**:判空后视为成功——被否决,语义不诚实且误导拖拽源。

**3. 日志:DropFilePanel 增加 Lombok `@Slf4j`,`log.warn` 记录异常与 `getCurrentDataFlavorsAsList()`**

- **为什么**:与 `AbstractOperationTemplate` 既有日志风格一致(slf4j 占位符、logback FILE 输出);WARN 对应"已处理的失败",不过度;记录可用 flavors 是定位"Finder 拖文件却无 URL"触发条件的关键诊断信息。
- **备选**:System.err 打印——被否决,STDOUT appender 已禁用且与项目日志体系割裂。

**4. 修改范围仅限 `DndListener.drop` 一个方法**

`dragEnter/dragOver` 的 `acceptDrag/rejectDrag`、动画计时器、模型监听逻辑均不动,降低回归面。

## Risks / Trade-offs

- [失败 drop 调用 `dropComplete(false)` 时,拖拽源(如 Finder)可能显示放下的副本动画回滚] → 可接受,这是如实反馈失败的正常表现,好于会话卡死。
- [连续拖入非法内容会在 FILE 日志产生多条 WARN] → 可接受,drop 失败本属低频;日志量级远小于加解密操作日志。
- [修改触及两个 DropTarget 共用的监听器,若行为回归会同时影响面板与列表] → 修复为单点且逻辑收敛到协议正确形态,回归面集中;按 tasks 中的手工验证清单回归拖拽主路径。

## Migration Plan

无迁移:仅 GUI 单文件方法级修复,无数据/格式/依赖变更。回滚方式为 revert 对应提交。

## Open Questions

- 触发条件未完全钉死:从 Finder 普通文件夹拖文件为何出现粘贴板无 URL(疑似 iCloud 占位文件、file promise 或粘贴板竞态)。修复本身是防御性的、与触发条件无关;若修复后复现,WARN 日志中的 flavors 列表 + 原生日志行足以继续定位,预计无需再改代码。
