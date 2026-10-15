package javax.microedition.lcdui;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
public class Image {
    final Bitmap bmp; final boolean mutable;
    Image(Bitmap b, boolean m) { bmp = b; mutable = m; }
    public static Image createImage(int w, int h) {
        if (w <= 0 || h <= 0) throw new IllegalArgumentException();
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); b.eraseColor(0xFFFFFFFF);
        return new Image(b, true);
    }
    public static Image createImage(String name) throws IOException {
        String n = name.startsWith("/") ? name.substring(1) : name;
        InputStream in = Image.class.getClassLoader().getResourceAsStream(n);
        if (in == null) throw new IOException("Resource not found: " + name);
        try {
            ByteArrayOutputStream o = new ByteArrayOutputStream(); byte[] buf = new byte[8192]; int r;
            while ((r = in.read(buf)) > 0) o.write(buf, 0, r);
            byte[] d = o.toByteArray(); return createImage(d, 0, d.length);
        } finally { in.close(); }
    }
    public static Image createImage(byte[] d, int off, int len) {
        BitmapFactory.Options o = new BitmapFactory.Options(); o.inScaled = false; o.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap b = BitmapFactory.decodeByteArray(d, off, len, o);
        if (b == null) throw new IllegalArgumentException("bad image data");
        return new Image(b, false);
    }
    public Graphics getGraphics() { if (!mutable) throw new IllegalStateException(); return new Graphics(bmp); }
    public int getWidth() { return bmp.getWidth(); }
    public int getHeight() { return bmp.getHeight(); }
    public boolean isMutable() { return mutable; }
}
