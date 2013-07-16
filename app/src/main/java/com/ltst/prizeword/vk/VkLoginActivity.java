package com.ltst.prizeword.vk;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.SherlockActivity;
import com.ltst.prizeword.R;
import com.perm.kate.api.Auth;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 16.07.13.
 */
public class VkLoginActivity extends SherlockActivity {

    private @Nonnull WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mWebView = (WebView) this.findViewById(R.id.activity_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.clearCache(true);

        //Чтобы получать уведомления об окончании загрузки страницы
        mWebView.setWebViewClient(new VkontakteWebViewClient());

        //otherwise CookieManager will fall with java.lang.IllegalStateException: CookieSyncManager::createInstance() needs to be called before CookieSyncManager::getInstance()
        CookieSyncManager.createInstance(this);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        Resources res = this.getResources();
        String url = Auth.getUrl(res.getString(R.string.VK_API_ID), Auth.getSettings());
        mWebView.loadUrl(url);
    }

    private class VkontakteWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            parseUrl(url);
        }
    }

    private void parseUrl(@Nonnull String url) {
        try {
            if(url==null)
                return;
            if(url.startsWith(Auth.redirect_url))
            {
                if(!url.contains("error=")){
                    String[] auth=Auth.parseRedirectUrl(url);
                    Intent intent=new Intent();
                    intent.putExtra(VkAccount.ACCOUNT_ACCESS_TOKEN, auth[0]);
                    intent.putExtra(VkAccount.ACCOUNT_USER_ID, Long.parseLong(auth[1]));
                    setResult(SherlockActivity.RESULT_OK, intent);
                }
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
