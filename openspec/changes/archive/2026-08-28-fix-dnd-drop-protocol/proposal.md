# Proposal: 修复拖拽框 drop 处理协议违规

## Why

用户在 GUI 首页向拖拽框拖入文件夹中的文件时,拖放处理抛 `InvalidDnDOperationException: invalid rejectDrop()`,且之后整个应用的拖放子系统失效,后续拖拽不再有任何响应,只能重启应用恢复。根因是 `DropFilePanel$DndListener.drop` 违反 java.awt.dnd 协议:在 `acceptDrop()` 之后于 catch 块中调用 `rejectDrop()`;同时真正触发失败的异常(粘贴板无文件 URL → `getTransferData` 返回 null → 文件列表 NPE)被 `catch (Throwable)` 无日志吞掉;且 drop 从未走到 `dropComplete()`,导致 macOS 原生拖拽会话一直等待完成响应而卡死。

## What Changes

- 重写 `DropFilePanel$DndListener.drop` 的收尾逻辑,严格遵守 DnD 状态机:
  - `rejectDrop()` 仅允许在 `acceptDrop()` 之前调用(不支持文件列表 flavor 的分支);
  - `acceptDrop()` 之后无论成败 SHALL 调用 `dropComplete(success)`,不允许再抛异常穿出 drop 监听器;
- `getTransferData(javaFileListFlavor)` 结果 SHALL 判空处理,不把 null 传给文件列表回调;
- catch 范围从 `Throwable` 收窄为 `Exception`,并通过既有日志框架(logback)记录真实异常与当时可用的 DataFlavor 列表,便于定位拖拽来源;
- 面板与文件列表共享的 `DndListener` 行为统一修复(两个 DropTarget 共用同一监听器,一处修复即全覆盖)。

## Capabilities

### New Capabilities

<!-- 无新增能力,修复落在既有 swing-gui 能力上 -->

### Modified Capabilities

- `swing-gui`: 新增"拖拽数据获取失败/回调异常时 drop 必须完整收尾、真实异常记录日志、后续拖拽不受影响"的容错行为要求,作为"拖拽添加源文件"需求的补充。

## Impact

- 代码:`src/main/java/com/gxl/encryptdog/gui/interfaces/swing/DropFilePanel.java`(仅 `DndListener.drop` 方法及可能新增的日志依赖)。
- 行为:失败拖拽不再打印误导性 `InvalidDnDOperationException`、不再使后续拖拽失效;正常拖入文件/文件夹的行为不变。
- 无 API 变更、无依赖变更、无终端模式影响。
