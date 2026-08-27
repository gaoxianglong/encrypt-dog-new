## 1. 蒙层布局整合

- [x] 1.1 `EncryptDogFrame`:窗口标题栏所有页面常显(移除 setVisible 切换,不动标题栏);homeButton 定位 (12, 2) 紧贴标题栏底部;WIDE_CARD_Y=70 蒙层上移;执行页 `glassCard.setTopHighlight(false)`、表单页恢复 true;`GlassCardPanel` 增加顶部高光带开关;验证 `mvn -q compile` 编译通过
- [x] 1.2 `ProgressPanel`:内部内容整体上移(statusLabel y=10、统计行 y=32、列表 y=96),其余不动;验证无头断言:布局坐标正确、统计值与行数据不受影响
- [x] 1.3 GUI 实测:窗口标题栏(加密狗+logo)执行页保持展示不动;蒙层为干净统一半透明面,无顶部高光带与横向分割线(用户已确认蒙层问题解决);home 图标紧贴标题栏底部(约 2px 间隙);顶部可拖动窗口;返回表单页后蒙层恢复原样

## 2. 回归验证

- [x] 2.1 终端模式回归:不带 `--gui` 执行一次加密与一次解密;验证终端交互与变更前一致
