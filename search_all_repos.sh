#!/bin/bash
# Loops through every cloned repo, runs a Matcha clone search using it as the
# query input, and saves the resulting output file named <repo>_<timestamp>.<ext>.
set -uo pipefail

MATCHA_DIR="/Users/chaiyong/Downloads/matcha/1_matcha"
JAVA_BIN="/Library/Java/JavaVirtualMachines/temurin-8.jdk/Contents/Home/bin/java"
JAR="matcha-0.1.0.jar"
CONFIG="config_python.properties"
REPOS_DIR="/Users/chaiyong/Downloads/matcha/python_repos_100"
OUTPUT_DIR="$MATCHA_DIR/search_results"
LOG_FILE="$MATCHA_DIR/search_all_repos.log"

mkdir -p "$OUTPUT_DIR"
cd "$MATCHA_DIR"

total=$(find "$REPOS_DIR" -mindepth 1 -maxdepth 1 -type d | wc -l | tr -d ' ')
count=0

echo "=== Starting search over $total repos at $(date) ===" | tee -a "$LOG_FILE"

for repo_path in "$REPOS_DIR"/*/; do
  repo_name=$(basename "$repo_path")
  count=$((count + 1))
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

echo "=== Done. Processed $count/$total repos at $(date) ===" | tee -a "$LOG_FILE"
