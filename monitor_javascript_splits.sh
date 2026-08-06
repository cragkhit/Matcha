#!/bin/bash
# One-shot status summary for the 20-split JS indexing run: which splits are done, which
# one is currently running, and its latest progress line.
#
# Usage: ./monitor_javascript_splits.sh
MATCHA_DIR="/Users/chaiyong/Downloads/matcha/1_matcha"
cd "$MATCHA_DIR" || exit 1

done_count=0
current_split=""

for i in $(seq -w 1 20); do
  LOG_FILE="index_javascript_split_${i}.log"
  if [ -f "$LOG_FILE" ] && grep -q "Successfully creating index." "$LOG_FILE"; then
    done_count=$((done_count + 1))
  elif [ -f "$LOG_FILE" ] && [ -z "$current_split" ]; then
    current_split="$i"
  fi
done

echo "Splits completed: $done_count/20"

if [ -n "$current_split" ]; then
  LOG_FILE="index_javascript_split_${current_split}.log"
  echo "Currently running: split $current_split ($LOG_FILE)"
  latest=$(grep "^Indexed " "$LOG_FILE" | tail -1)
  if [ -n "$latest" ]; then
    echo "  -> $latest"
  else
    echo "  -> (still scanning input folder / no progress line yet)"
  fi
  # is the process actually alive?
  if pgrep -f "cf config_javascript_split_${current_split}.properties" > /dev/null; then
    echo "  -> process is alive"
  else
    echo "  -> WARNING: no matching process found (may have crashed or been killed)"
  fi
elif [ "$done_count" -lt 20 ]; then
  echo "No split currently running and fewer than 20 done — run_javascript_splits.sh may not be active."
else
  echo "All 20 splits complete."
fi

echo
echo "--- tail of master log ---"
tail -5 run_javascript_splits.log 2>/dev/null
