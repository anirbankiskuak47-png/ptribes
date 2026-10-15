package com.ptribes.newhorizons;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
/** Turns gestures into the key events the original keypad game understands. Everything runs on one thread, in order. */
final class Injector {
    private final ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
    private final Bridge br;
    private final HashMap<Integer, Long> downT = new HashMap<Integer, Long>();
    private final AtomicInteger pendingPan = new AtomicInteger(), boxTarget = new AtomicInteger(-1);
    private volatile boolean boxOn;
    Injector(Bridge b) { br = b; }
    private static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) {} }
    private static void press(int k) { Canvas c = Display.currentCanvas(); if (c != null) c.dispatchKeyPressed(k); }
    private static void release(int k) { Canvas c = Display.currentCanvas(); if (c != null) c.dispatchKeyReleased(k); }
    /** real press / release (for on-screen buttons, supports hold). */
    void keyDown(int k) { synchronized (downT) { downT.put(k, System.currentTimeMillis()); } press(k); }
    void keyUp(final int k) {
        long t; synchronized (downT) { Long v = downT.get(k); t = v == null ? 0 : v; }
        long d = Math.max(0, 90 - (System.currentTimeMillis() - t));
        ex.schedule(new Runnable() { public void run() { release(k); } }, d, TimeUnit.MILLISECONDS);
    }
    void tapKey(final int k) { ex.execute(new Runnable() { public void run() { press(k); sleep(85); release(k); sleep(40); } }); }
    void tapTile(final int c, final int r) {
        ex.execute(new Runnable() { public void run() { br.setCursor(c, r); sleep(120); press(Bridge.OK); sleep(90); release(Bridge.OK); sleep(30); } });
    }
    void pan(final int key) {
        if (pendingPan.get() > 3) return;
        pendingPan.incrementAndGet();
        ex.execute(new Runnable() { public void run() {
            try { br.pin(key); sleep(30); press(key); sleep(70); release(key); sleep(45); } finally { pendingPan.decrementAndGet(); }
        } });
    }
    void boxBegin(final int c, final int r) {
        ex.execute(new Runnable() { public void run() { br.setCursor(c, r); sleep(120); press(Bridge.OK); sleep(520); release(Bridge.OK); sleep(100); boxOn = true; } });
    }
    void boxMove(int c, int r) {
        boolean first = boxTarget.getAndSet((c << 8) | r) == -1;
        if (first) ex.execute(new Runnable() { public void run() { int t = boxTarget.getAndSet(-1); if (boxOn && t >= 0) br.setCursor(t >> 8, t & 255); } });
    }
    void boxEnd(final int c, final int r) {
        ex.execute(new Runnable() { public void run() {
            boxTarget.set(-1); br.setCursor(c, r); sleep(120); press(Bridge.OK); sleep(90); release(Bridge.OK); boxOn = false; sleep(30);
        } });
    }
    void shutdown() { ex.shutdownNow(); }
}
