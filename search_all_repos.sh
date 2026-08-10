#!/bin/bash
# Loops through every cloned repo in a given directory, runs a Matcha clone
# search using it as the query input, and saves the resulting output file
# named <repo>_<timestamp>.<ext>.
#
# Usage: ./search_all_repos.sh [repos_dir] [config_file] [output_dir]
#   repos_dir defaults to python_repos_100 if omitted.
#   config_file defaults to config_python.properties if omitted.
#   output_dir defaults to search_results if omitted.
set -uo pipefail

MATCHA_DIR="/Users/chaiyong/Downloads/matcha/1_matcha"
JAVA_BIN="/Library/Java/JavaVirtualMachines/temurin-8.jdk/Contents/Home/bin/java"
JAR="matcha-0.1.0.jar"
CONFIG="${2:-config_python.properties}"
REPOS_DIR="${1:-/Users/chaiyong/Downloads/matcha/python_repos_100}"
OUTPUT_DIR="${3:-$MATCHA_DIR/search_results}"
LOG_FILE="$MATCHA_DIR/search_all_repos_$(basename "$REPOS_DIR").log"

mkdir -p "$OUTPUT_DIR"
cd "$MATCHA_DIR"

total=$(find "$REPOS_DIR" -mindepth 1 -maxdepth 1 -type d | wc -l | tr -d ' ')
count=0
start_epoch=$(date +%s)

echo "=== Starting search over $total repos at $(date) ===" | tee -a "$LOG_FILE"

for repo_path in "$REPOS_DIR"/*/; do
  repo_name=$(basename "$repo_path")
  count=$((count + 1))

  # resume support: skip repos that already have a saved result file
  if compgen -G "$OUTPUT_DIR/${repo_name}_*" > /dev/null; then
    echo "[$count/$total] $(date '+%H:%M:%S') Skipping $repo_name (already has a result file)" | tee -a "$LOG_FILE"
    continue
  fi

  echo "[$count/$total] $(date '+%H:%M:%S') Searching with repo: $repo_name" | tee -a "$LOG_FILE"

  before=$(ls -1 "$OUTPUT_DIR" 2>/dev/null | sort)

  "$JAVA_BIN" -jar "$JAR" -cf "$CONFIG" -c search -i "$repo_path" -o "$OUTPUT_DIR" >> "$LOG_FILE" 2>&1

  after=$(ls -1 "$OUTPUT_DIR" 2>/dev/null | sort)
  new_file=$(comm -13 <(echo "$before") <(echo "$after") | head -1)

  if [ -n "$new_file" ]; then
    timestamp=$(date +%Y%m%d_%H%M%S)
    ext="${new_file##*.}"
    dest="${repo_name}_${timestamp}.${ext}"
    mv "$OUTPUT_DIR/$new_file" "$OUTPUT_DIR/$dest"
    echo "  -> saved as $dest" | tee -a "$LOG_FILE"
  else
    echo "  -> WARNING: no output file produced for $repo_name" | tee -a "$LOG_FILE"
  fi
done

end_epoch=$(date +%s)
elapsed=$((end_epoch - start_epoch))
printf -v elapsed_hms '%02d:%02d:%02d' $((elapsed/3600)) $((elapsed%3600/60)) $((elapsed%60))

echo "=== Done. Processed $count/$total repos at $(date) (elapsed: $elapsed_hms) ===" | tee -a "$LOG_FILE"
