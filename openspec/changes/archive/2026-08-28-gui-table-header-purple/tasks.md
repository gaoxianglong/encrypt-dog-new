## 1. 表头主色紫

- [x] 1.1 `ProgressPanel.HeaderBand` 填充色由 `DEEP_ACCENT` 改为 `UiConstants.MOSAIC_PUR`(主色系四色渐变紫段,α150 不变),表头文字由 `TEXT_SECONDARY` 改为 `TEXT_PRIMARY`(近白、可读),圆角/高度不动,验证 `mvn -q compile` 编译通过

## 2. 验收

- [x] 2.1 `mvn -q package` 打包通过;GUI 实测执行页:表头背景呈主色系半透明紫、与按钮/进度条/滚动条等强调组件协调,表头文字清晰可读,其余布局无回归(对照 spec 中「列表紫色滚动条」场景)
