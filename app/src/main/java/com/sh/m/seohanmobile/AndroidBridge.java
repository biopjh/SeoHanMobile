package com.sh.m.seohanmobile;

import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.text.SimpleDateFormat;
import java.util.Date;


public class AndroidBridge {

    private final Handler handler = new Handler();
    private WebView mWebView;

    @JavascriptInterface
    public void testMove(final String arg) { // must be final
        handler.post(new Runnable() {
           @Override
            public void run() {
                // 원하는 동작
                mWebView.loadUrl(arg);
            }
        });
    }

}
