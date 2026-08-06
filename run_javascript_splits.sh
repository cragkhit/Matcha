#!/bin/bash
# Runs Matcha indexing sequentially over the 20 javascript_files_splits/split_NN folders,
# each appending into the same javascript_files ES index (only split_01's config recreates
# the index, since it starts out empty/nonexistent; splits 02-20 never recreate it).
#
# Never runs splits in parallel: a single indexer JVM already saturates this machine's
# CPU/memory, and concurrent runs were what caused resource-contention/OOM problems before.
#
# Usage: ./run_javascript_splits.sh
set -uo pipefail

MATCHA_DIR="/Users/chaiyong/Downloads/matcha/1_matcha"
JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-8.jdk/Contents/Home"
JAVA_BIN="$JAVA_HOME/bin/java"
JAR="matcha-0.1.0.jar"
MASTER_LOG="$MATCHA_DIR/run_javascript_splits.log"

cd "$MATCHA_DIR"
start_epoch=$(date +%s)

echo "=== Starting 20-split JS indexing run at $(date) ===" | tee -a "$MASTER_LOG"

for i in $(seq -w 1 20); do
  CONFIG="config_javascript_split_${i}.properties"
  LOG_FILE="$MATCHA_DIR/index_javascript_split_${i}.log"

  # resume support: skip splits that already finished successfully
  if [ -f "$LOG_FILE" ] && grep -q "Successfully creating index." "$LOG_FILE"; then
    echo "[split $i/20] $(date '+%Y-%m-%d %H:%M:%S') Skipping (already completed, see $LOG_FILE)" | tee -a "$MASTER_LOG"
    continue
  fi

  echo "[split $i/20] $(date '+%Y-%m-%d %H:%M:%S') Starting (config: $CONFIG, log: $LOG_FILE)" | tee -a "$MASTER_LOG"
  split_start=$(date +%s)

  "$JAVA_BIN" -Xmx12g -Xss16m -jar "$JAR" -cf "$CONFIG" -c index > "$LOG_FILE" 2>&1
  exit_code=$?

  split_end=$(date +%s)
  split_elapsed=$((split_end - split_start))
  printf -v split_hms '%02d:%02d:%02d' $((split_elapsed/3600)) $((split_elapsed%3600/60)) $((split_elapsed%60))

  if [ $exit_code -ne 0 ] || ! grep -q "Successfully creating index." "$LOG_FILE"; then
    echo "[split $i/20] $(date '+%Y-%m-%d %H:%M:%S') FAILED after $split_hms (exit code $exit_code). Stopping — check $LOG_FILE, then re-run this script to resume from here." | tee -a "$MASTER_LOG"
    exit 1
  fi

  echo "[split $i/20] $(date '+%Y-%m-%d %H:%M:%S') Done in $split_hms" | tee -a "$MASTER_LOG"
done

end_epoch=$(date +%s)
elapsed=$((end_epoch - start_epoch))
printf -v elapsed_hms '%02d:%02d:%02d' $((elapsed/3600)) $((elapsed%3600/60)) $((elapsed%60))

echo "=== All 20 splits complete at $(date) (elapsed: $elapsed_hms) ===" | tee -a "$MASTER_LOG"
