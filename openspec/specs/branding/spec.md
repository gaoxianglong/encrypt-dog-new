# branding Specification

## Purpose

规定应用品牌名称为 EncryptDog 及其在标题栏、CLI 命令名、构建产物与 README 文档中的统一呈现，杜绝旧名混用。

## Requirements

### Requirement: 品牌名称统一

系统的规范品牌名称 SHALL 为 "EncryptDog"，窗口标题栏与品牌相关界面 SHALL 展示该名称，SHALL NOT 展示旧名 "EncryptionDog"。

#### Scenario: 标题栏品牌名称

- **WHEN** 用户启动 GUI 查看窗口标题栏
- **THEN** 标题栏显示品牌名称 "EncryptDog"，不出现 "EncryptionDog"

### Requirement: 终端 banner 品牌名

终端模式启动时输出的 ASCII banner SHALL 拼写品牌名 "EncryptDog"（与标题栏、CLI 命令名一致），SHALL NOT 拼写旧名 "EncryptionDog"。

#### Scenario: 启动 banner 拼写新名

- **WHEN** 用户运行 `java -jar encryptdog-2.0.6.jar -h` 查看启动输出
- **THEN** banner 艺术字拼写 "EncryptDog"，不呈现 "EncryptionDog" 形态

### Requirement: CLI 命令名

CLI SHALL 以 `encryptdog` 作为命令名呈现于 usage 与帮助输出，SHALL NOT 呈现旧命令名 `encrypt-dog`。

#### Scenario: 帮助输出命令名

- **WHEN** 用户运行 `java -jar encryptdog-2.0.6.jar -h` 查看帮助
- **THEN** usage 首行以 `Usage: encryptdog` 开头，输出中不出现 `encrypt-dog`

### Requirement: 构建产物命名

Maven 构建 SHALL 产出 jar `encryptdog-<version>.jar`（如 `encryptdog-2.0.6.jar`），macOS 打包 SHALL 产出 `EncryptDog.app` 与 `EncryptDog-<version>.dmg`；SHALL NOT 产出 `dog-<version>.jar` 命名的 jar。

#### Scenario: 构建产物名称

- **WHEN** 执行 `mvn package` 与 `build-mac.sh` 打包
- **THEN** `target/` 下存在 `encryptdog-2.0.6.jar` 且不存在 `dog-2.0.6.jar`；`dist/` 下存在 `EncryptDog.app` 与 `EncryptDog-2.0.6.dmg`

### Requirement: 文档品牌引用

README 中应用品牌名 SHALL 统一为 EncryptDog，启动示例中的 jar 引用 SHALL 为 `encryptdog-<version>.jar`。

#### Scenario: README 品牌一致

- **WHEN** 用户浏览 README 的品牌说明与启动命令示例
- **THEN** 品牌名均为 EncryptDog、jar 引用均为 `encryptdog-<version>.jar`，无 "EncryptionDog" 与 `dog-<version>.jar` 残留（指向上游仓库 `encryption-dog` 的 logo 外链除外）
