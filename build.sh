#!/bin/bash
# Builds Digital.app: a self-contained macOS app with its own Java runtime.
#
#   bash build.sh            build target/Digital.app
#   bash build.sh --install  build, quit the running app, replace /Applications/Digital.app, launch it
#
# App icon: packaging/macos/icon.png (1024×1024 full-bleed artwork) is masked onto the
# macOS icon grid. Without it a placeholder is drawn.
set -euo pipefail
cd "$(dirname "$0")"

export JAVA_HOME="${JAVA_HOME_DIGITAL:-/opt/homebrew/opt/openjdk@21}"
export PATH="$JAVA_HOME/bin:$PATH"

INSTALL=0
for arg in "$@"; do
    case "$arg" in
        --install) INSTALL=1 ;;
        *) echo "unknown option $arg" >&2; exit 1 ;;
    esac
done

APP_NAME=Digital
BUNDLE_ID=com.endurance.Digital
TARGET=target
STAGE=$TARGET/mac-input
ICONSET=$TARGET/Digital.iconset
APP=$TARGET/$APP_NAME.app

echo "▸ jar"
mvn -q -B -DskipTests -Djacoco.skip=true package

echo "▸ stage"
rm -rf "$STAGE" && mkdir -p "$STAGE/examples"
cp "$TARGET/Digital.jar" "$STAGE/"
# the component library must sit next to Digital.jar (see ElementLibrary.getLibPath)
rsync -a --include='*/' --include='*.dig' --include='*.txt' --exclude='*' src/main/dig/lib/ "$STAGE/lib/"
for d in src/main/dig/*/; do
    name=$(basename "$d")
    [ "$name" = lib ] && continue
    rsync -a --include='*/' --include='*.dig' --include='*.svg' --include='*.config' --exclude='*' "$d" "$STAGE/examples/$name/"
done
mkdir -p "$STAGE/examples/fsm" && cp src/main/fsm/*.fsm "$STAGE/examples/fsm/" 2>/dev/null || true

echo "▸ icon"
ART=packaging/macos/icon.png
[ -f "$ART" ] || ART=-
swift packaging/macos/make-icon.swift "$ART" "$TARGET/icon-1024.png"
rm -rf "$ICONSET" && mkdir -p "$ICONSET"
for s in 16 32 128 256 512; do
    sips -z $s $s "$TARGET/icon-1024.png" --out "$ICONSET/icon_${s}x${s}.png" >/dev/null
    sips -z $((s * 2)) $((s * 2)) "$TARGET/icon-1024.png" --out "$ICONSET/icon_${s}x${s}@2x.png" >/dev/null
done
iconutil -c icns "$ICONSET" -o "$TARGET/Digital.icns"

echo "▸ app"
# jdeps can't read the fat jar (it carries FlatLaf's module-info), so the list is fixed:
# what jdeps reports for Digital plus java.logging for FlatLaf
MODULES=java.base,java.desktop,java.prefs,java.sql,java.logging,jdk.unsupported,jdk.localedata
rm -rf "$APP"
jpackage --type app-image \
    --dest "$TARGET" \
    --name "$APP_NAME" \
    --app-version 1.0 \
    --vendor "Helmut Neemann" \
    --input "$STAGE" \
    --main-jar Digital.jar \
    --main-class de.neemann.digital.gui.App \
    --icon "$TARGET/Digital.icns" \
    --mac-package-identifier "$BUNDLE_ID" \
    --add-modules "$MODULES" \
    --jlink-options "--strip-debug --no-man-pages --no-header-files" \
    --java-options "-Dapple.awt.application.appearance=NSAppearanceNameDarkAqua -Dapple.laf.useScreenMenuBar=true -Dapple.awt.application.name=Digital -Xss4m"

PLIST="$APP/Contents/Info.plist"
pb() { /usr/libexec/PlistBuddy -c "$1" "$PLIST" >/dev/null; }
pb "Delete :CFBundleDocumentTypes" 2>/dev/null || true
pb "Add :CFBundleDocumentTypes array"
i=0
for entry in "dig:Digital Circuit" "fsm:Finite State Machine" "tru:Truth Table"; do
    ext=${entry%%:*}; label=${entry#*:}
    pb "Add :CFBundleDocumentTypes:$i dict"
    pb "Add :CFBundleDocumentTypes:$i:CFBundleTypeName string $label"
    pb "Add :CFBundleDocumentTypes:$i:CFBundleTypeRole string Editor"
    pb "Add :CFBundleDocumentTypes:$i:LSHandlerRank string Owner"
    pb "Add :CFBundleDocumentTypes:$i:CFBundleTypeExtensions array"
    pb "Add :CFBundleDocumentTypes:$i:CFBundleTypeExtensions:0 string $ext"
    pb "Add :CFBundleDocumentTypes:$i:CFBundleTypeIconFile string Digital.icns"
    i=$((i + 1))
done
pb "Delete :NSHighResolutionCapable" 2>/dev/null || true
pb "Add :NSHighResolutionCapable bool true"

codesign --force --deep --sign - "$APP" 2>/dev/null
echo "✓ $APP ($(du -sh "$APP" | cut -f1))"

if [ $INSTALL = 1 ]; then
    echo "▸ install"
    if pgrep -f "/Applications/$APP_NAME.app/" >/dev/null; then
        osascript -e "quit app \"$APP_NAME\"" >/dev/null 2>&1 || true
        for _ in $(seq 1 50); do pgrep -f "/Applications/$APP_NAME.app/" >/dev/null || break; sleep 0.2; done
        if pgrep -f "/Applications/$APP_NAME.app/" >/dev/null; then
            echo "Digital is still running (unsaved changes?) — not replaced" >&2
            exit 1
        fi
    fi
    rm -rf "/Applications/$APP_NAME.app"
    cp -R "$APP" /Applications/
    /System/Library/Frameworks/CoreServices.framework/Frameworks/LaunchServices.framework/Support/lsregister -f "/Applications/$APP_NAME.app"
    open "/Applications/$APP_NAME.app"
    echo "✓ installed /Applications/$APP_NAME.app"
fi
