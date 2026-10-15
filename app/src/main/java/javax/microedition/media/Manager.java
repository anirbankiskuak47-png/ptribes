package javax.microedition.media;
import java.io.IOException;
import java.io.InputStream;
public final class Manager {
    public static Player createPlayer(InputStream is, String type) throws IOException, MediaException { return new AndroidPlayer(is, type); }
    public static void pauseAll() { AndroidPlayer.pauseAll(); }
    public static void resumeAll() { AndroidPlayer.resumeAll(); }
}
