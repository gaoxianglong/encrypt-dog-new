#!/usr/bin/env bash
#
# 加密狗 macOS 打包脚本: 把 fat jar 打包成可双击运行的 EncryptDog.app
# 用法: ./build-mac.sh        # 产出 dist/EncryptDog.app
#       ./build-mac.sh dmg    # 额外产出 dist/EncryptDog-<版本>.dmg
#
set -euo pipefail

# 切换到脚本所在目录,保证任意 cwd 下可运行
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

APP_NAME="EncryptDog"
ICON_SRC="src/main/resources/dock_logo.png"
DIST="dist"

# 前置检查: jpackage/iconutil/mvn 必须可用
for tool in jpackage iconutil mvn; do
    if ! command -v "$tool" >/dev/null 2>&1; then
        echo "[build-mac] 缺少工具: $tool (需要完整 JDK,如 jdk-21.0.1.jdk), 退出"
        exit 1
    fi
done

# 1. 构建 fat jar
echo "[build-mac] 1/4 构建 fat jar..."
mvn -q clean package -DskipTests

# 2. 提取版本号(与 pom 单份维护)
VERSION="$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout 2>/dev/null | tail -1 | tr -d '[:space:]')"
if [ -z "$VERSION" ]; then
    VERSION="$(sed -n 's#.*<version>\([^<]*\)</version>.*#\1#p' pom.xml | head -1)"
fi
echo "[build-mac] 版本: $VERSION"

# 3. 从 dock_logo.png 生成 .icns(iconset 十档: 16/32/128/256/512 及 @2x)
# 圆角预处理(半径=边长22%,与运行时Java2D渲染同比例):PIL可用则输出圆角PNG作为图标源,否则降级原图
echo "[build-mac] 2/4 生成 .icns..."
TMP_DIR="$(mktemp -d)"
ICONSET="$TMP_DIR/app.iconset"
mkdir -p "$ICONSET"
ROUNDED_SRC="$ICON_SRC"
if python3 -c "import PIL" >/dev/null 2>&1; then
    python3 - "$ICON_SRC" "$TMP_DIR/dock_rounded.png" <<'PYEOF'
import sys
from PIL import Image, ImageChops, ImageDraw
src, dst = sys.argv[1], sys.argv[2]
# 内容收进82%内框(与macOS标准图标边距一致,避免Dock观感偏大),圆角半径=内框22%
INNER = int(1024 * 0.82)
INSET = (1024 - INNER) // 2
img = Image.open(src).convert("RGBA").resize((INNER, INNER), Image.LANCZOS)
canvas = Image.new("RGBA", (1024, 1024), (0, 0, 0, 0))
canvas.paste(img, (INSET, INSET))
mask = Image.new("L", (1024, 1024), 0)
ImageDraw.Draw(mask).rounded_rectangle([INSET, INSET, INSET + INNER, INSET + INNER],
                                       radius=int(INNER * 0.22), fill=255)
canvas.putalpha(ImageChops.multiply(canvas.split()[3], mask))
canvas.save(dst)
PYEOF
    ROUNDED_SRC="$TMP_DIR/dock_rounded.png"
    echo "[build-mac] 图标已圆角预处理(82%内框+半径22%)"
else
    echo "[build-mac] 未找到PIL,降级使用原始方形图标"
fi
for size in 16 32 128 256 512; do
    sips -z "$size" "$size" "$ROUNDED_SRC" --out "$ICONSET/icon_${size}x${size}.png" >/dev/null
    sips -z "$((size * 2))" "$((size * 2))" "$ROUNDED_SRC" --out "$ICONSET/icon_${size}x${size}@2x.png" >/dev/null
done
iconutil -c icns "$ICONSET" -o "/tmp/${APP_NAME}.icns"
rm -rf "$TMP_DIR"
echo "[build-mac] icns 已生成: /tmp/${APP_NAME}.icns"

# 4. jpackage 产出 app-image(每次运行先清理,幂等)
# 仅暂存 fat jar 作为 --input: 直接 --input target 会把瘦 jar 一并塞进 bundle classpath
echo "[build-mac] 3/4 jpackage app-image..."
rm -rf "$DIST"
STAGE_DIR="$(mktemp -d)"
cp "target/encryptdog-${VERSION}.jar" "$STAGE_DIR/"
jpackage --type app-image \
    --name "$APP_NAME" \
    --app-version "$VERSION" \
    --main-class com.gxl.encryptdog.Starter \
    --main-jar "encryptdog-${VERSION}.jar" \
    --arguments "--gui" \
    --java-options "-Xms1g -Xmx1g -Xmn384m" \
    --file-associations file-associations.properties \
    --icon "/tmp/${APP_NAME}.icns" \
    --input "$STAGE_DIR" \
    --dest "$DIST"
rm -rf "$STAGE_DIR"

# 校验文件关联声明写入Info.plist(防jpackage参数回归:声明缺失时双击.dog无法唤起GUI)
INFO_PLIST="$DIST/${APP_NAME}.app/Contents/Info.plist"
if /usr/libexec/PlistBuddy -c "Print :CFBundleDocumentTypes" "$INFO_PLIST" 2>/dev/null | grep -q "dog"; then
    echo "[build-mac] 文件关联声明已写入: .dog -> $APP_NAME"
else
    echo "[build-mac] 错误: Info.plist 缺少 .dog 文件关联声明, 打包中止" >&2
    exit 1
fi

echo "[build-mac] 完成: $DIST/${APP_NAME}.app"

# 可选: dmg 安装镜像(基于已产出的 app-image,不重复 jlink)
if [ "${1:-}" = "dmg" ]; then
    echo "[build-mac] 4/4 jpackage dmg..."
    jpackage --type dmg \
        --app-image "$DIST/${APP_NAME}.app" \
        --name "$APP_NAME" \
        --app-version "$VERSION" \
        --dest "$DIST"
    echo "[build-mac] 完成: $DIST/${APP_NAME}-${VERSION}.dmg"
else
    echo "[build-mac] 跳过 dmg(需要时执行: ./build-mac.sh dmg)"
fi
