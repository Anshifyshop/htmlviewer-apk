package com.anshify.htmlviewer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.webkit.ValueCallback;
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
    ValueCallback<Uri[]> fc;
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
            public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (fc != null) fc.onReceiveValue(null);
                fc = cb;
                try { startActivityForResult(p.createIntent(), 77); }
                catch (Exception e) { fc = null; return false; }
                return true;
            }
            @Override
            public void onPermissionRequest(final PermissionRequest r) {
                runOnUiThread(new Runnable() { public void run() { r.grant(r.getResources()); } });
            }
            @Override
            public void onGeolocationPermissionsShowPrompt(String o, GeolocationPermissions.Callback cb) {
                cb.invoke(o, true, false);
            }
        });
        String[] need = new String[]{"android.permission.CAMERA","android.permission.RECORD_AUDIO","android.permission.ACCESS_FINE_LOCATION","android.permission.POST_NOTIFICATIONS","android.permission.READ_EXTERNAL_STORAGE","android.permission.WRITE_EXTERNAL_STORAGE","android.permission.READ_MEDIA_IMAGES","android.permission.READ_MEDIA_VIDEO","android.permission.READ_MEDIA_AUDIO","android.permission.BLUETOOTH_CONNECT","android.permission.BLUETOOTH_SCAN","android.permission.READ_CONTACTS","android.permission.READ_CALENDAR","android.permission.READ_PHONE_STATE"};
        if (need.length > 0 && Build.VERSION.SDK_INT >= 23) requestPermissions(need, 1);
        if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) { try { startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName()))); } catch (Exception e) {} }
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
    protected void onActivityResult(int rq, int rs, Intent d) {
        super.onActivityResult(rq, rs, d);
        if (rq == 77 && fc != null) { fc.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(rs, d)); fc = null; }
    }

    @Override
    public void onBackPressed() {
        if (w.canGoBack()) w.goBack(); else super.onBackPressed();
    }

    @Override protected void onPause() { super.onPause(); w.onPause(); }
    @Override protected void onResume() { super.onResume(); w.onResume(); }
}
