## 1. 状态取值对齐核心枚举

- [x] 1.1 `OperationProgressDTO.FileProgress` 增加 `state` 字段(枚举状态值字符串);`EncryptCoreFacade` 快照组装循环中 `fp.setState(vs.getState().getState())`;验证 `mvn -q compile` 编译通过
- [x] 1.2 `ProgressPanel`:状态常量改为 `WAITING`/`RUNNING`/`FINISHED`;`resolveStatus` 优先取 `fp.getState()`、空值回退现有推导;`finish()` 行状态置 `FINISHED`;验证 `mvn -q compile` 编译通过
- [x] 1.3 无头断言:`begin()` 初始行为 `WAITING`;`refresh()` 行状态取快照枚举值;`finish()` 后行为 `FINISHED`;排序优先级 `RUNNING > WAITING > FINISHED`
- [x] 1.4 GUI 实测:State 列显示 `WAITING`/`RUNNING`/`FINISHED`(与核心枚举一致),完成行显示 `FINISHED`,成败由 Result 列区分;排序与配色语义不变

## 2. 回归验证

- [x] 2.1 终端模式回归:不带 `--gui` 执行一次加密与一次解密;验证终端交互与变更前一致
