# Design: 删除未使用的 import

## Context

动机见 proposal.md - Why。现状:111 个 Java 文件、775 条 import(其中 14 条通配符)、0 条静态 import;无 src/test。Java 编译器不报告未使用 import(合法语法),IDE 检查不可用,需自建保守的检测流程。

## Goals / Non-Goals

**Goals:**

- 删除全部"确定未引用"的 import,零行为变更、编译通过

**Non-Goals:**

- 不通配符 import 开刀(无法逐类验证);不调整 import 排序/格式;不引入 checkstyle 等新工具链(一次性清理不值得)

## Decisions

### D1: 脚本检测 + 保守判定

Python 脚本扫描 `src/main/java/*.java`:提取每条非通配符 import 的简单类名,在整个文件文本(去掉 import 行自身)中做词边界搜索——任何位置(代码、javadoc、注释、字符串字面量)出现即判"使用"并保留;从未出现才进入候选清单。该规则只会误留、不会误删。

- 备选:引入 checkstyle UnusedImports 检查 → 否决,为一次性清理加构建插件与配置,遗留维护成本

### D2: 候选清单人工复核

脚本产出清单后逐项复核,重点排查:

- 同名类冲突场景(如 `java.awt.List` vs `java.util.List`,文件正文用全限定名时 import 实际多余或反之)
- javadoc `{@link}` / `{@code}` 中的引用(已在判定内,复核确认)
- 注解处理器隐式依赖(如 Lombok 生成的代码引用 import,字面不出现 → 极少数,复核时保留)

### D3: 逐文件删除,编译门禁

按复核后的最终清单逐文件删除 import 行;每批删除后立即 `mvn compile`,编译失败即回退该文件(误删暴露无遗)。

### D4: 验收

`mvn clean package` 全量构建 + GUI 冒烟(启动、表单、执行页往返)+ 残留核对(脚本复跑,候选清单应为空)。

## Risks / Trade-offs

- **[R1] 误删导致编译失败** → D3 编译门禁,失败即回退该文件
- **[R2] 注解处理器/Lombok 隐式引用被误判未使用** → D2 人工复核兜底
- **[R3] 通配符 import 下隐藏的未使用依赖** → 明确不在范围内,记录为已知限制
- **[R4] 误留(保守判定的代价)** → 接受;本次目标是零误删,残余可后续 IDE 再清
