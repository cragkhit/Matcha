package crest.siamese.helpers;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TimeoutRunnerTest {

    @Test
    public void taskThatFinishesInTimeReturnsTrueAndRuns() {
        AtomicBoolean ran = new AtomicBoolean(false);
        boolean completed = TimeoutRunner.run(() -> ran.set(true), 1000);
        assertTrue(completed);
        assertTrue(ran.get());
    }

    @Test
    public void slowTaskIsAbandonedAndReturnsFalse() {
        AtomicInteger reachedAfterSleep = new AtomicInteger(0);
        long start = System.currentTimeMillis();
        boolean completed = TimeoutRunner.run(() -> {
            try {
                Thread.sleep(2000);
                reachedAfterSleep.incrementAndGet();
            } catch (InterruptedException ignored) {
            }
        }, 100);
        long elapsed = System.currentTimeMillis() - start;

        assertFalse(completed);
        // must return close to the timeout, not wait for the full 2s sleep
        assertTrue("expected to return within ~1s of the 100ms timeout, took " + elapsed + "ms",
                elapsed < 1000);
    }

    @Test
    public void infiniteBusyLoopIsForciblyStoppedRatherThanHangingForever() {
        AtomicBoolean stillRunning = new AtomicBoolean(true);
        long start = System.currentTimeMillis();
        boolean completed = TimeoutRunner.run(() -> {
            //noinspection StatementWithEmptyBody
            while (stillRunning.get()) {
                // deliberately never-ending CPU-bound loop, simulating a catastrophic
                // ANTLR ambiguity explosion with no natural exit and no exception thrown
            }
        }, 200);
        long elapsed = System.currentTimeMillis() - start;

        assertFalse(completed);
        assertTrue("expected to return within ~1s of the 200ms timeout, took " + elapsed + "ms",
                elapsed < 1200);
    }

    @Test
    public void exceptionInsideTaskDoesNotEscapeAndStillCountsAsCompleted() {
        boolean completed = TimeoutRunner.run(() -> {
            throw new RuntimeException("boom");
        }, 1000);
        // the task's own exception is its business (caller decides how to observe it, e.g. via
        // a captured Throwable[] as JSMethodParser does); TimeoutRunner itself must not propagate
        // it or mistake it for a timeout.
        assertTrue(completed);
    }

    @Test
    public void zeroTimeoutStillEventuallyStopsRunawayTask() {
        boolean completed = TimeoutRunner.run(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                // never-ending
            }
        }, 0);
        assertEquals(false, completed);
    }
}
