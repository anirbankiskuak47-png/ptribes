package javax.microedition.lcdui;
public abstract class Canvas extends Displayable {
    public static final int UP = 1, LEFT = 2, RIGHT = 5, DOWN = 6, FIRE = 8, GAME_A = 9, GAME_B = 10, GAME_C = 11, GAME_D = 12;
    public static final int KEY_NUM0 = 48, KEY_NUM1 = 49, KEY_NUM2 = 50, KEY_NUM3 = 51, KEY_NUM4 = 52, KEY_NUM5 = 53,
            KEY_NUM6 = 54, KEY_NUM7 = 55, KEY_NUM8 = 56, KEY_NUM9 = 57, KEY_STAR = 42, KEY_POUND = 35;
    public static volatile Runnable wakeHook;
    private volatile boolean dirty = true;
    protected Canvas() {}
    public int getWidth() { return 240; }
    public int getHeight() { return 320; }
    public void setFullScreenMode(boolean b) {}
    public boolean isDoubleBuffered() { return true; }
    public boolean hasPointerEvents() { return false; }
    public boolean isShown() { return true; }
    public final void repaint() { dirty = true; Runnable h = wakeHook; if (h != null) h.run(); }
    public final void repaint(int x, int y, int w, int h) { repaint(); }
    public final void serviceRepaints() {
        long end = System.currentTimeMillis() + 80;
        while (dirty && System.currentTimeMillis() < end) { try { Thread.sleep(2); } catch (InterruptedException e) { break; } }
    }
    public int getGameAction(int k) {
        switch (k) {
            case 50: case -1: return UP;
            case 56: case -2: return DOWN;
            case 52: case -3: return LEFT;
            case 54: case -4: return RIGHT;
            case 53: case -5: return FIRE;
            case 49: return GAME_A;
            case 51: return GAME_B;
            case 55: return GAME_C;
            case 57: return GAME_D;
            default: return 0;
        }
    }
    public int getKeyCode(int ga) {
        switch (ga) { case UP: return 50; case DOWN: return 56; case LEFT: return 52; case RIGHT: return 54; case FIRE: return 53; default: return 0; }
    }
    public String getKeyName(int k) { return String.valueOf(k); }
    protected abstract void paint(Graphics g);
    protected void keyPressed(int k) {}
    protected void keyReleased(int k) {}
    protected void keyRepeated(int k) {}
    protected void showNotify() {}
    protected void hideNotify() {}
    // host hooks
    public final boolean takeDirty() { boolean d = dirty; dirty = false; return d; }
    public final void paintTo(Graphics g) { paint(g); }
    public final void dispatchKeyPressed(int k) { keyPressed(k); }
    public final void dispatchKeyReleased(int k) { keyReleased(k); }
}
