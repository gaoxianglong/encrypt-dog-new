# Tasks: 修复标题栏 logo 资源加载

## 1. 资源路径与缩放逻辑

- [x] 1.1 `UiConstants.LOGO_RESOURCE` 由磁盘当前值 `"file-empty.png"` 恢复为 `"logo.png"`，验证 `FILE_EMPTY_RESOURCE` 未受影响（已恢复为 "logo.png"，`FILE_EMPTY_RESOURCE="file-empty.png"` 独立不变 ✓）
- [x] 1.2 `LogoUtil.loadImage` 由 `getScaledInstance(size,size)` 改为等比缩放适配（scale=min(size/w,size/h)，图标 ≤ 槽位、居中显示），验证缩放后宽高比与原图一致（apply 中补充: alpha bbox 裁边 + drawImage 同步缩放，规避透明边距挤压与 getScaledInstance 异步残缺；icon 28×24 aspect=1.167≈1133/976=1.161 ✓）

## 2. 打包与回归

- [x] 2.1 `mvn package` 重建 jar，验证新 logo.png（1164070 字节）打入 jar，无屏渲染验证标题栏 logo 区出现新图签名（近白高 alpha 像素）且渲染图标 alpha bbox 宽高比 ≈1.16（不变形）（渲染近白 74 px、白像素 bbox 23×22 与 PIL 基准 81 px/23×22 一致，居中 ✓）
- [x] 2.2 终端回归套件（AES/XOR/DESede 往返、多文件、目录递归等）全部通过（11/11 ✓）
