## 1. 资源引入

- [x] 1.1 用 `sips -z 64 64` 将 `/Users/johngao/Downloads/3.1 返回.png` 缩放复制为 `src/main/resources/back.png`,删除 `src/main/resources/home-fill.png`,验证 back.png 存在且为 64×64 PNG

## 2. 引用切换

- [x] 2.1 `UiConstants` 删除 `HOME_RESOURCE`、新增 `BACK_RESOURCE = "back.png"`;`EncryptDogFrame` homeButton 图标加载改用 `BACK_RESOURCE`,验证 `mvn -q compile` 编译通过且无 `HOME_RESOURCE` 残留引用

## 3. 验收

- [x] 3.1 `mvn -q package` 打包通过;GUI 实测执行页:返回按钮显示新的左向返回箭头图标、位于标题栏底部左侧、点击返回表单页正常、图标清晰无锯齿(对照 spec 中「进入即展示三层布局」场景)
