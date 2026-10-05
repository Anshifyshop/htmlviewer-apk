package com.anshify.htmlviewer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class MainActivity extends Activity {
    WebView w;
    ImageView sp;
    long t0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        t0 = SystemClock.uptimeMillis();
        FrameLayout root = new FrameLayout(this);
        w = new WebView(this);
        root.addView(w, new FrameLayout.LayoutParams(-1, -1));
        sp = new ImageView(this);
        sp.setBackgroundColor(Color.parseColor("#10151c"));
        sp.setImageResource(R.drawable.splash);
        sp.setScaleType(ImageView.ScaleType.CENTER);
        root.addView(sp, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);

        WebSettings s = w.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        w.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView v, String u) { hideSplash(); }
        });
        w.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest r) {
                runOnUiThread(new Runnable() { public void run() { r.grant(r.getResources()); } });
            }
            @Override
            public void onGeolocationPermissionsShowPrompt(String o, GeolocationPermissions.Callback cb) {
                cb.invoke(o, true, false);
            }
        });
        String[] need = new String[]{"android.permission.CAMERA","android.permission.RECORD_AUDIO","android.permission.ACCESS_FINE_LOCATION","android.permission.POST_NOTIFICATIONS"};
        if (need.length > 0 && Build.VERSION.SDK_INT >= 23) requestPermissions(need, 1);
        w.loadUrl("file:///android_asset/index.html");
    }

    void hideSplash() {
        long d = Math.max(0, 1200 - (SystemClock.uptimeMillis() - t0));
        sp.postDelayed(new Runnable() {
            public void run() {
                sp.animate().alpha(0f).setDuration(300).withEndAction(new Runnable() {
                    public void run() { sp.setVisibility(View.GONE); }
                });
            }
        }, d);
    }

    @Override
    public void onWindowFocusChanged(boolean f) {
        super.onWindowFocusChanged(f);
        if (f) w.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    public void onBackPressed() {
        if (w.canGoBack()) w.goBack(); else super.onBackPressed();
    }

    @Override protected void onPause() { super.onPause(); w.onPause(); }
    @Override protected void onResume() { super.onResume(); w.onResume(); }
}
