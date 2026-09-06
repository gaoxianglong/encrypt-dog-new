## 1. 滚动位置保持（方案 A）

- [x] 1.1 修改 `ProgressPanel.rebuildRows()`：在 `rowsPanel.removeAll()` 前保存 `scrollPane.getViewport().getViewPosition()`，在 `revalidate()`/`repaint()` 之后 `setViewPosition(saved)` 恢复，且恢复仅在 `refresh()`/`finish()` 路径生效（如给 `rebuildRows` 增加恢复开关）；`begin()` 路径不恢复、重建后显式 `setViewPosition(0,0)` 置顶，避免继承上一轮操作的滚动位置；尺寸（宽度与行数）未变化时跳过冗余的 `setBounds`/`setPreferredSize` 调用。验证：编译通过。
- [x] 1.2 编写/运行 Swing 层验证（沿用最小复现实验思路）：列表重建后 viewport 位置与滚动条 value 均保持在底部、可见区域仍为列表底部行，不出现 viewPosition=(0,0) 与滚动条脱钩。验证：验证程序输出位置一致。
- [x] 1.3 真实场景回归：大量文件（列表出现滚动条）执行加/解密，执行中将滚动条拉到中部与底部停留数秒。验证：每次刷新后视口不被拉回顶部、滚动条瞄定位置不变，可正常继续滚动。
- [x] 1.4 真实场景回归：完成一次操作（列表曾滚到非顶部）返回表单后再次提交新操作。验证：新操作执行页从列表顶部开始展示，不继承上一轮操作的滚动位置。

## 2. Elapsed 时钟统一

- [x] 2.1 修改 `ProgressPanel.finish()`：`statElapsed` 改用 `Utils.currentTimeFormat(elapsedSeconds())`，不再用 `result.getTimeConsuming()` 覆盖。验证：编译通过。
- [x] 2.2 真实场景回归：执行一批文件，记录完成前最后一次刷新显示的耗时与完成态显示的耗时。验证：完成态数值 ≥ 完成前数值，不出现 60s → 57s 的回退；`OperationResultDTO.timeConsuming` 字段保持填充（无其他 GUI 消费方）。
- [x] 2.3 回归：终端模式执行一次操作，确认终端 Dashboard 耗时展示与之前一致（core 计时未改动）。验证：终端输出耗时正常。

## 3. 收尾

- [x] 3.1 运行项目构建与既有测试（`mvn test` 或项目既有构建脚本），确认无回归。验证：构建与测试通过。
- [x] 3.2 执行 `openspec validate --changes 2026-09-06-fix-progress-panel-refresh` 并更新 README/版本说明（若项目惯例要求）。验证：validate 通过。
