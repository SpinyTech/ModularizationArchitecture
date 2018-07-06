package com.spinytech.webdemo;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.spinytech.macore.MaApplication;
import com.spinytech.macore.router.LocalRouter;
import com.spinytech.macore.router.RouterRequest;

public class WebDemoActivity extends Activity {

    private WebView mContentWv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mContentWv = (WebView) findViewById(R.id.web);
        mContentWv.setWebViewClient(new MyWebViewClient());
        mContentWv.getSettings().setBuiltInZoomControls(true);
        mContentWv.getSettings().setJavaScriptEnabled(true);
        mContentWv.getSettings().setSupportZoom(true);
        mContentWv.getSettings().setUseWideViewPort(true);
        mContentWv.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mContentWv.getSettings().setLoadWithOverviewMode(true);
        mContentWv.loadUrl("file:///android_asset/page.html");
    }


    public void dispatchAction(String url) {
        if (url.indexOf("your_protocol://") >= 0) {
            String command = url.substring("your_protocol://".length());
            try {
                LocalRouter.getInstance(MaApplication.getMaApplication()).route(this, new RouterRequest.Builder(this).url(command).build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!TextUtils.isEmpty(url) && url.startsWith("your_protocol://")) {
                dispatchAction(url);
            } else {
                mContentWv.loadUrl(url);
            }
            return true;
        }
    }
}
