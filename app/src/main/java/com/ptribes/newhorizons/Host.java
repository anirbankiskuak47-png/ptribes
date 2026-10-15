package com.ptribes.newhorizons;
import android.app.Activity;
import android.content.Context;
import java.util.HashMap;
import java.util.Map;
/** Global hooks used by the J2ME compatibility layer. */
public final class Host {
    public static volatile Context ctx;
    public static volatile Activity activity;
    public static final Map<String, String> props = new HashMap<String, String>();
    static {
        props.put("MIDlet-Name", "Prehistoric Tribes");
        props.put("MIDlet-Version", "0.1.28");
        props.put("MIDlet-Vendor", "THQ Wireless");
        props.put("GameLinkEnabled", "false");
        props.put("Cheat-Support", "off");
    }
    public static void exit() {
        final Activity a = activity;
        if (a != null) a.runOnUiThread(new Runnable() { public void run() { a.finish(); } });
    }
    private Host() {}
}
