package crest.siamese.helpers;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProcessedFilesTrackerTest {

    @Test
    public void nullLogPathDisablesTrackingAndRecordIsANoOp() throws IOException {
        ProcessedFilesTracker tracker = new ProcessedFilesTracker(null);
        assertFalse(tracker.isEnabled());
        tracker.recordProcessed("/some/file.js");
        tracker.close();
    }

    @Test
    public void emptyLogPathDisablesTrackingAndRecordIsANoOp() throws IOException {
        ProcessedFilesTracker tracker = new ProcessedFilesTracker("");
        assertFalse(tracker.isEnabled());
        tracker.recordProcessed("/some/file.js");
        tracker.close();
    }

    @Test
    public void nonEmptyLogPathEnablesTrackingAndRecordsFilesInOrder() throws IOException {
        File logFile = File.createTempFile("processed-files-tracker-test", ".txt");
        logFile.deleteOnExit();

        ProcessedFilesTracker tracker = new ProcessedFilesTracker(logFile.getAbsolutePath());
        assertTrue(tracker.isEnabled());
        tracker.recordProcessed("/a/1.js");
        tracker.recordProcessed("/a/2.js");
        tracker.recordProcessed("/a/3.js");
        tracker.close();

        List<String> lines = Files.readAllLines(logFile.toPath());
        assertEquals(3, lines.size());
        assertEquals("/a/1.js", lines.get(0));
        assertEquals("/a/2.js", lines.get(1));
        assertEquals("/a/3.js", lines.get(2));
    }

    @Test
    public void eachRecordIsFlushedImmediatelyRatherThanOnlyOnClose() throws IOException {
        // this is the whole point of the class: these indexing runs get killed abruptly
        // (forcibly-stopped hangs, OOM storms, manual kill -9), so a record must be durable
        // on disk the moment it's made, not sitting in a buffer waiting for a clean close().
        File logFile = File.createTempFile("processed-files-tracker-test-flush", ".txt");
        logFile.deleteOnExit();

        ProcessedFilesTracker tracker = new ProcessedFilesTracker(logFile.getAbsolutePath());
        tracker.recordProcessed("/a/1.js");

        // deliberately not closing tracker here, simulating an abrupt kill after the record
        List<String> lines = Files.readAllLines(logFile.toPath());
        assertEquals(1, lines.size());
        assertEquals("/a/1.js", lines.get(0));
    }

    @Test
    public void reopeningTheSameLogPathAppendsRatherThanOverwriting() throws IOException {
        // simulates resuming a killed run: relaunching with the same processedFilesLog path
        // must preserve everything already recorded, not truncate it.
        File logFile = File.createTempFile("processed-files-tracker-test-append", ".txt");
        logFile.deleteOnExit();

        ProcessedFilesTracker firstRun = new ProcessedFilesTracker(logFile.getAbsolutePath());
        firstRun.recordProcessed("/a/1.js");
        firstRun.recordProcessed("/a/2.js");
        firstRun.close();

        ProcessedFilesTracker secondRun = new ProcessedFilesTracker(logFile.getAbsolutePath());
        secondRun.recordProcessed("/a/3.js");
        secondRun.close();

        List<String> lines = Files.readAllLines(logFile.toPath());
        assertEquals(3, lines.size());
        assertEquals("/a/1.js", lines.get(0));
        assertEquals("/a/2.js", lines.get(1));
        assertEquals("/a/3.js", lines.get(2));
    }

    @Test
    public void closeOnDisabledTrackerIsSafe() throws IOException {
        ProcessedFilesTracker tracker = new ProcessedFilesTracker(null);
        tracker.close();
        tracker.close(); // idempotent: closing twice must not throw
    }
}
