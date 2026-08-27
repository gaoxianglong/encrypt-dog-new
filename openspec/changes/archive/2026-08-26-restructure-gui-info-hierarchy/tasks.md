# Tasks: Restructure GUI Info Hierarchy

## 1. 卡片头部重构

- [x] 1.1 `EncryptFormPanel` 移除头部 logo/品牌标题/版本号，新增任务标题 "Encrypt files"（30,14,400,34、26pt 加粗、TEXT_PRIMARY）与副标题 "Protect your files with local encryption"（30,48,420,16、13pt、TEXT_SECONDARY），验证副标题底边 64 与分段选择器紧接、下方模块几何零变化（真窗截图:任务标题 2675 签名像素、副标题 478 像素,下方模块几何零变化 ✓）
- [x] 1.2 版本号移至卡片右下角（430,578,134,14、11pt、TEXT_SECONDARY、右对齐），验证与主按钮/错误提示无重叠、视觉低调（真窗截图:版本区 106 签名像素 + ASCII 字形确认 "v2.0.3-RELEASE" 渲染于右下角;早期"0 像素"为离屏双缓冲陈旧 blit 与窗口坐标过期所致假信号,Control 对照实验证明 label 单独绘制 1196 像素正常 ✓）

## 2. 标题栏与回归

- [x] 2.1 `UiConstants.LOGO_TITLE_SIZE` 22→24，验证标题栏 logo 与 EncryptionDog 同线居中（BoxLayout 已保证）、尺寸协调（真窗截图:标题栏 logo 35 像素高、与标题同线 ✓）
- [x] 2.2 `mvn package` 重新打包，GUI 冒烟验证三级层级（品牌/任务/操作）观感、表单交互无回归、终端模式不受影响（GUI 冒烟: 标题栏品牌/任务标题/副标题/版本号四级区域像素验证全过; 终端回归: AES/XOR/DESede 往返、多文件、目录递归、缺省算法、缺省解密模式、通配符 11/11 通过 ✓）
