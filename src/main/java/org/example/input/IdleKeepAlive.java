package org.example.input;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Breaks up long {@link Thread#sleep(long)} intervals with tiny mouse movements so the game is less likely to treat
 * the session as fully idle (disconnect / kick).
 */
public final class IdleKeepAlive {

    /** Waits shorter than this use a plain sleep (no mouse movement). */
    public static final long MIN_WAIT_MS_FOR_JIGGLE = 2_000L;

    /** Time between cursor nudges during a long wait. */
    private static final long JIGGLE_STEP_MS = 4_000L;

    /** Max horizontal/vertical nudge per step (pixels). */
    private static final int JIGGLE_RADIUS_PX = 8;

    private static final Robot ROBOT;

    static {
        try {
            ROBOT = new Robot();
        } catch (AWTException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private IdleKeepAlive() {
    }

    /**
     * Sleeps for {@code totalMs}. When {@code totalMs >=} {@link #MIN_WAIT_MS_FOR_JIGGLE}, sleeps in chunks and nudges
     * the mouse slightly after each chunk; otherwise uses {@link Thread#sleep(long)} only.
     */
    public static void sleepWithMouseJiggle(long totalMs) throws InterruptedException {
        if (totalMs <= 0) {
            return;
        }
        if (totalMs < MIN_WAIT_MS_FOR_JIGGLE) {
            Thread.sleep(totalMs);
            return;
        }
        long remaining = totalMs;
        while (remaining > 0) {
            long chunk = Math.min(JIGGLE_STEP_MS, remaining);
            Thread.sleep(chunk);
            remaining -= chunk;
            jiggleMouseSmall();
        }
    }

    private static void jiggleMouseSmall() {
        var pointer = MouseInfo.getPointerInfo();
        if (pointer == null) {
            return;
        }
        Point p = pointer.getLocation();
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int dx = r.nextInt(-JIGGLE_RADIUS_PX, JIGGLE_RADIUS_PX + 1);
        int dy = r.nextInt(-JIGGLE_RADIUS_PX, JIGGLE_RADIUS_PX + 1);
        if (dx == 0 && dy == 0) {
            dx = 1;
        }
        ROBOT.mouseMove(p.x + dx, p.y + dy);
    }
}
