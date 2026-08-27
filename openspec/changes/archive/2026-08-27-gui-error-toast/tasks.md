## 1. ToastBubble 组件

- [x] 1.1 新建 `ToastBubble`:ACCENT 渐变圆角气泡 + 白色文字 + 底部箭头,单 Timer 三阶段动画(淡入 300ms → 保持至 3300ms → 淡出 800ms 后隐藏),重复 `show(message)` 重置计时;验证 `mvn -q compile` 编译通过
- [x] 1.2 气泡宽度按文案自适应(最大宽度上限、超长换行最多两行),水平居中于主按钮;验证无头断言短/长文案下气泡 bounds 正确

## 2. 接入表单

- [x] 2.1 `EncryptFormPanel`:移除 `errorLabel`(字段/构造样式/layoutRows 登记),`showError`/`clearError` 改为驱动 `toastBubble`,气泡 bounds 登记到 `layoutRows`;验证编译通过,无头断言解密模式下气泡 y 随主按钮同步上移 46
- [x] 2.2 校验失败与操作失败返回表单时弹出气泡,约 4 秒后自动消失,期间再次提交失败重新弹出并重新计时;验证 GUI 实测:短密钥提交 → 气泡出现 → 约 4 秒后消失,展示期间再次提交重新弹出

## 3. 回归验证

- [x] 3.1 终端模式回归:不带 `--gui` 执行一次加密与一次解密;验证终端交互与变更前一致
