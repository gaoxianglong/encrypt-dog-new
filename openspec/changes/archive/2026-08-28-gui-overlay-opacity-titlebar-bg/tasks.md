## 1. 标题栏背景色

- [x] 1.1 修改 TitleBar：`paintComponent` 首行以 `UiConstants.BG_TOP` 填充整个标题栏区域（保持 `setOpaque(false)` 不变），并更新类注释；验证启动 GUI 后顶部无灰色底、标题栏与内容区渐变顶部颜色一致、无拼缝（用户目视验收通过）
- [x] 1.2 删除 UiConstants 中从未被引用的 `TITLE_BAR_BG` 常量；验证 `grep -rn "TITLE_BAR_BG" src/main/java` 无残留引用且 `mvn compile` 通过

## 2. 蒙层不透明度（验收否决，已回滚）

- [x] 2.1 曾将 `CARD_FILL` 由 alpha 20 试调至 64 供目视验收；用户否决后回滚至 alpha 20，验证 UiConstants 恢复原值且注释与原版一致

## 3. 整体验收

- [x] 3.1 回滚后 `mvn package` 构建通过；启动 GUI 确认标题栏仍为 BG_TOP、与内容区无缝，蒙层恢复原观感
