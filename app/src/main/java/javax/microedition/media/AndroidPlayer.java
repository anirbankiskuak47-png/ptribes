package javax.microedition.media;
import android.media.MediaPlayer;
import com.ptribes.newhorizons.Host;
import java.io.*;
import java.util.ArrayList;
import javax.microedition.media.control.VolumeControl;
final class AndroidPlayer implements Player {
    private static final ArrayList<AndroidPlayer> ALL = new ArrayList<AndroidPlayer>();
    private MediaPlayer mp; private File f; private int state = UNREALIZED, vol = 100; private final String type;
    private final VolumeControl vc = new VolumeControl() {
        public void setMute(boolean m) {} public boolean isMuted() { return false; } public int getLevel() { return vol; }
        public int setLevel(int l) { vol = Math.max(0, Math.min(100, l)); if (mp != null) try { mp.setVolume(vol / 100f, vol / 100f); } catch (Exception e) {} return vol; }
    };
    AndroidPlayer(InputStream is, String type) throws IOException {
        this.type = type;
        f = File.createTempFile("mus", ".mid", Host.ctx.getCacheDir());
        FileOutputStream o = new FileOutputStream(f); byte[] b = new byte[8192]; int r; while ((r = is.read(b)) > 0) o.write(b, 0, r); o.close();
        synchronized (ALL) { ALL.add(this); }
    }
    static void pauseAll() { synchronized (ALL) { for (AndroidPlayer p : ALL) if (p.state == STARTED && p.mp != null) try { p.mp.pause(); } catch (Exception e) {} } }
    static void resumeAll() { synchronized (ALL) { for (AndroidPlayer p : ALL) if (p.state == STARTED && p.mp != null) try { p.mp.start(); } catch (Exception e) {} } }
    public synchronized void realize() {
        if (state != UNREALIZED) return;
        try { mp = new MediaPlayer(); mp.setDataSource(f.getPath()); mp.setLooping(true); mp.prepare(); mp.setVolume(vol / 100f, vol / 100f); }
        catch (Exception e) { android.util.Log.w("PT", "music", e); mp = null; }
        state = REALIZED;
    }
    public synchronized void prefetch() { realize(); if (state < PREFETCHED) state = PREFETCHED; }
    public synchronized void start() { prefetch(); if (mp != null) try { mp.start(); } catch (Exception e) {} state = STARTED; }
    public synchronized void stop() { if (mp != null && state == STARTED) try { mp.pause(); } catch (Exception e) {} if (state == STARTED) state = PREFETCHED; }
    public void deallocate() { stop(); }
    public synchronized void close() { if (mp != null) try { mp.release(); } catch (Exception e) {} mp = null; state = CLOSED; if (f != null) f.delete(); synchronized (ALL) { ALL.remove(this); } }
    public synchronized int getState() { return state; }
    public synchronized long setMediaTime(long t) { if (mp != null) try { mp.seekTo((int) (t / 1000)); } catch (Exception e) {} return t; }
    public long getMediaTime() { try { return mp == null ? 0 : mp.getCurrentPosition() * 1000L; } catch (Exception e) { return 0; } }
    public long getDuration() { return -1; }
    public String getContentType() { return type; }
    public void setLoopCount(int c) {}
    public void addPlayerListener(PlayerListener l) {}
    public void removePlayerListener(PlayerListener l) {}
    public Control getControl(String n) { return n != null && n.endsWith("VolumeControl") ? vc : null; }
    public Control[] getControls() { return new Control[] { vc }; }
}
