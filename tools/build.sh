#!/usr/bin/env bash
set -euo pipefail

# ── Paths ──────────────────────────────────────────────────────────────
JAVAC="$HOME/.jdks/azul-17.0.18/bin/javac"
JAR="$HOME/.jdks/azul-17.0.18/bin/jar"
SS="$HOME/games/starsector"
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

# ── Classpath (Starsector API + dependencies) ──────────────────────────
CP=$(printf '%s:' \
  "$SS/commons-compiler-jdk.jar" \
  "$SS/commons-compiler.jar" \
  "$SS/fs.common_obf.jar" \
  "$SS/fs.sound_obf.jar" \
  "$SS/janino.jar" \
  "$SS/jaxb-api-2.4.0-b180830.0359.jar" \
  "$SS/jinput.jar" \
  "$SS/jogg-0.0.7.jar" \
  "$SS/jorbis-0.0.15.jar" \
  "$SS/json.jar" \
  "$SS/log4j-1.2.9.jar" \
  "$SS/lwjgl.jar" \
  "$SS/lwjgl_util.jar" \
  "$SS/starfarer.api.jar" \
  "$SS/starfarer_obf.jar" \
  "$SS/txw2-3.0.2.jar" \
  "$SS/webp-imageio-0.1.6.jar" \
  "$SS/xstream-1.4.10.jar" \
  "$SS/mods/Console Commands/jars/lw_Console.jar" \
  "$SS/mods/LazyLib-3.0.0/jars/LazyLib.jar")

# ── Build ──────────────────────────────────────────────────────────────
OUT_DIR="$PROJECT_DIR/out/production"
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

SOURCES=$(find "$PROJECT_DIR/src" -name '*.java')
NUM_SOURCES=$(echo "$SOURCES" | wc -l)

echo "Compiling $NUM_SOURCES source files..."
echo "$SOURCES" | $JAVAC -source 17 -target 17 -cp "$CP" -d "$OUT_DIR" @/dev/stdin

echo "Packaging jar..."
$JAR cf "$PROJECT_DIR/jars/TreasureHunt.jar" -C "$OUT_DIR" .

echo "Done! $(ls -lh "$PROJECT_DIR/jars/TreasureHunt.jar")"
