## 1. 首页干净蒙层改造

- [x] 1.1 `EncryptFormPanel`:移除主副标题(标签/常量/switchMode 文案更新);表单内容整体上移 50px(modeToggle 64→14、dropPanel 104→54、选择按钮 220→170、secretKey 252→202、确认密钥 298/306→248/256、layoutRows 基准 352/344→302/294);versionLabel 不动;验证 `mvn -q compile` 编译通过
- [x] 1.2 `GlassCardPanel`:移除 topHighlight 开关与高光绘制,蒙层常画干净半透明面;`EncryptDogFrame` 移除 setTopHighlight 两处调用;验证 `mvn -q compile` 编译通过
- [x] 1.3 无头断言:表单卡片无主副标题标签;modeToggle 位于卡片顶部(y≈14);蒙层无顶部高光带;layoutRows 上移逻辑与解密模式 offset 不变;versionLabel 位置不变
- [x] 1.4 GUI 实测:首页蒙层为干净统一半透明面(无分割线),无主副标题,首行为 Encrypt/Decrypt 切换,表单整体上移无顶部空洞;加密/解密切换联动不变;版本号仍在右下角;执行页不变

## 2. 回归验证

- [x] 2.1 终端模式回归:不带 `--gui` 执行一次加密与一次解密;验证终端交互与变更前一致
