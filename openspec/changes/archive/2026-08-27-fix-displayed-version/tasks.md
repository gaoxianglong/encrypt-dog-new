## 1. 版本号与构建版本对齐

- [x] 1.1 `pom.xml`:增加 resources 过滤配置(整目录不过滤,仅 `properties/**/*.properties` 过滤);`dog.properties`:`project.version=${project.version}`;`UiConstants.VERSION` 改为运行时读取 dog.properties("v"+project.version,失败回退 vUNKNOWN);验证 `mvn -q clean package -DskipTests` 打包通过
- [x] 1.2 无头断言:打包产物 jar 内 dog.properties 的 project.version=2.0.4;UiConstants.VERSION == "v2.0.4";png 等二进制资源未被过滤损坏(jar 内文件与源文件字节一致)
- [x] 1.3 GUI 实测:首页右下角版本号显示 v2.0.4;终端启动横幅显示 version: 2.0.4

## 2. 回归验证

- [x] 2.1 终端模式回归:不带 `--gui` 执行一次加密与一次解密;验证横幅版本为 2.0.4 且加/解密 roundtrip 与变更前一致
