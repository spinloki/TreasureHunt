#!/usr/bin/env bash
# Usage: test/run.sh [--seed N] [--no-build]
set -uo pipefail

MOD="$(cd "$(dirname "$0")/.." && pwd)"
SS="$(cd "$MOD/../.." && pwd)"
H="$SS/headless-sim"
JDK="${JDK:-$HOME/.jdks/azul-17.0.18}"
SEED=1
BUILD=1
while [ $# -gt 0 ]; do
  case "$1" in
    --seed) SEED="$2"; shift 2 ;;
    --no-build) BUILD=0; shift ;;
    *) echo "unknown argument $1" >&2; exit 2 ;;
  esac
done

OUT="$MOD/test/out"
FIXTURE="th_test_seed$SEED"
RESULTS="$OUT/results.txt"
rm -rf "$OUT" "$H/fixtures/$FIXTURE" && mkdir -p "$OUT/classes"

if [ "$BUILD" = 1 ]; then
  "$MOD/tools/build.sh" > "$OUT/build.log" 2>&1 || { cat "$OUT/build.log"; exit 1; }
fi
[ -d "$H/build/headless" ] || "$H/build.sh" || exit 1

"$JDK/bin/javac" -nowarn -encoding UTF-8 --release 17 -d "$OUT/classes" \
  -cp "$SS/starfarer.api.jar:$SS/starfarer_obf.jar:$SS/fs.common_obf.jar:$SS/xstream-1.4.10.jar:$SS/json.jar:$SS/lwjgl_util.jar:$SS/log4j-1.2.9.jar:$H/build:$MOD/jars/TreasureHunt.jar" \
  $(find "$MOD/test/src" -name '*.java') || exit 1

export MODS_DIR="$H/profiles/treasurehunt"
run_phase() {
  local name="$1"; shift
  local t0=$SECONDS
  "$H/run.sh" headless.Main "$@" --setup-cp "$OUT/classes" --set th.test.results="$RESULTS" --days 0 > "$OUT/$name.log" 2>&1
  local code=$?
  echo "$name: exit $code in $((SECONDS - t0))s (log: test/out/$name.log)"
  return $code
}

run_phase new-game --seed "$SEED" --setup spinloki.TreasureHunt.test.NewGameSuite --save "$FIXTURE"
P1=$?
P2=1
if [ $P1 = 0 ]; then
  run_phase load --load "$FIXTURE" --setup spinloki.TreasureHunt.test.LoadSuite
  P2=$?
fi

echo
[ -f "$RESULTS" ] && cat "$RESULTS"
PASS=$(grep -c '^PASS' "$RESULTS" 2>/dev/null)
FAIL=$(grep -c '^FAIL' "$RESULTS" 2>/dev/null)
echo
echo "$PASS passed, $FAIL failed"
for p in new-game load; do
  [ -f "$OUT/$p.log" ] && grep -q "^\S*Exception\|^\s*at headless.Main" "$OUT/$p.log" && echo "crash in $p, see test/out/$p.log"
done
[ $P1 = 0 ] && [ $P2 = 0 ] && [ "$FAIL" = 0 ] && [ "$PASS" -gt 0 ]
