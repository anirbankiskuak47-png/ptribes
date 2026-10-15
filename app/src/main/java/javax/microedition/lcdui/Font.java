package javax.microedition.lcdui;
import android.graphics.Paint;
import android.graphics.Typeface;
public final class Font {
    public static final int FACE_SYSTEM = 0, FACE_MONOSPACE = 32, FACE_PROPORTIONAL = 64;
    public static final int STYLE_PLAIN = 0, STYLE_BOLD = 1, STYLE_ITALIC = 2, STYLE_UNDERLINED = 4;
    public static final int SIZE_SMALL = 8, SIZE_MEDIUM = 0, SIZE_LARGE = 16;
    final Typeface tf; final float px; private final Paint m = new Paint();
    private Font(int face, int style, int size) {
        int ts = ((style & STYLE_BOLD) != 0 ? Typeface.BOLD : 0) | ((style & STYLE_ITALIC) != 0 ? Typeface.ITALIC : 0);
        tf = Typeface.create(face == FACE_MONOSPACE ? Typeface.MONOSPACE : Typeface.SANS_SERIF, ts);
        px = size == SIZE_SMALL ? 11f : size == SIZE_LARGE ? 17f : 13f;
        m.setTypeface(tf); m.setTextSize(px); m.setAntiAlias(true);
    }
    public static Font getFont(int face, int style, int size) { return new Font(face, style, size); }
    public static Font getDefaultFont() { return new Font(FACE_SYSTEM, STYLE_PLAIN, SIZE_MEDIUM); }
    public int getHeight() { Paint.FontMetrics f = m.getFontMetrics(); return (int) Math.ceil(f.descent - f.ascent); }
    public int getBaselinePosition() { return (int) Math.ceil(-m.getFontMetrics().ascent); }
    public int stringWidth(String s) { return (int) Math.ceil(m.measureText(s)); }
    public int charWidth(char c) { return (int) Math.ceil(m.measureText(String.valueOf(c))); }
}
