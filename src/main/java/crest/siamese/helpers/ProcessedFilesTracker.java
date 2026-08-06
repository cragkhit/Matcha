/*
   Copyright 2026 Chaiyong Ragkhitwetsagul

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package crest.siamese.helpers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Records the absolute path of every file an indexing run finishes processing (success or
 * parse failure), so a killed/interrupted run can be resumed against only the files that were
 * never reached instead of re-scanning the whole input folder.
 * <p>
 * Constructing with a null or empty log path disables tracking entirely: {@link #isEnabled()}
 * returns false and {@link #recordProcessed(String)} becomes a no-op, so callers don't need to
 * branch on whether tracking is configured.
 */
public class ProcessedFilesTracker {

    private final BufferedWriter writer;

    public ProcessedFilesTracker(String logPath) throws IOException {
        if (logPath == null || logPath.isEmpty()) {
            this.writer = null;
        } else {
            // append mode: reusing the same path across a killed/restarted run keeps
            // previously recorded files rather than losing them.
            this.writer = new BufferedWriter(new FileWriter(logPath, true));
        }
    }

    public boolean isEnabled() {
        return writer != null;
    }

    /**
     * Records filePath as done and flushes immediately. These indexing runs have repeatedly
     * been killed abruptly (hangs forcibly stopped, OOM storms, manual kill -9), and a
     * buffered-but-unflushed tail would silently reappear as "not yet processed" on the next
     * resume, defeating the point of tracking.
     */
    public void recordProcessed(String filePath) throws IOException {
        if (writer == null) {
            return;
        }
        writer.write(filePath);
        writer.newLine();
        writer.flush();
    }

    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }
}
