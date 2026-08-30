# Design: 修复 -o 模式秘钥存储链路

## Context

（动机与缺陷清单见 proposal.md - Why。）

现状代码结构：加密侧 `AbstractEncrypt.saveSecretKey` 在 `onSuccess` 中将随机密钥（UUID 字符串）用 `dataEncrypt` 加密后直接以 UTF-8 String 写入 `~/.dog/DOG-SECRET-KEY.properties`；解密侧 `AbstractDecrypt.bind` 读取文件头硬件绑定字段后调用 `restoreKey` 从同一文件恢复。`OperationVO.setSecretKey` 在 `-o` 加密时以用户密码为 `sourceSecretKey`、随机 UUID 为实际 `secretKey`。

冻结约束（硬性）：文件头布局（magic/type/iv/hid/fileId 字段与字节序）、数据加解密路径（`store`/`dataEncrypt`/`dataDecrypt` 及分块大小）、非 `-o` 全部行为、硬件绑定字段的写入与校验语义。JDK 21.0.1 实测确认：二进制密文经 UTF-8 String 往返必损坏（48 字节 → 91 字节，解密抛 `IllegalBlockSizeException`）。git 证实旧版本写入的条目从未可解，因此无旧格式迁移负担。

## Goals / Non-Goals

**Goals:**

- `-o` 链路端到端可用：加密 → 同机解密，密码错误/篡改/缺失记录全部 fail-loud
- 密钥条目完全自描述（随机盐 + 随机 nonce + 迭代数随条目存储），不依赖 `Utils.chars2Bytes` 的 JDK 实现行为
- 跨进程并发不丢条目；崩溃不损坏既有条目

**Non-Goals:**

- 不引入数据文件的算法格式 v2（不加盐到文件头、不做数据侧 GCM/连续 CBC —— 那是未来新算法 ID 的独立 change）
- 不改设备绑定采集命令与校验逻辑（仅改进其失败时的报错）
- 不做旧条目数据恢复（本就不可用，报错指引重新加密即可）

## Decisions

### D1: 条目格式 v2 —— AES-256-GCM + PBKDF2，全部材料随条目自包含

```
value = v2:<iter>:base64(salt16):base64(nonce12):base64(ct):base64(tag16)

key   = PBKDF2WithHmacSHA256(用户密码, salt, iter, 256)
ct||tag = AES-256-GCM(key, nonce, 随机密钥的UTF-8字节)
```

- **为什么 GCM 而非 CBC+HMAC**：GCM 单原语同时提供机密性与 128bit 完整性，免去派生两个密钥与组合逻辑；JCE 自带，无新依赖。备选 CBC+HMAC（截断 8 字节 MAC）同样可行但零件更多、完整性仅 64bit。
- **为什么迭代数存进条目**：未来提高迭代数时旧条目仍可解；解密侧按条目内数值执行。
- **为什么明文是随机密钥的 UTF-8 字节**：随机密钥即现有数据路径实际使用的 `secretKey`（UUID 字符串）。数据路径冻结，明文载体不变，仅存储外壳升级。
- **为什么随机 salt 而非沿用密码或文件头 IV**：自描述格式让条目派生彻底脱离 `chars2Bytes` 与文件头解析；条目级唯一盐防预计算。

### D2: 迭代次数 600_000

OWASP 2023 对 PBKDF2-HMAC-SHA256 的建议值。仅在 `-o` 加密（一次性）与解密（每文件一次）时执行，实测单次 ~0.5s 可接受。备选沿用数据路径的 65536 —— 偏弱且密钥条目是离线猜密码的主要入口，否决。

### D3: 跨进程文件锁 + 原子替换

- 独立 `DOG-SECRET-KEY.properties.lock` 文件，`FileChannel.lock()` 覆盖整个 读-改-写 周期；进程内保留 `synchronized`。
- 写入走同目录临时文件 + `Files.move(ATOMIC_MOVE, REPLACE_EXISTING)`。
- **为什么独立锁文件而非锁 properties 本身**：原子替换会使锁落在旧 inode 上，后续进程锁到新 inode，锁语义失效。备选仅 `synchronized` —— 只管单进程，跨进程丢条目风险仍在，否决。

### D4: fileId 改为随机 64bit

`UUID.randomUUID().getMostSignificantBits()` 写入文件头 8 字节，解密端读取逻辑不变。备选保留雪花（5bit worker/idc 随机 + 每重启清零 sequence）：跨重启同毫秒碰撞会覆盖他人密钥条目造成永久丢失，否决。备选 16 字节 UUID 进文件头：破坏文件头格式，否决。

### D5: fail-loud 错误分类

恢复失败区分四类可操作错误：**密码错误**（GCM tag 校验失败）/ **记录缺失**（存储文件不存在或无此 fileId 条目）/ **条目损坏**（非 v2 格式或解析失败，提示用原密码重新加密）/ **设备不匹配**（沿用现有语义）。`restoreKey` 不再静默返回。

### D6: 权限收紧为 600

临时文件以 600 权限创建，原子替换后存储文件权限自然为 600；对已存在的旧存储文件，在首次重写时同步收紧为 600。

### D7: 密钥派生每文件仅一次（性能，全模式受益）

派生结果缓存在任务级上下文（`EncryptContext`），`dataEncrypt`/`dataDecrypt` 各分块复用同一派生密钥。派生输入输出字节不变，因此对所有既有文件（含 `-o` 与非 `-o`）零格式影响；仅消除每 10MB 块重复派生的浪费（1GB 文件约省 6s）。

### D8: `createSecretkeyFile` 仅在加密路径执行

解密时不再创建空密钥文件，缺失时直接走「记录缺失」错误路径。

## Risks / Trade-offs

- [600k 迭代使每次 `-o` 解密多 ~0.5s] → 迭代数随条目存储，未来可下调或上调；每文件仅一次，批量场景影响可控。
- [回滚旧版本 jar 后，新写入的 v2 条目在旧版本下无法解析] → 表现为「条目损坏」报错，提示重新加密；条目本身可重建（原文件 + 原密码），且回滚场景罕见。
- [GCM nonce 复用] → nonce 12 字节 SecureRandom 每次独立生成；条目级随机 salt 提供双重保险，碰撞概率 ~2^-96 可忽略。
- [`.lock` 文件残留] → 空占位文件无害，无需清理逻辑；与 properties 同目录同生命周期。
- [原子替换跨文件系统失败] → 临时文件创建于目标同目录，规避跨卷 rename 限制；失败时回退直接写并输出告警日志。

## Migration Plan

- **无数据迁移**：旧条目原样保留在存储文件中（读取时被识别为损坏并给出「重新加密」指引，覆盖行为发生在用户重新加密同一文件时）。
- **部署**：直接替换 jar。首次 `-o` 加密即写入 v2 条目；非 `-o` 用户完全无感。
- **回滚**：恢复旧版本 jar 后，非 `-o` 行为不受影响；v2 条目在旧版本下报错并提示重新加密，无静默数据损坏路径。
