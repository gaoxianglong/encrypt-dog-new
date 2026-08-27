# Tasks: 标题字号与 logo 尺寸协调

## 1. 字号与尺寸调整

- [x] 1.1 `UiConstants` 常量更名 `LOGO_FONT_SIZE`→`TASK_TITLE_FONT_SIZE` 且值 26→22，`EncryptFormPanel` 引用同步，验证任务标题 bounds (30,7,400,34) 不变（头部块居中几何零变化）且 22pt 正常渲染（无屏渲染: 字体 22 bold=品牌同号 ✓、bounds 不变 ✓、标题带 3776 px ✓）
- [x] 1.2 `UiConstants.LOGO_TITLE_SIZE` 24→28，验证标题栏 logo 28px 渲染、与品牌名称同线垂直居中（无屏渲染像素验证）（无屏渲染: logo 区 28×28 = 324 px ✓；BoxLayout 居中由组件树 bounds 保证）

## 2. 回归

- [x] 2.1 `mvn package` 重新打包，GUI 无屏渲染冒烟（标题/副标题/间距带/切换栏/标题栏 logo 全部正常），终端回归套件（AES/XOR/DESede 往返、多文件、目录递归等）全部通过（jar 18:38 重建; GUI 冒烟=任务1.1/1.2卡片单独无屏渲染全过; 终端回归 11/11 通过 ✓）
