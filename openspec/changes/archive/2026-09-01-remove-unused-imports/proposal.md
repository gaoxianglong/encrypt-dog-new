# 删除未使用的 import

## Why

代码库 111 个 Java 文件共 775 条 import 语句,其中存在导入了包/类但代码实际未引用的情况(历史迭代遗留)。未使用 import 是死代码:干扰阅读、误导 IDE 依赖分析、违反 Java 编码规范(阿里规范:【强制】避免无用的 import)。一次性清理,零行为变更。

## What Changes

- 删除 `src/main/java` 下所有 Java 文件中"已导入但代码中未引用"的 import 语句
- 清理规则(保守,见 design.md):
  - 通配符 import(`import xxx.*;`,共 14 条)不处理(无法逐类验证,保持现状)
  - 静态 import:当前为 0 条,不涉及
  - 判定标准:import 的简单类名在 import 行之外的整个文件文本中(含 javadoc、注释、字符串字面量)从未出现 → 删除;出现即保留(宁可多留,不可误删)
  - 逐文件人工复核候选清单后再删(同名类、javadoc `{@link}` 引用等场景)
- 不改变任何行为:无代码逻辑、依赖、配置或文档变更;构建产物字节码等价

## Capabilities

### New Capabilities

(无)

### Modified Capabilities

(无——纯重构,已在 `.openspec.yaml` 声明 `skip_specs: true`)

## Impact

- 代码:预计数十个文件删减 import 行,零逻辑改动
- 验证:`mvn compile` 与 `mvn clean package` 通过、GUI 冒烟无回归
- 用户可见行为:无
