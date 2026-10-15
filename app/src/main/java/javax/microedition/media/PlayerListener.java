package javax.microedition.media;
public interface PlayerListener {
    String STARTED = "started", STOPPED = "stopped", END_OF_MEDIA = "endOfMedia", CLOSED = "closed", ERROR = "error",
            DEVICE_UNAVAILABLE = "deviceUnavailable", DEVICE_AVAILABLE = "deviceAvailable", VOLUME_CHANGED = "volumeChanged";
    void playerUpdate(Player player, String event, Object eventData);
}
