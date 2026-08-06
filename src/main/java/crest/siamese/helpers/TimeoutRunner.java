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

/**
 * Runs a task on its own thread and abandons it if it doesn't finish within a deadline.
 * <p>
 * Used to guard against pathological inputs (e.g. ANTLR grammar ambiguity explosions, or
 * custom stateful lexer/parser predicates looping) that can hang a worker thread indefinitely
 * with no exception ever thrown, so a plain try/catch can't recover from them.
 * <p>
 * The worker thread is never joined without a timeout and, on timeout, is forcibly stopped via
 * the deprecated {@link Thread#stop()} rather than {@link Thread#interrupt()}, because ANTLR's
 * ATN simulation does not check the interrupt flag during closure/prediction-context merging.
 * This is only safe when the task touches no shared mutable state beyond thread-local objects.
 */
public final class TimeoutRunner {

    private TimeoutRunner() {
    }

    /**
     * Runs {@code task} on a new daemon thread and waits up to {@code timeoutMs} for it to finish.
     *
     * @param task      the task to run; must not share mutable state with the caller
     * @param timeoutMs how long to wait before giving up
     * @return true if the task completed within the deadline, false if it was forcibly stopped
     */
    public static boolean run(Runnable task, long timeoutMs) {
        Thread worker = new Thread(task);
        worker.setDaemon(true);
        worker.start();
        try {
            worker.join(timeoutMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (worker.isAlive()) {
            forciblyStop(worker);
            return false;
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    private static void forciblyStop(Thread worker) {
        try {
            worker.stop();
        } catch (Throwable ignored) {
        }
    }
}
