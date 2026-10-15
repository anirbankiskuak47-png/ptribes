package javax.microedition.midlet;
import com.ptribes.newhorizons.Host;
public abstract class MIDlet {
    protected MIDlet() {}
    protected abstract void startApp() throws MIDletStateChangeException;
    protected abstract void pauseApp();
    protected abstract void destroyApp(boolean unconditional) throws MIDletStateChangeException;
    public final void notifyDestroyed() { Host.exit(); }
    public final void notifyPaused() {}
    public final void resumeRequest() {}
    public final String getAppProperty(String key) { return Host.props.get(key); }
    public final boolean platformRequest(String url) { return false; }
    public final void hostStart() { try { startApp(); } catch (Throwable t) { android.util.Log.e("PT", "startApp", t); } }
    public final void hostPause() { try { pauseApp(); } catch (Throwable t) { android.util.Log.e("PT", "pauseApp", t); } }
    public final void hostDestroy() { try { destroyApp(true); } catch (Throwable t) { android.util.Log.e("PT", "destroyApp", t); } }
}
