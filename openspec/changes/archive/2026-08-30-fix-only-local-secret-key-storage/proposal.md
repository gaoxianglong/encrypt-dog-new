# Proposal: 修复 -o（only-local）模式秘钥存储链路

## Why

`-o`（仅限本机）模式的解密链路当前完全不可用：真实密钥的二进制密文经 UTF-8 String 往返存入 Properties 后字节损坏（已实测：48 字节密文往返后变 91 字节、解密抛 `IllegalBlockSizeException`），`restoreKey` 在记录缺失时静默返回导致静默损坏，且无锁的 load→store 在跨进程并发加密时可能覆盖其他文件的密钥条目造成永久数据丢失。git 历史确认 `saveSecretKey` 自 `7ee0899` 引入后从未改动，即旧版本产生的 `-o` 文件也从未成功解密过，修复不存在可用数据回归。

## What Changes

- 密钥文件条目格式升级为 **v2 自描述格式**：`v2:<iter>:base64(salt16):base64(nonce12):base64(ct):base64(tag16)`，密钥密文以 Base64 存储，PBKDF2 随机盐与 nonce 随条目自包含，彻底摆脱 `Utils.chars2Bytes` 的 JDK 实现依赖
- 增加 **完整性认证**（AES-GCM 128bit 认证标签），解密前先验标签：错误密码立即报错，覆盖 XOR 等解密永不失败的算法，杜绝静默损坏
- 密钥文件写入增加 **跨进程文件锁 + 同目录临时文件原子替换**，消除并发加密丢条目与崩溃损坏全文件的风险
- fileId 由雪花算法改为**随机 64bit**（`UUID.getMostSignificantBits()`），消除跨重启碰撞覆盖他人条目的风险；文件头仍为 8 字节，读取逻辑不变
- `restoreKey` 对缺失/损坏条目 **fail-loud**：明确报错并提示处理方式（重新加密 / 恢复密钥文件），不再静默返回
- **BREAKING**（仅对旧 `-o` 条目）：旧版本写入的密钥条目为损坏数据且无法恢复，新版解密时识别为损坏条目并明确提示用原密码重新加密该文件。旧版本产生的 `-o` 密文在旧版本中同样无法解密，因此无用户可用数据受影响
- 非 `-o` 模式的加解密数据路径、文件头格式、硬件绑定逻辑**全部冻结不动**

## Capabilities

### New Capabilities

- `secret-key-storage`: 管理 `-o`（仅限本机）模式下真实密钥在 `~/.dog/DOG-SECRET-KEY.properties` 中的持久化与恢复，包括条目格式、完整性校验、并发安全、错误处理与文件权限

### Modified Capabilities

<!-- 无现有能力的需求变更；swing-gui 不受影响 -->

## Impact

- **代码**：`AbstractEncrypt.saveSecretKey`（v2 格式 + 锁 + 原子写）、`AbstractDecrypt.restoreKey`/`bind`（v2 解析 + MAC 校验 + fail-loud）、`AbstractOperationTemplate`（密钥派生每文件一次、`createSecretkeyFile` 仅加密时调用）、fileId 生成处
- **数据**：`~/.dog/DOG-SECRET-KEY.properties` 条目格式 v2，新增 `DOG-SECRET-KEY.properties.lock` 锁文件；文件权限收紧为仅属主可读写
- **不涉及**：AES/3DES/XOR 的数据加解密路径（`store`/`dataEncrypt`/`dataDecrypt`）、文件头布局、硬件绑定、CLI/GUI 参数链路。非 `-o` 旧文件零影响，需以既有文件样本做回归验证
