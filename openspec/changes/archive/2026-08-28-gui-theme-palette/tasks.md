## 1. 主色系常量更新

- [x] 1.1 `UiConstants`:ACCENT → #AF97E5、ACCENT_BRIGHT → #B49CF4、PARTICLE_BRIGHT → #D5C7F3、PARTICLE_DIM → #8A76C4、LINK_COLOR → #9D89DC、BG_BOTTOM → #2B1A52、BUTTON_DISABLED → #5B5478,新增 `DEEP_ACCENT = #3A2A66`,验证 `mvn -q compile` 编译通过
- [x] 1.2 确认全部引用 ACCENT/ACCENT_BRIGHT 的组件(按钮/进度条/分段选择器/滚动条/选中态/拖拽脉冲/执行中状态文字/提示文字)随常量自动换色,无遗漏的独立色值定义

## 2. 硬编码深紫底收敛

- [x] 2.1 `FieldHint.BACKING`(α170)与 `ProgressPanel` RowStrip 填充(α150)改用共享常量 `DEEP_ACCENT`(保留各自 alpha),验证提示浮层与行条底色仍为协调深紫

## 3. 验收

- [x] 3.1 `mvn -q package` 打包通过;GUI 实测表单页(主按钮、分段选择器、拖拽提示)、执行页(进度条、执行中状态文字、行条、滚动条)、确认弹窗与星空背景,核对:强调组件为 #AF97E5、悬停/按下/进行中为 #B49CF4、星空与背景为同色相紫色调且无蓝色调残留、错误红/成功绿不变、白字按钮文本可读(对照 spec 中「主题强调组件着色」「星空背景配色」「语义色不变」场景)
