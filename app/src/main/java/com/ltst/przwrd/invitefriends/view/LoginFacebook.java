package com.ltst.przwrd.invitefriends.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockActivity;
import com.ltst.przwrd.R;
import com.ltst.przwrd.app.SharedPreferencesValues;
import com.ltst.przwrd.login.model.SocialParser;
import com.ltst.przwrd.rest.RestParams;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by dmitry on 9/30/13.
 */
public class LoginFacebook extends SherlockActivity {

    static final public @Nonnull String BF_FACEBOOK_TOKEN = "facebook_token";

    private @Nonnull WebView mWebView;
    private @Nonnull ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        mWebView = (WebView) this.findViewById(R.id.activity_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.clearCache(true);

        mProgressBar = (ProgressBar) this.findViewById(R.id.activity_webview_progressbar);
        mProgressBar.setVisibility(ProgressBar.GONE);

        //Чтобы получать уведомления об окончании загрузки страницы
        mWebView.setWebViewClient(new SocialWebViewClient());

        //otherwise CookieManager will fall with java.lang.IllegalStateException: CookieSyncManager::createInstance() needs to be called before CookieSyncManager::getInstance()
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        Bundle extras = getIntent().getExtras();
        mWebView.loadUrl(RestParams.URL_FB_LOGIN);

        setTitle(R.string.fb_login_fragment_title);
    }

    private class SocialWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            parseUrl(url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mProgressBar.setVisibility(ProgressBar.GONE);
        }
    }

    private void parseUrl(@Nonnull String url) {
        try {
            if (url == null)
                return;
//            Log.d(NavigationActivity.LOG_TAG, "SocialLogin PARSE URL = "+url);
            if (url.startsWith(RestParams.URL_FB_TOKEN))
            {
                if (!url.contains("error=")) {
//                    String[] auth = Auth.parseRedirectUrl(url);
                    String[] auth;

                    auth = SocialParser.parseFbRedirectUrl(url);
                    final @Nullable String access_token = auth[0];
                    SharedPreferencesValues.setFacebookToken(this,access_token);

                    Intent intent = new Intent();
                    intent.putExtra(BF_FACEBOOK_TOKEN, access_token);
                    setResult(RESULT_OK, intent);
                }
                else
                {
                    SharedPreferencesValues.setFacebookToken(this,null);
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);

                }
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
