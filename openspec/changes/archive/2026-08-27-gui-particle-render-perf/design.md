## Context

ParticlePanel 每 16ms(60fps)在 EDT 全量重绘:①每帧 `new GradientPaint` 整窗填充 1200×800;②300 粒子的星座连线全对检测 O(n²)=4.5 万次 hypot/帧(2.7M 次/秒);③每粒子每帧 `new Color`(blendColor)与 `new Ellipse2D.Double`(约 1.8 万次分配/秒,GC 压力)。用户实测约 140% 单核 CPU。动机见 proposal.md「Why」。

## Goals / Non-Goals

**Goals:**

- 空闲 CPU 显著下降(目标:实测对比明显低于 140%)。
- 动画与交互效果完全不变(同帧率、同粒子数、同连线规则、同交互)。

**Non-Goals:**

- 不降帧率(保持 16ms)、不减粒子数、不弱化连线/鼠标/爆发效果。
- 不改其他页面与终端模式。

## Decisions

### D1: 背景渐变预渲染缓存

构造/尺寸变化时把 BG_TOP→BG_BOTTOM 渐变渲染进 BufferedImage 缓存;每帧 `drawImage` 一次贴绘。面板 setBounds 变化时重建缓存。备选:保留每帧渐变填充 → 丢弃(主要开销之一)。

### D2: 连线检测网格化(视觉等价)

单元边长 = LINK_DISTANCE,每帧把粒子登记进网格桶,每个粒子仅与同单元及 8 邻域粒子做距离检测(平方距离预判,仅连线时开方)。所有距离 < LINK_DISTANCE 的粒子对必在同单元或相邻单元内,连线结果与全对检测完全一致。备选:降 LINK_DISTANCE 或减连线数量 → 改变视觉,放弃。

### D3: 零每帧分配绘制

- 粒子颜色:预计算 64 级亮度调色板 `Color[64]`(PARTICLE_DIM↔PARTICLE_BRIGHT 插值),闪烁相位映射为桶索引,替代每帧 blendColor。
- 连线颜色:预计算 `Color[LINK_MAX_ALPHA+1]` alpha 阶梯调色板,距离映射索引。
- 几何对象:共享单个 Ellipse2D.Double 实例,逐粒子改写坐标绘制。
- 网格桶:固定 int 数组复用,不每帧新建集合对象。

### D4: 动画状态机与帧率不变

Timer 16ms、STATE_NORMAL/GATHERING/BURSTING 逻辑、drift/repelFromMouse/wrap、triggerBurst 全部不动,仅替换绘制与检测实现。

### D5: 粒子精灵贴绘(替代共享几何填充)

jstack 实测:MTL 下每帧 300 次 AA 椭圆填充(AAShapePipe 掩码生成)与 setColor 管线冲刷是主要渲染开销之一。改为(半径桶×亮度桶)→预渲染 AA 椭圆精灵缓存,逐帧仅 drawImage 贴绘;半径 0.1 步长量化,视觉与像素对比一致(容差内)。共享 Ellipse2D 方案取消。

### D6: 图标加载缓存(LogoUtil)

jstack 实测发现 DropFilePanel.paintComponent 每次重绘调用 LogoUtil.loadImage——PNG 解码+alpha 扫描+双三次缩放每帧重复执行(60fps 级联重绘放大)。LogoUtil 增加 (resource@size)→ImageIcon 并发缓存,全部调用点受益,视觉不变。

### D7: 60fps 固定开销说明(用户决策)

分层窗格中粒子面板与毛玻璃卡片重叠,Swing 对重叠组件每帧整窗重绘(粒子重绘带动卡片树 60 次/秒),该管线开销与单帧绘制优化无关。已向用户呈现实测数据(纯优化后 CPU 117.9%→109.7%,渲染耗时-28%),用户选择保持 60fps 不变,接受该固定开销;不降帧率、不做局部脏区重绘。

## Risks / Trade-offs

- [网格化影响连线正确性] → 单元边长=连线距离保证等价;任务中以"优化前后连线集合一致"做无头断言。
- [背景缓存与尺寸变化] → 尺寸变化重建;执行页宽屏切换会触发重建,仅一次性开销。
- [优化后 CPU 仍偏高] → 以实测为准;若仍不满意,降帧率作为后续变更(不在本变更范围)。

## Migration Plan

纯渲染实现替换,无数据迁移;回滚即恢复原 paintLinks/paintBackground/paintParticles。
