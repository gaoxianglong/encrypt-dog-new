## 1. 恢复统计小窗与取消内窗透明

- [x] 1.1 `ProgressPanel`:恢复五个 StatCard 统计小窗(Operation/Files/Success/Failed/Elapsed,高 54、横向等宽、文字居中),begin/refresh/finish 驱动取值;移除纯文字统计行,状态文字回到顶部小字;验证 `mvn -q compile` 编译通过,无头断言:五窗等宽、操作类型值为 Encrypt/Decrypt、实时累加与完成终值正确
- [x] 1.2 `GlassCardPanel` 新增 `setFrosted(boolean)`(false 时不绘制填充/描边/高光/阴影);`EncryptDogFrame` 进入执行页关闭、返回表单页恢复;验证编译通过
- [x] 1.3 GUI 实测:执行页内窗无透明打底(表格与统计小窗直接呈现于背景),五个统计小窗与"通过"时一致;表单页毛玻璃不变

## 2. 回归验证

- [x] 2.1 终端模式回归:不带 `--gui` 执行一次加密与一次解密;验证终端交互与变更前一致
