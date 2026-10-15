package com.ptribes.newhorizons;
import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;

/**
 * Landscape view. The 240x320 game is drawn big in the middle; touch works like a modern strategy game:
 * drag = pan, tap = select / give order, long-press + drag = box select, two-finger tap = cancel.
 * Small floating buttons sit on the sides for the game's menu and hot-keys.
 */
public final class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final int BG = 0xFF14100A;
    private final Bitmap screen = Bitmap.createBitmap(240, 320, Bitmap.Config.ARGB_8888);
    private final Graphics screenG = new Graphics(screen);
    private final Bridge bridge = new Bridge();
    private final Injector inj = new Injector(bridge);
    private final Object lock = new Object();
    private final Paint pix = new Paint(), fillP = new Paint(), strokeP = new Paint(), textP = new Paint();
    private final Rect src = new Rect(0, 0, 240, 320);
    private final RectF dst = new RectF();
    private final Handler ui = new Handler(Looper.getMainLooper());
    private volatile boolean running, uiDirty = true, surfaceOk;
    private Thread thread;
    private int W, H; private float s = 1, gx, gy;

    // ---- floating buttons
    private static final class Btn {
        final String label; final int code, color; float cx, cy, r; int pid = -1;
        Btn(String l, int c, int col) { label = l; code = c; color = col; }
    }
    private final Btn menu = new Btn("MENU", Bridge.LSK, 0xFF2E9E3A), back = new Btn("BACK", Bridge.RSK, 0xFFC62828);
    private final Btn[] btns = { menu, back, new Btn("7", 55, 0xFF8D6E3F), new Btn("9", 57, 0xFF8D6E3F),
            new Btn("!", 49, 0xFF8D6E3F), new Btn("MAP", 51, 0xFF8D6E3F), new Btn("?", 48, 0xFF8D6E3F) };

    // ---- game-area gesture
    private static final int IDLE = 0, PENDING = 1, DRAG = 2, BOX = 3;
    private int gMode = IDLE, gPid = -1; private float sx, sy, lx, ly, accX, accY; private long twoT; private boolean twoActive;
    private Runnable longPress;

    public GameView(Context c) {
        super(c);
        getHolder().addCallback(this);
        pix.setFilterBitmap(false); pix.setAntiAlias(false);
        fillP.setStyle(Paint.Style.FILL); fillP.setAntiAlias(true);
        strokeP.setStyle(Paint.Style.STROKE); strokeP.setAntiAlias(true); strokeP.setColor(0xAAFFFFFF);
        textP.setAntiAlias(true); textP.setColor(Color.WHITE); textP.setTextAlign(Paint.Align.CENTER); textP.setTypeface(Typeface.DEFAULT_BOLD);
        setFocusable(true);
        Canvas.wakeHook = new Runnable() { public void run() { wake(); } };
    }
    private void wake() { synchronized (lock) { lock.notifyAll(); } }
    public void onResumeView() { uiDirty = true; wake(); }
    public Injector injector() { return inj; }

    // ---- surface / render loop
    public void surfaceCreated(SurfaceHolder h) { surfaceOk = true; uiDirty = true; running = true; thread = new Thread(this, "render"); thread.start(); }
    public void surfaceChanged(SurfaceHolder h, int f, int w, int hh) { W = w; H = hh; layout(); uiDirty = true; wake(); }
    public void surfaceDestroyed(SurfaceHolder h) {
        surfaceOk = false; running = false; wake();
        try { if (thread != null) thread.join(500); } catch (InterruptedException e) {}
    }
    private void layout() {
        s = Math.min(H / 320f, W * 0.62f / 240f);
        float gw = 240 * s, gh = 320 * s; gx = (W - gw) / 2f; gy = (H - gh) / 2f;
        dst.set(gx, gy, gx + gw, gy + gh);
        float lw = Math.max(gx, H * 0.25f), big = Math.min(lw * 0.34f, H * 0.14f), small = Math.min(lw * 0.26f, H * 0.095f);
        float lcx = Math.min(gx, lw) / 2f, rcx = W - lcx;
        place(menu, lcx, H * 0.80f, big);
        place(back, rcx, H * 0.80f, big);
        place(btns[2], lcx, H * 0.17f, small); place(btns[3], lcx, H * 0.37f, small);
        place(btns[4], rcx, H * 0.17f, small); place(btns[5], rcx, H * 0.37f, small); place(btns[6], rcx, H * 0.57f, small);
    }
    private static void place(Btn b, float x, float y, float r) { b.cx = x; b.cy = y; b.r = r; }

    public void run() {
        while (running) {
            Canvas cv = Display.currentCanvas();
            boolean frame = uiDirty; uiDirty = false;
            if (cv != null && cv.takeDirty()) {
                try { screenG.resetClip(); cv.paintTo(screenG); } catch (Throwable t) { android.util.Log.e("PT", "paint", t); }
                frame = true;
            }
            if (frame && surfaceOk) present();
            synchronized (lock) { if (running && !uiDirty) { try { lock.wait(12); } catch (InterruptedException e) { return; } } }
        }
    }
    private void present() {
        android.graphics.Canvas c = null;
        try {
            c = getHolder().lockCanvas();
            if (c == null) return;
            c.drawColor(BG);
            c.drawBitmap(screen, src, dst, pix);
            for (Btn b : btns) drawBtn(c, b);
        } catch (Throwable t) { android.util.Log.e("PT", "present", t); }
        finally { if (c != null) try { getHolder().unlockCanvasAndPost(c); } catch (Throwable t) {} }
    }
    private void drawBtn(android.graphics.Canvas c, Btn b) {
        boolean on = b.pid != -1;
        fillP.setColor(b.color); fillP.setAlpha(on ? 0xE0 : 0x66);
        c.drawCircle(b.cx, b.cy, b.r, fillP);
        strokeP.setStrokeWidth(Math.max(2f, b.r * 0.06f)); strokeP.setAlpha(on ? 0xFF : 0x99);
        c.drawCircle(b.cx, b.cy, b.r, strokeP);
        textP.setTextSize(b.label.length() > 2 ? b.r * 0.42f : b.r * 0.8f); textP.setAlpha(on ? 0xFF : 0xCC);
        c.drawText(b.label, b.cx, b.cy + textP.getTextSize() * 0.35f, textP);
    }

    // ---- touch
    private int tileCol(float x) { return Math.max(0, Math.min(Bridge.COLS - 1, (int) (((x - gx) / s) / 22f))); }
    private int tileRow(float y) { return Math.max(0, Math.min(Bridge.ROWS - 1, (int) (((y - gy) / s) / 16f))); }
    private boolean inGame(float x, float y) { return dst.contains(x, y); }
    private Btn hit(float x, float y) {
        for (Btn b : btns) { float dx = x - b.cx, dy = y - b.cy; float r = b.r * 1.15f; if (dx * dx + dy * dy <= r * r) return b; }
        return null;
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        int a = e.getActionMasked(), idx = e.getActionIndex(), id = e.getPointerId(idx);
        float x = e.getX(idx), y = e.getY(idx);
        switch (a) {
            case MotionEvent.ACTION_DOWN: case MotionEvent.ACTION_POINTER_DOWN: {
                Btn b = hit(x, y);
                if (b != null) { b.pid = id; inj.keyDown(b.code); uiDirty = true; wake(); return true; }
                if (inGame(x, y)) {
                    if (gPid == -1) { gPid = id; gMode = PENDING; sx = lx = x; sy = ly = y; accX = accY = 0; startLongPress(); }
                    else { cancelGesture(); twoT = System.currentTimeMillis(); twoActive = true; }
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                for (int i = 0; i < e.getPointerCount(); i++) if (e.getPointerId(i) == gPid) gMove(e.getX(i), e.getY(i));
                return true;
            }
            case MotionEvent.ACTION_UP: case MotionEvent.ACTION_POINTER_UP: case MotionEvent.ACTION_CANCEL: {
                for (Btn b : btns) if (b.pid == id) { b.pid = -1; inj.keyUp(b.code); uiDirty = true; wake(); return true; }
                if (id == gPid) { if (a == MotionEvent.ACTION_CANCEL) cancelGesture(); else gUp(x, y); }
                else if (twoActive && a != MotionEvent.ACTION_CANCEL) {
                    twoActive = false;
                    if (System.currentTimeMillis() - twoT < 350) inj.tapKey(Bridge.RSK); // two-finger tap = cancel / deselect
                }
                return true;
            }
        }
        return true;
    }
    private void startLongPress() {
        longPress = new Runnable() { public void run() {
            if (gMode == PENDING && bridge.isDirect()) { gMode = BOX; inj.boxBegin(tileCol(sx), tileRow(sy)); }
        } };
        ui.postDelayed(longPress, 380);
    }
    private void cancelGesture() { if (longPress != null) ui.removeCallbacks(longPress); gMode = IDLE; gPid = -1; }
    private void gMove(float x, float y) {
        float slop = H * 0.03f;
        if (gMode == PENDING && Math.hypot(x - sx, y - sy) > slop) { if (longPress != null) ui.removeCallbacks(longPress); gMode = DRAG; lx = x; ly = y; }
        if (gMode == DRAG) {
            accX += x - lx; accY += y - ly; lx = x; ly = y;
            if (bridge.isDirect()) {
                float tw = 22 * s, th = 16 * s; // dragging the map: it follows the finger
                while (accX >= tw) { accX -= tw; inj.pan(Bridge.LEFT); }
                while (accX <= -tw) { accX += tw; inj.pan(Bridge.RIGHT); }
                while (accY >= th) { accY -= th; inj.pan(Bridge.UP); }
                while (accY <= -th) { accY += th; inj.pan(Bridge.DOWN); }
            } else { // menus: swipe to move the highlight
                float t = H * 0.09f;
                while (accY >= t) { accY -= t; inj.tapKey(Bridge.UP); }
                while (accY <= -t) { accY += t; inj.tapKey(Bridge.DOWN); }
                while (accX >= t) { accX -= t; inj.tapKey(Bridge.RIGHT); }
                while (accX <= -t) { accX += t; inj.tapKey(Bridge.LEFT); }
            }
        } else if (gMode == BOX) inj.boxMove(tileCol(x), tileRow(y));
    }
    private void gUp(float x, float y) {
        int mode = gMode; cancelGesture();
        if (mode == BOX) { inj.boxEnd(tileCol(x), tileRow(y)); return; }
        if (mode != PENDING) return;
        float lx2 = (x - gx) / s, ly2 = (y - gy) / s;
        if (ly2 >= 300) { // game's own bottom bar: soft keys, groups, minimap
            if (lx2 < 45) inj.tapKey(Bridge.LSK); else if (lx2 > 195) inj.tapKey(Bridge.RSK);
            else if (lx2 >= 88 && lx2 < 120) inj.tapKey(55); else if (lx2 >= 120 && lx2 < 158) inj.tapKey(57);
            else if (lx2 >= 160 && lx2 < 200) inj.tapKey(51);
            return;
        }
        if (bridge.isDirect()) { if (ly2 >= 20) inj.tapTile(tileCol(x), tileRow(y)); }
        else inj.tapKey(Bridge.OK);
    }
}
