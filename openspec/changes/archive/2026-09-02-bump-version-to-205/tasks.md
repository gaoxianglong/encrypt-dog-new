## 1. 硬编码版本文案更新

- [x] 1.1 README.md 更新 7 处 `2.0.4` → `2.0.5`：版本 badge、两处 `dog` 别名示例、wget 下载链接（注意：v2.0.5 的 GitHub Release 需在发布时真实存在）、两条 GUI 启动命令示例、终端 banner 输出示例。验证：`grep -n "2\.0\.4" README.md` 无输出
- [x] 1.2 `openspec/specs/branding/spec.md` 更新 4 处示例文案为 2.0.5（「终端 banner 品牌名」「CLI 命令名」场景的 WHEN 命令示例、jar/dmg 产物需求文本与 THEN 示例）。验证：该文件内 `grep -n "2\.0\.4"` 无输出
- [x] 1.3 `openspec/specs/swing-gui/spec.md`「版本号与构建版本一致」场景示例文案更新为 2.0.5。验证：该文件内 `grep -n "2\.0\.4"` 无输出

## 2. 全局验证

- [x] 2.1 全量扫描（排除 `openspec/changes/archive/` 与 `target/`）确认无 `2.0.4` 残留；`mvn -DskipTests package` 重新打包后运行 `java -jar target/encryptdog-2.0.5.jar -h` 确认 banner 展示 `version: 2.0.5`，GUI 版本展示为 v2.0.5。验证：扫描干净且打包产物名与运行时版本展示均为 2.0.5
