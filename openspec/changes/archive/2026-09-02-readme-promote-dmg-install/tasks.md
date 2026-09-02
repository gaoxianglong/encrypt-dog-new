## 1. README 安装节重写

- [x] 1.1 `### install` 重写为三段式：`macOS App (recommended)` 置顶主推（wget 下载 `EncryptDog-2.0.5.dmg` → 打开 dmg 拖入 Applications → 双击启动即进入 GUI）；`jar (also supported)` 保留（wget `encryptdog-2.0.5.jar` + `dog` 别名，GUI/终端两用）；`build from source` 保留（git clone + mvn package + alias）。验证：install 节以 dmg 为第一推荐方式，三段均存在，dmg 链接为 v2.0.5 Release 真实资产地址
- [x] 1.2 `### gui mode` 开头补一句说明：dmg 安装用户直接启动 .app 即进入 GUI，`--gui` 启动命令适用于 jar 形态。验证：gui mode 节提及 dmg 路径且 `--gui` 命令示例完整保留
- [x] 1.3 删除 `### gui mode` 中冗长的 Interface guide 整段（10 条 bullet：Mode/Source files/Secret key/Algorithm/Target directory/Options/Validation/Execution/Back/Menu bar tray），GUI 使用说明不再逐字段罗列。验证：README 中不存在 Interface guide 段落与上述 bullet，`### terminal mode` 及其后内容完整

## 2. 验证

- [x] 2.1 校验两个下载链接可用（v2.0.5 的 dmg 与 jar，HTTP 200 或 302）；grep 确认 README 中 jar/源码构建内容保留且整体结构符合 proposal。验证：`curl -sIL -o /dev/null -w "%{http_code}"` 两链接均非 4xx/5xx，README 结构审查通过
