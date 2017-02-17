package com.spinytech.webdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.spinytech.macore.MaApplication;
import com.spinytech.macore.router.LocalRouter;
import com.spinytech.macore.router.RouterRequest;
import com.spinytech.macore.router.RouterRequestUtil;

public class WebActivity extends AppCompatActivity {

    private WebView mContentWv;
    private static String protocole = "wutongke://";

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
        if (url.indexOf(protocole) >= 0) {
            String command = url.substring(protocole.length());
            try {
                LocalRouter.getInstance(MaApplication.getMaApplication()).route(this, RouterRequestUtil.url(command));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!TextUtils.isEmpty(url) && url.startsWith(protocole)) {
                dispatchAction(url);
            } else {
                mContentWv.loadUrl(url);
            }
            return true;
        }
    }
}
