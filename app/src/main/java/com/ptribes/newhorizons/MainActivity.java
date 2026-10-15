package com.ptribes.newhorizons;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import javax.microedition.media.Manager;
import javax.microedition.midlet.MIDlet;

public class MainActivity extends Activity {
    private GameView view;
    private static volatile MIDlet midlet;
    private static boolean started;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Host.ctx = getApplicationContext(); Host.activity = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        view = new GameView(this);
        setContentView(view);
        hideUi();
    }
    private void hideUi() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
    @Override public void onWindowFocusChanged(boolean f) { super.onWindowFocusChanged(f); if (f) hideUi(); }
    @Override protected void onResume() {
        super.onResume(); view.onResumeView();
        if (!started) {
            started = true;
            new Thread(new Runnable() { public void run() {
                try { midlet = (MIDlet) Class.forName("tribes").getDeclaredConstructor().newInstance(); midlet.hostStart(); }
                catch (Throwable t) { android.util.Log.e("PT", "cannot start game", t); }
            } }, "midlet").start();
        } else if (midlet != null) { midlet.hostStart(); }
        Manager.resumeAll();
    }
    @Override protected void onPause() {
        super.onPause();
        if (midlet != null) midlet.hostPause();
        Manager.pauseAll();
    }
    @Override public void onBackPressed() { view.injector().tapKey(Bridge.RSK); } // Back = game's cancel / pause key
    @Override protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) android.os.Process.killProcess(android.os.Process.myPid()); // game keeps static state: always start fresh
    }
}
