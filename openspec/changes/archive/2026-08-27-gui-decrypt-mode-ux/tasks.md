## 1. 标题/副标题随模式联动

- [x] 1.1 将 `taskTitleLabel`、`subtitleLabel` 从构造方法局部变量提升为字段(字号与颜色样式初始化保留在构造方法),验证 `mvn -q compile` 编译通过且启动 GUI 后加密模式标题文案与变更前一致
- [x] 1.2 在 `switchMode` 中按模式设置标题文案:Encrypt 为 "Encrypt files" / "Protect your files with local encryption",Decrypt 为 "Decrypt files" / "Restore your files with local decryption";验证点击 Decrypt 后标题副标题立即切换,切回 Encrypt 恢复,标题块位置不动

## 2. 解密模式动态重排

- [x] 2.1 新增面板私有常量 `ROW_PITCH = 46`,新增 `layoutRows(boolean isEncrypt)` 集中设置 Algorithm 标签/下拉框/XOR 提示、Target directory 标签/输入组、两个复选框、错误提示与主按钮的 y 坐标(解密偏移 `-ROW_PITCH`),构造方法中这些控件的 bounds 改由 `layoutRows(true)` 初始化;验证编译通过,加密模式界面与变更前逐像素一致(对照变更前截图)
- [x] 2.2 将 `targetBox` 提升为字段,保存 Algorithm、Target directory 两个标签的引用供 `layoutRows` 使用;验证 Browse 按钮随输入组移动、两个标签与各自字段对齐
- [x] 2.3 在 `switchMode` 末尾调用 `layoutRows(isEncrypt)` 并执行 `revalidate()` + `repaint()`;验证点击 Decrypt 后确认密钥行隐藏且下方模块整体上移,Secret key 与 Algorithm 间距同加密模式相邻字段一致、无空洞,Encrypt/Decrypt 来回切换各 3 次无残留绘制
- [x] 2.4 在 `layoutRows` 与确认密钥字段的 javadoc 中标注约定「确认密钥行下方的控件必须登记到 layoutRows」;验证代码评审确认注释与实现一致

## 3. 回归验证

- [x] 3.1 验证预填路径:通过无头验证类 PanelVerify 断言 `prefill(isEncrypt=false)` 后标题/布局/按钮均为解密态;GUI 启动冒烟以 `--gui` 参数启动无异常。(注:Starter 的 GUI 参数解析仅支持 `-e` 显式加密,`-d` 为删除源文件,并无解密预填参数——任务原按 `--gui -d` 呈现解密态编写,与代码事实不符,已按可验证口径改写)
- [x] 3.2 以解密模式走通一次完整操作(选择 .dog 文件、填写密钥、提交、确认弹窗、进度、结果页);验证 XOR 提示不显示、错误提示与主按钮位置正确、结果面板正常展示
- [x] 3.3 终端模式回归:不带 `--gui` 执行一次加密与一次解密;验证终端交互与变更前一致
