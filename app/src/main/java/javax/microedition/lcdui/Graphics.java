package javax.microedition.lcdui;
import android.graphics.*;
public class Graphics {
    public static final int HCENTER = 1, VCENTER = 2, LEFT = 4, RIGHT = 8, TOP = 16, BOTTOM = 32, BASELINE = 64, SOLID = 0, DOTTED = 1;
    private final android.graphics.Canvas c; private int base;
    private final Paint fill = new Paint(), line = new Paint(), bm = new Paint(); private Paint text;
    private int color = 0xFF000000, tx, ty;
    public Graphics(Bitmap b) {
        c = new android.graphics.Canvas(b);
        fill.setStyle(Paint.Style.FILL); fill.setAntiAlias(false);
        line.setStyle(Paint.Style.STROKE); line.setStrokeWidth(1f); line.setAntiAlias(false);
        bm.setFilterBitmap(false); bm.setAntiAlias(false);
        base = c.save(); setFont(Font.getDefaultFont()); setColor(0);
    }
    public void resetClip() { c.restoreToCount(base); base = c.save(); }
    public void setColor(int rgb) { color = 0xFF000000 | (rgb & 0xFFFFFF); fill.setColor(color); line.setColor(color); text.setColor(color); }
    public void setColor(int r, int g, int b) { setColor(((r & 255) << 16) | ((g & 255) << 8) | (b & 255)); }
    public void setFont(Font f) { text = new Paint(); text.setAntiAlias(true); text.setTypeface(f.tf); text.setTextSize(f.px); text.setColor(color); }
    public void translate(int x, int y) { tx += x; ty += y; }
    public void setClip(int x, int y, int w, int h) { c.restoreToCount(base); base = c.save(); c.clipRect(x + tx, y + ty, x + tx + w, y + ty + h); }
    public void clipRect(int x, int y, int w, int h) { c.clipRect(x + tx, y + ty, x + tx + w, y + ty + h); }
    public void fillRect(int x, int y, int w, int h) { if (w > 0 && h > 0) c.drawRect(x + tx, y + ty, x + tx + w, y + ty + h, fill); }
    public void drawRect(int x, int y, int w, int h) { if (w >= 0 && h >= 0) c.drawRect(x + tx + .5f, y + ty + .5f, x + tx + w + .5f, y + ty + h + .5f, line); }
    public void drawLine(int x1, int y1, int x2, int y2) { c.drawLine(x1 + tx + .5f, y1 + ty + .5f, x2 + tx + .5f, y2 + ty + .5f, line); }
    public void fillArc(int x, int y, int w, int h, int start, int arc) {
        RectF r = new RectF(x + tx, y + ty, x + tx + w, y + ty + h);
        if (Math.abs(arc) >= 360) c.drawOval(r, fill); else c.drawArc(r, -start, -arc, true, fill);
    }
    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        Path p = new Path(); p.moveTo(x1 + tx, y1 + ty); p.lineTo(x2 + tx, y2 + ty); p.lineTo(x3 + tx, y3 + ty); p.close(); c.drawPath(p, fill);
    }
    public void drawString(String s, int x, int y, int anchor) {
        float w = text.measureText(s), px = x + tx; Paint.FontMetrics fm = text.getFontMetrics();
        if ((anchor & HCENTER) != 0) px -= w / 2; else if ((anchor & RIGHT) != 0) px -= w;
        float by = (anchor & BOTTOM) != 0 ? y + ty - fm.descent : (anchor & BASELINE) != 0 ? y + ty : y + ty - fm.ascent;
        c.drawText(s, px, by, text);
    }
    private int ax(int x, int w, int a) { return (a & HCENTER) != 0 ? x - w / 2 : (a & RIGHT) != 0 ? x - w : x; }
    private int ay(int y, int h, int a) { return (a & VCENTER) != 0 ? y - h / 2 : (a & BOTTOM) != 0 ? y - h : y; }
    public void drawImage(Image img, int x, int y, int anchor) {
        int w = img.bmp.getWidth(), h = img.bmp.getHeight(); c.drawBitmap(img.bmp, ax(x, w, anchor) + tx, ay(y, h, anchor) + ty, bm);
    }
    public void drawRegion(Image src, int xs, int ys, int w, int h, int transform, int xd, int yd, int anchor) {
        if (w <= 0 || h <= 0) return;
        if (transform == 0) {
            int dx = ax(xd, w, anchor) + tx, dy = ay(yd, h, anchor) + ty;
            c.drawBitmap(src.bmp, new Rect(xs, ys, xs + w, ys + h), new Rect(dx, dy, dx + w, dy + h), bm); return;
        }
        Matrix m = new Matrix();
        switch (transform) {
            case 1: m.postScale(1, -1); break;
            case 2: m.postScale(-1, 1); break;
            case 3: m.postRotate(180); break;
            case 4: m.postScale(-1, 1); m.postRotate(270); break;
            case 5: m.postRotate(90); break;
            case 6: m.postRotate(270); break;
            case 7: m.postScale(-1, 1); m.postRotate(90); break;
        }
        Bitmap t = Bitmap.createBitmap(src.bmp, xs, ys, w, h, m, false);
        c.drawBitmap(t, ax(xd, t.getWidth(), anchor) + tx, ay(yd, t.getHeight(), anchor) + ty, bm);
    }
}
