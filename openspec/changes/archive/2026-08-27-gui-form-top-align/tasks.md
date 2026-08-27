## 1. 首行与拖拽区左对齐

- [x] 1.1 `EncryptFormPanel`:modeToggle x 134→30、宽 420→524;dropPanel x 134→30、宽 420→524(右边缘 554 不变);验证 `mvn -q compile` 编译通过
- [x] 1.2 无头断言:modeToggle x=30 宽=524、dropPanel x=30 宽=524,右边缘与 secretKeyField 右边缘一致;其余控件坐标不变
- [x] 1.3 GUI 实测:Encrypt/Decrypt 切换栏与文件拖拽区左边缘与 "Secret key" 标签对齐,右边缘与输入框对齐,其余控件位置不变

## 2. 回归验证

- [x] 2.1 终端模式回归:不带 `--gui` 执行一次加密与一次解密;验证终端交互与变更前一致
