#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SRC_DIR="$ROOT_DIR/xroar-1.10"
STAGE_SRC_DIR="${STAGE_SRC_DIR:-/tmp/xroar-wasm-src}"
BUILD_DIR="${BUILD_DIR:-/tmp/xroar-wasm-build}"
ASSET_DIR="$ROOT_DIR/app/src/main/assets/xroar"
EM_CONFIG_FILE="${EM_CONFIG_FILE:-/tmp/emscripten-config.py}"
EM_CACHE_DIR="${EM_CACHE_DIR:-/tmp/emscripten-cache}"
EM_PORTS_DIR="${EM_PORTS_DIR:-$EM_CACHE_DIR/ports}"
LLVM_ROOT_DIR="${LLVM_ROOT_DIR:-}"

if ! command -v emcc >/dev/null 2>&1; then
    echo "emcc not found in PATH" >&2
    exit 1
fi

if [ -z "$LLVM_ROOT_DIR" ]; then
    if [ -x /usr/lib/llvm-15/bin/clang ]; then
        LLVM_ROOT_DIR="/usr/lib/llvm-15/bin"
    elif command -v llvm-config-15 >/dev/null 2>&1; then
        LLVM_ROOT_DIR="$(llvm-config-15 --bindir)"
    elif command -v llvm-config >/dev/null 2>&1; then
        LLVM_ROOT_DIR="$(llvm-config --bindir)"
    else
        echo "Unable to determine LLVM bindir for emscripten" >&2
        exit 1
    fi
fi

if [ ! -f "$SRC_DIR/configure" ]; then
    echo "Missing configure script in $SRC_DIR" >&2
    exit 1
fi

rm -rf "$STAGE_SRC_DIR" "$BUILD_DIR"
mkdir -p "$BUILD_DIR" "$ASSET_DIR" "$EM_CACHE_DIR" "$EM_PORTS_DIR"
cp -a "$SRC_DIR" "$STAGE_SRC_DIR"
if [ -f "$STAGE_SRC_DIR/Makefile" ]; then
    make -C "$STAGE_SRC_DIR" distclean >/dev/null 2>&1 || true
fi

cat >"$EM_CONFIG_FILE" <<EOF
import os
NODE_JS = '/usr/bin/node'
BINARYEN_ROOT = '/usr/lib/emscripten'
LLVM_ROOT = '$LLVM_ROOT_DIR'
EMSCRIPTEN_ROOT = '/usr/share/emscripten'
TEMP_DIR = '/tmp'
COMPILER_ENGINE = NODE_JS
JS_ENGINES = [NODE_JS]
CACHE = '$EM_CACHE_DIR'
PORTS = '$EM_PORTS_DIR'
EOF

export EM_CONFIG="$EM_CONFIG_FILE"
export EM_CACHE="$EM_CACHE_DIR"
export EM_PORTS="$EM_PORTS_DIR"
export EMCC_SKIP_SANITY_CHECK=1

cat >/tmp/emscripten-warmup.c <<'EOF'
int main(void) { return 0; }
EOF
emcc /tmp/emscripten-warmup.c -o /tmp/emscripten-warmup.js >/dev/null 2>&1
rm -f /tmp/emscripten-warmup.c /tmp/emscripten-warmup.js /tmp/emscripten-warmup.wasm

pushd "$BUILD_DIR" >/dev/null
SDL_CFLAGS='-sUSE_SDL=2' \
SDL_LIBS='-sUSE_SDL=2' \
emconfigure "$STAGE_SRC_DIR/configure" \
    --host=wasm32-unknown-emscripten \
    --build=x86_64-pc-linux-gnu \
    --enable-wasm \
    --enable-ui-sdl \
    --without-gtk3 \
    --without-gtk2 \
    --without-gtkgl \
    --without-cocoa \
    --without-alsa \
    --without-oss \
    --without-pulse \
    --without-sndfile \
    --without-tre \
    --without-x \
    --without-zlib \
    --without-libpng

mkdir -p "$BUILD_DIR/src/wasm"
cp "$STAGE_SRC_DIR/src/wasm/exported_functions" "$BUILD_DIR/src/wasm/exported_functions"
python3 - <<'PY'
from pathlib import Path

makefile = Path("/tmp/xroar-wasm-build/src/Makefile")
text = makefile.read_text()
for marker in ("am__append_37", "am__append_38", "am__append_39"):
    text = text.replace(f"#{marker}", marker)
makefile.write_text(text)

config_h = Path("/tmp/xroar-wasm-build/config.h")
text = config_h.read_text()
text = text.replace("/* #undef HAVE_SDL2 */", "#define HAVE_SDL2 1")
config_h.write_text(text)
PY

emmake make -j"$(nproc)"
popd >/dev/null

if ! rg -q "_wasm_boot" "$BUILD_DIR/src/xroar.js"; then
    echo "Generated xroar.js is missing wasm_boot export" >&2
    exit 1
fi

cp "$BUILD_DIR/src/xroar.js" "$ASSET_DIR/xroar.js"
cp "$BUILD_DIR/src/xroar.wasm" "$ASSET_DIR/xroar.wasm"

echo "Built and copied:"
echo "  $ASSET_DIR/xroar.js"
echo "  $ASSET_DIR/xroar.wasm"
