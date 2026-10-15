package com.ptribes.newhorizons;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
/** Reads / writes the game's cursor and mode fields by reflection (class "f", obfuscated names). */
final class Bridge {
    static final int UP = 50, DOWN = 56, LEFT = 52, RIGHT = 54, OK = 53, LSK = -6, RSK = -7;
    static final int COLS = 11, ROWS = 19;
    private Class<?> cls; private Field fy, fA, fQ, fw;
    private synchronized boolean bind() {
        Canvas c = Display.currentCanvas();
        if (c == null) return false;
        if (c.getClass() != cls) {
            cls = c.getClass(); fy = fA = fQ = fw = null;
            if ("f".equals(cls.getName())) {
                for (Field f : cls.getDeclaredFields()) {
                    if (f.getType() != byte.class || Modifier.isStatic(f.getModifiers())) continue;
                    String n = f.getName();
                    if (n.equals("y")) fy = f; else if (n.equals("A")) fA = f; else if (n.equals("Q")) fQ = f; else if (n.equals("w")) fw = f;
                    f.setAccessible(true);
                }
            }
        }
        return fy != null && fA != null && fQ != null && fw != null;
    }
    /** true while the real map is shown and a click on a tile makes sense (Q==28, w==0 or 1). */
    boolean isDirect() {
        try {
            if (!bind()) return false;
            Canvas c = Display.currentCanvas();
            int q = fQ.getByte(c), w = fw.getByte(c);
            return q == 28 && (w == 0 || w == 1);
        } catch (Exception e) { return false; }
    }
    void setCursor(int col, int row) {
        try { if (!bind()) return; Canvas c = Display.currentCanvas();
            fy.setByte(c, (byte) Math.max(0, Math.min(COLS - 1, col))); fA.setByte(c, (byte) Math.max(0, Math.min(ROWS - 1, row)));
        } catch (Exception e) {}
    }
    /** Push the cursor to the edge in the direction of a pan so the next arrow press scrolls the camera. */
    void pin(int key) {
        try { if (!bind()) return; Canvas c = Display.currentCanvas();
            if (key == LEFT) fy.setByte(c, (byte) 0); else if (key == RIGHT) fy.setByte(c, (byte) (COLS - 1));
            else if (key == UP) fA.setByte(c, (byte) 0); else if (key == DOWN) fA.setByte(c, (byte) (ROWS - 1));
        } catch (Exception e) {}
    }
}
