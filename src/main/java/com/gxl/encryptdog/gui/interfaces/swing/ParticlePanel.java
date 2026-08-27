/*
 *
 *  * Copyright 2019-2119 gao_xianglong@sina.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.gxl.encryptdog.gui.interfaces.swing;

import com.gxl.encryptdog.gui.interfaces.swing.constant.UiConstants;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 粒子背景层：深空渐变、漂浮粒子、星座连线、鼠标微扰与聚集爆发动画。
 * 全部动画由 Swing Timer 在 EDT 上驱动（约 60fps）。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/25 18:00
 */
public class ParticlePanel extends JPanel {
    /**
     * 粒子速度阻尼系数（每帧）
     */
    private static final double VELOCITY_DAMPING   = 0.96;
    /**
     * 鼠标推动速度上限（像素/帧）
     */
    private static final double MOUSE_PUSH_MAX     = 2.4;
    /**
     * 爆发速度相对距离的系数
     */
    private static final double BURST_SPEED_FACTOR = 0.06;
    /**
     * 爆发速度随机抖动上限
     */
    private static final double BURST_SPEED_JITTER = 0.8;
    /**
     * 爆发初始距离下限（避免中心粒子静止）
     */
    private static final double MIN_BURST_DISTANCE = 1.0;
    /**
     * 聚集速度系数（相对距离的比例）
     */
    private static final double GATHER_SPEED_FACTOR = 0.05;
    /**
     * 闪烁周期（毫秒）
     */
    private static final double TWINKLE_PERIOD_MS  = 1200.0;
    /**
     * 距离平方下限（避免除零）
     */
    private static final double EPSILON            = 1e-9;
    /**
     * 完整圆周弧度
     */
    private static final double FULL_TURN          = Math.PI * 2.0;
    /**
     * 状态：正常漂移
     */
    private static final int    STATE_NORMAL       = 0;
    /**
     * 状态：聚集
     */
    private static final int    STATE_GATHERING    = 1;
    /**
     * 状态：爆发飞散
     */
    private static final int    STATE_BURSTING     = 2;
    /**
     * 粒子列表
     */
    private final List<Particle> particles = new ArrayList<>(UiConstants.PARTICLE_COUNT);
    /**
     * 随机数生成器
     */
    private final Random         random    = new Random();
    /**
     * 动画计时器
     */
    private final Timer          animationTimer;
    /**
     * 当前鼠标位置（本面板坐标系），null 表示鼠标不在窗口内
     */
    private Point                mousePoint;
    /**
     * 当前动画状态
     */
    private int                  animationState = STATE_NORMAL;
    /**
     * 状态切换时间戳
     */
    private long                 stateStartMs;
    /**
     * 爆发中心
     */
    private Point                burstCenter;
    /**
     * 背景渐变缓存(尺寸变化时重建,每帧仅贴绘)
     */
    private BufferedImage        backgroundCache;
    /**
     * 缓存宽度(尺寸变化判定)
     */
    private int                  cacheWidth          = -1;
    /**
     * 缓存高度(尺寸变化判定)
     */
    private int                  cacheHeight         = -1;
    /**
     * 连线网格桶表头(复用固定数组,避免每帧分配)
     */
    private int[]                cellHead;
    /**
     * 连线网格桶链表后继指针(复用固定数组)
     */
    private int[]                cellNext;
    /**
     * 粒子精灵缓存:(半径桶×亮度桶)→预渲染AA椭圆图像,逐帧仅贴绘,
     * 避免每帧300次AA路径填充与setColor管线冲刷(MTL下主要CPU开销)
     */
    private final BufferedImage[][] particleSprites  = new BufferedImage[SPRITE_RADIUS_BUCKETS][SPRITE_BRIGHTNESS_BUCKETS];
    /**
     * 精灵半径桶数(半径0.7..2.2,步长0.1)
     */
    private static final int      SPRITE_RADIUS_BUCKETS = 16;
    /**
     * 精灵亮度桶数(与调色板一致)
     */
    private static final int      SPRITE_BRIGHTNESS_BUCKETS = 64;
    /**
     * 精灵半径步长
     */
    private static final double   SPRITE_RADIUS_STEP  = 0.1;
    /**
     * 精灵内边距(AA外扩留白)
     */
    private static final int      SPRITE_INSET        = 1;
    /**
     * 粒子亮度调色板(64级,PARTICLE_DIM↔PARTICLE_BRIGHT插值)
     */
    private static final Color[] PARTICLE_PALETTE    = buildParticlePalette();
    /**
     * 连线透明度调色板(alpha 0..LINK_MAX_ALPHA)
     */
    private static final Color[] LINK_PALETTE        = buildLinkPalette();

    public ParticlePanel() {
        setOpaque(false);
        initParticles();
        // 每帧回调：推进状态机、更新粒子、重绘
        animationTimer = new Timer(UiConstants.FRAME_DELAY_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tick();
            }
        });
        animationTimer.start();
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                setMousePoint(e.getPoint());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setMousePoint(null);
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    /**
     * 触发粒子聚集→爆发动画（幂等：动画进行中时忽略）。
     */
    public void triggerBurst() {
        if (animationState != STATE_NORMAL) {
            return;
        }
        animationState = STATE_GATHERING;
        stateStartMs = System.currentTimeMillis();
        burstCenter = new Point(getWidth() / 2, getHeight() / 2);
    }

    /**
     * 设置鼠标位置（供上层卡片转发），超出边界视为离开。
     *
     * @param point 鼠标位置（本面板坐标系）
     */
    public void setMousePoint(Point point) {
        if (point == null || point.x < 0 || point.y < 0 || point.x > getWidth() || point.y > getHeight()) {
            mousePoint = null;
        } else {
            mousePoint = point;
        }
    }

    /**
     * 停止动画计时器（窗口销毁时调用）。
     */
    public void stop() {
        animationTimer.stop();
    }

    /**
     * 初始化粒子。
     */
    private void initParticles() {
        for (int i = 0; i < UiConstants.PARTICLE_COUNT; i++) {
            particles.add(Particle.create(random, UiConstants.WINDOW_WIDTH,
                    UiConstants.WINDOW_HEIGHT - UiConstants.TITLE_BAR_HEIGHT));
        }
    }

    /**
     * 每帧回调：推进状态机、更新粒子、重绘。
     */
    private void tick() {
        updateState();
        updateParticles();
        repaint();
    }

    /**
     * 推进爆发动画状态机。
     */
    private void updateState() {
        long elapsed = System.currentTimeMillis() - stateStartMs;
        if (animationState == STATE_GATHERING && elapsed >= UiConstants.BURST_GATHER_MS) {
            switchToBursting();
        } else if (animationState == STATE_BURSTING
                && elapsed >= UiConstants.BURST_GATHER_MS + UiConstants.BURST_FLY_MS) {
            animationState = STATE_NORMAL;
        }
    }

    /**
     * 聚集阶段结束：为粒子赋予向外飞散的爆发速度。
     */
    private void switchToBursting() {
        animationState = STATE_BURSTING;
        stateStartMs = System.currentTimeMillis();
        for (Particle particle : particles) {
            double dx = particle.x - burstCenter.x;
            double dy = particle.y - burstCenter.y;
            double distance = Math.max(Math.hypot(dx, dy), MIN_BURST_DISTANCE);
            double speed = distance * BURST_SPEED_FACTOR + random.nextDouble() * BURST_SPEED_JITTER;
            particle.vx = dx / distance * speed;
            particle.vy = dy / distance * speed;
        }
    }

    /**
     * 按当前状态更新所有粒子。
     */
    private void updateParticles() {
        int width = getWidth();
        int height = getHeight();
        for (Particle particle : particles) {
            if (animationState == STATE_GATHERING) {
                moveTowardCenter(particle);
            } else if (animationState == STATE_BURSTING) {
                particle.x += particle.vx;
                particle.y += particle.vy;
            } else {
                drift(particle, width, height);
            }
        }
    }

    /**
     * 粒子向爆发中心聚集。
     *
     * @param particle 粒子
     */
    private void moveTowardCenter(Particle particle) {
        particle.x += (burstCenter.x - particle.x) * GATHER_SPEED_FACTOR;
        particle.y += (burstCenter.y - particle.y) * GATHER_SPEED_FACTOR;
    }

    /**
     * 粒子正常漂移：基础速度 + 扰动速度阻尼衰减 + 鼠标微扰 + 边界回绕。
     *
     * @param particle 粒子
     * @param width    面板宽度
     * @param height   面板高度
     */
    private void drift(Particle particle, int width, int height) {
        particle.x += particle.baseVx + particle.vx;
        particle.y += particle.baseVy + particle.vy;
        particle.vx *= VELOCITY_DAMPING;
        particle.vy *= VELOCITY_DAMPING;
        repelFromMouse(particle);
        wrap(particle, width, height);
    }

    /**
     * 鼠标附近的粒子受到轻微排斥。
     *
     * @param particle 粒子
     */
    private void repelFromMouse(Particle particle) {
        if (mousePoint == null) {
            return;
        }
        double dx = particle.x - mousePoint.x;
        double dy = particle.y - mousePoint.y;
        double distanceSquared = dx * dx + dy * dy;
        double radiusSquared = (double) UiConstants.MOUSE_INFLUENCE_RADIUS * UiConstants.MOUSE_INFLUENCE_RADIUS;
        if (distanceSquared < EPSILON || distanceSquared >= radiusSquared) {
            return;
        }
        double distance = Math.sqrt(distanceSquared);
        double push = UiConstants.MOUSE_PUSH_STRENGTH
                * (1.0 - distance / UiConstants.MOUSE_INFLUENCE_RADIUS);
        particle.vx += dx / distance * Math.min(push, MOUSE_PUSH_MAX);
        particle.vy += dy / distance * Math.min(push, MOUSE_PUSH_MAX);
    }

    /**
     * 边界回绕：粒子从一侧离开后从另一侧进入。
     *
     * @param particle 粒子
     * @param width    面板宽度
     * @param height   面板高度
     */
    private void wrap(Particle particle, int width, int height) {
        if (particle.x < 0) {
            particle.x += width;
        } else if (particle.x > width) {
            particle.x -= width;
        }
        if (particle.y < 0) {
            particle.y += height;
        } else if (particle.y > height) {
            particle.y -= height;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintBackground(g2d);
        paintLinks(g2d);
        paintParticles(g2d);
        g2d.dispose();
    }

    /**
     * 绘制深空渐变背景(渐变预渲染为缓冲图像,尺寸不变时每帧仅贴绘)。
     *
     * @param g2d 绘图上下文
     */
    private void paintBackground(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();
        if (backgroundCache == null || cacheWidth != width || cacheHeight != height) {
            backgroundCache = new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_RGB);
            Graphics2D bg = backgroundCache.createGraphics();
            bg.setPaint(new GradientPaint(0, 0, UiConstants.BG_TOP, 0, height, UiConstants.BG_BOTTOM));
            bg.fillRect(0, 0, width, height);
            bg.dispose();
            cacheWidth = width;
            cacheHeight = height;
        }
        g2d.drawImage(backgroundCache, 0, 0, null);
    }

    /**
     * 绘制近距离粒子对之间的星座连线（透明度随距离衰减）。
     * 网格化邻域检测:单元边长=LINK_DISTANCE,任何距离<LINK_DISTANCE的粒子对必在同单元或相邻单元,
     * 连线结果与全对检测完全一致,仅检测量由O(n²)降为O(n·邻域)。
     *
     * @param g2d 绘图上下文
     */
    private void paintLinks(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();
        int cols = Math.max(1, width / UiConstants.LINK_DISTANCE + 1);
        int rows = Math.max(1, height / UiConstants.LINK_DISTANCE + 1);
        int cells = cols * rows;
        if (cellHead == null || cellHead.length < cells) {
            cellHead = new int[cells];
        }
        if (cellNext == null || cellNext.length < particles.size()) {
            cellNext = new int[particles.size()];
        }
        Arrays.fill(cellHead, 0, cells, -1);
        // 粒子登记进网格桶
        for (int i = 0; i < particles.size(); i++) {
            Particle particle = particles.get(i);
            int col = Math.min(cols - 1, (int) (particle.x / UiConstants.LINK_DISTANCE));
            int row = Math.min(rows - 1, (int) (particle.y / UiConstants.LINK_DISTANCE));
            int cell = row * cols + col;
            cellNext[i] = cellHead[cell];
            cellHead[cell] = i;
        }
        double maxDistSq = (double) UiConstants.LINK_DISTANCE * UiConstants.LINK_DISTANCE;
        for (int i = 0; i < particles.size(); i++) {
            Particle first = particles.get(i);
            int col = Math.min(cols - 1, (int) (first.x / UiConstants.LINK_DISTANCE));
            int row = Math.min(rows - 1, (int) (first.y / UiConstants.LINK_DISTANCE));
            for (int dr = -1; dr <= 1; dr++) {
                int nr = row + dr;
                if (nr < 0 || nr >= rows) {
                    continue;
                }
                for (int dc = -1; dc <= 1; dc++) {
                    int nc = col + dc;
                    if (nc < 0 || nc >= cols) {
                        continue;
                    }
                    int neighborCell = nr * cols + nc;
                    for (int j = cellHead[neighborCell]; j != -1; j = cellNext[j]) {
                        if (j <= i) {
                            continue;
                        }
                        Particle second = particles.get(j);
                        double dx = first.x - second.x;
                        double dy = first.y - second.y;
                        double distSq = dx * dx + dy * dy;
                        if (distSq >= maxDistSq) {
                            continue;
                        }
                        int alpha = (int) (UiConstants.LINK_MAX_ALPHA
                                * (1.0 - Math.sqrt(distSq) / UiConstants.LINK_DISTANCE));
                        g2d.setColor(LINK_PALETTE[alpha]);
                        g2d.drawLine((int) Math.round(first.x), (int) Math.round(first.y),
                                (int) Math.round(second.x), (int) Math.round(second.y));
                    }
                }
            }
        }
    }

    /**
     * 绘制粒子（亮度随时间闪烁,使用预渲染精灵贴绘,颜色与几何与AA椭圆填充视觉一致）。
     *
     * @param g2d 绘图上下文
     */
    private void paintParticles(Graphics2D g2d) {
        long now = System.currentTimeMillis();
        for (Particle particle : particles) {
            double twinkle = (Math.sin(now * FULL_TURN / TWINKLE_PERIOD_MS + particle.twinklePhase) + 1.0) / 2.0;
            // 亮度区间与原实现一致(MIN_BRIGHTNESS..MAX_BRIGHTNESS映射到调色板)
            double brightness = UiConstants.PARTICLE_MIN_BRIGHTNESS
                    + twinkle * (UiConstants.PARTICLE_MAX_BRIGHTNESS - UiConstants.PARTICLE_MIN_BRIGHTNESS);
            int bucket = (int) (brightness * (PARTICLE_PALETTE.length - 1) + 0.5);
            int radiusBucket = (int) ((particle.radius - UiConstants.PARTICLE_MIN_RADIUS) / SPRITE_RADIUS_STEP + 0.5);
            g2d.drawImage(particleSprite(radiusBucket, bucket),
                    (int) Math.round(particle.x - particle.radius) - SPRITE_INSET,
                    (int) Math.round(particle.y - particle.radius) - SPRITE_INSET, null);
        }
    }

    /**
     * 取粒子精灵(懒创建:首次使用时预渲染AA椭圆,后续帧仅贴绘)。
     *
     * @param radiusBucket 半径桶
     * @param brightnessBucket 亮度桶
     * @return 预渲染精灵
     */
    private BufferedImage particleSprite(int radiusBucket, int brightnessBucket) {
        BufferedImage sprite = particleSprites[radiusBucket][brightnessBucket];
        if (sprite == null) {
            double radius = UiConstants.PARTICLE_MIN_RADIUS + radiusBucket * SPRITE_RADIUS_STEP;
            int size = (int) Math.ceil(radius * 2) + SPRITE_INSET * 2;
            sprite = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D sg = sprite.createGraphics();
            sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            sg.setColor(PARTICLE_PALETTE[brightnessBucket]);
            sg.fill(new Ellipse2D.Double(SPRITE_INSET, SPRITE_INSET, radius * 2, radius * 2));
            sg.dispose();
            particleSprites[radiusBucket][brightnessBucket] = sprite;
        }
        return sprite;
    }

    /**
     * 构建粒子亮度调色板:PARTICLE_DIM与PARTICLE_BRIGHT之间64级插值。
     *
     * @return 调色板
     */
    private static Color[] buildParticlePalette() {
        Color[] palette = new Color[64];
        for (int i = 0; i < palette.length; i++) {
            palette[i] = blendColor(UiConstants.PARTICLE_DIM, UiConstants.PARTICLE_BRIGHT,
                    (double) i / (palette.length - 1));
        }
        return palette;
    }

    /**
     * 构建连线透明度调色板:alpha 0..LINK_MAX_ALPHA阶梯。
     *
     * @return 调色板
     */
    private static Color[] buildLinkPalette() {
        Color[] palette = new Color[UiConstants.LINK_MAX_ALPHA + 1];
        for (int i = 0; i < palette.length; i++) {
            palette[i] = new Color(UiConstants.LINK_COLOR.getRed(), UiConstants.LINK_COLOR.getGreen(),
                    UiConstants.LINK_COLOR.getBlue(), i);
        }
        return palette;
    }

    /**
     * 在两个颜色之间按系数插值。
     *
     * @param fromColor 起始颜色
     * @param toColor   目标颜色
     * @param ratio     插值系数（0~1）
     * @return 插值后的颜色
     */
    private static Color blendColor(Color fromColor, Color toColor, double ratio) {
        int red = (int) (fromColor.getRed() + (toColor.getRed() - fromColor.getRed()) * ratio);
        int green = (int) (fromColor.getGreen() + (toColor.getGreen() - fromColor.getGreen()) * ratio);
        int blue = (int) (fromColor.getBlue() + (toColor.getBlue() - fromColor.getBlue()) * ratio);
        return new Color(red, green, blue);
    }

    /**
     * 粒子：位置、速度、半径与闪烁相位。
     */
    private static final class Particle {
        /**
         * X 坐标
         */
        private double       x;
        /**
         * Y 坐标
         */
        private double       y;
        /**
         * X 方向扰动速度
         */
        private double       vx;
        /**
         * Y 方向扰动速度
         */
        private double       vy;
        /**
         * X 方向基础漂移速度
         */
        private final double baseVx;
        /**
         * Y 方向基础漂移速度
         */
        private final double baseVy;
        /**
         * 粒子半径
         */
        private final double radius;
        /**
         * 闪烁相位
         */
        private final double twinklePhase;

        private Particle(double x, double y, double baseVx, double baseVy, double radius, double twinklePhase) {
            this.x = x;
            this.y = y;
            this.baseVx = baseVx;
            this.baseVy = baseVy;
            this.radius = radius;
            this.twinklePhase = twinklePhase;
        }

        /**
         * 创建随机粒子。
         *
         * @param random 随机数生成器
         * @param width  面板宽度
         * @param height 面板高度
         * @return 粒子
         */
        private static Particle create(Random random, int width, int height) {
            double radius = UiConstants.PARTICLE_MIN_RADIUS
                    + random.nextDouble() * (UiConstants.PARTICLE_MAX_RADIUS - UiConstants.PARTICLE_MIN_RADIUS);
            return new Particle(random.nextDouble() * width, random.nextDouble() * height,
                    randomSignedSpeed(random), randomSignedSpeed(random),
                    radius, random.nextDouble() * FULL_TURN);
        }

        /**
         * 生成带随机方向的漂移速度。
         *
         * @param random 随机数生成器
         * @return 速度
         */
        private static double randomSignedSpeed(Random random) {
            double speed = UiConstants.PARTICLE_MIN_SPEED
                    + random.nextDouble() * (UiConstants.PARTICLE_MAX_SPEED - UiConstants.PARTICLE_MIN_SPEED);
            return random.nextBoolean() ? speed : -speed;
        }
    }
}
