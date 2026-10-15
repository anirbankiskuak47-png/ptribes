package javax.microedition.lcdui;
import javax.microedition.midlet.MIDlet;
public class Display {
    private static Display inst;
    private static volatile Displayable cur;
    public static synchronized Display getDisplay(MIDlet m) { if (inst == null) inst = new Display(); return inst; }
    public void setCurrent(Displayable d) { cur = d; if (d instanceof Canvas) ((Canvas) d).repaint(); }
    public Displayable getCurrent() { return cur; }
    public static Canvas currentCanvas() { Displayable d = cur; return d instanceof Canvas ? (Canvas) d : null; }
}
