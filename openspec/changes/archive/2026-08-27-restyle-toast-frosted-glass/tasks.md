## 1. 错误锚点

- [x] 1.1 新增 `GuiAnchor` 枚举,`GuiException` 增加 anchor 字段与构造器(缺省 SYSTEM,原构造器兼容);`EncryptOperationAppService.validate` 按规则设置锚点,`prepareOperation` 将 parseFiles 异常重抛为 FILE 锚点;验证 `mvn -q compile` 编译通过
- [x] 1.2 `EncryptFormPanel.showError(String, GuiAnchor)` 按锚点组件定位气泡,`EncryptDogFrame` 调用点传入锚点;验证无头断言:密钥错误→密钥框上方、不一致→确认框上方、文件错误→拖拽区上方、算法错误→下拉框上方、系统错误→主按钮上方,解密模式下密钥/算法锚点随布局上移

## 2. 内联提示组件

- [x] 2.1 新建 `FieldHint`:主题紫文字+半透明深色背衬,展示 1.2s 后淡出 0.8s(总时长约 2s),重复 showHint 重新计时;删除 `ToastBubble.java`;验证 `mvn -q compile` 编译通过
- [x] 2.2 `EncryptFormPanel` 以 `fieldHint` 替换 `toastBubble`:密钥/确认密钥/算法→字段内部(预留眼睛按钮/下拉箭头宽度),文件→拖拽区内居中,系统/执行失败→主按钮上方;验证无头断言各锚点提示位于对应字段内部且可见,dismiss 后不可见,解密模式算法锚点随布局上移

## 3. 回归验证

- [x] 3.1 GUI 实测:短密钥→提示出现在 Secret key 框内(主题紫、非红色),约 2 秒逐步淡出消失;不同字段错误提示位置正确切换
- [x] 3.2 终端模式回归:不带 `--gui` 执行一次加密与一次解密;验证终端交互与变更前一致
