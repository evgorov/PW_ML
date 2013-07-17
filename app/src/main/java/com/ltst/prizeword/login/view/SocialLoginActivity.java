package com.ltst.prizeword.login.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.SherlockActivity;
import com.ltst.prizeword.R;
import com.ltst.prizeword.rest.RestParams;
import com.ltst.prizeword.login.model.SocialParser;
import com.ltst.prizeword.login.vk.VkAccount;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 16.07.13.
 */
public class SocialLoginActivity extends SherlockActivity {

    static public final @Nonnull String PROVEDER_ID = "prov_id";

    private @Nonnull WebView mWebView;

    private String pProviderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mWebView = (WebView) this.findViewById(R.id.activity_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.clearCache(true);

        //Чтобы получать уведомления об окончании загрузки страницы
        mWebView.setWebViewClient(new VkWebViewClient());

        //otherwise CookieManager will fall with java.lang.IllegalStateException: CookieSyncManager::createInstance() needs to be called before CookieSyncManager::getInstance()
        CookieSyncManager.createInstance(this);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        Bundle extras = getIntent().getExtras();
        pProviderId = extras.getString(PROVEDER_ID);
        Log.d(VkAccount.LOG_TAG, "PROVIDER_ID = "+pProviderId);

        if(pProviderId.equals(VkLoginFragment.FRAGMENT_ID)) {
            loadUrl(RestParams.VK_LOGIN_URL);
            setTitle(R.string.vk_login_fragment_title);
        }
        else if(pProviderId.equals(FbLoginFragment.FRAGMENT_ID)) {
            loadUrl(RestParams.FB_LOGIN_URL);
            setTitle(R.string.fb_login_fragment_title);
        }

    }

    private class VkWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            parseUrl(url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }

    private void loadUrl(String url){
        mWebView.loadUrl(url);
    }

    private void parseUrl(@Nonnull String url) {
        try {
            if(url==null)
                return;
            Log.d(VkAccount.LOG_TAG, "PARSE URL = "+url);
            if(url.startsWith(RestParams.VK_TOKEN_URL) || url.startsWith(RestParams.FB_TOKEN_URL))
            {
                if(!url.contains("error=")){
//                    String[] auth = Auth.parseRedirectUrl(url);
                    String[] auth;
                    if(pProviderId.equals(VkLoginFragment.FRAGMENT_ID))
                        auth = SocialParser.parseVkRedirectUrl(url);
                    else if(pProviderId.equals(FbLoginFragment.FRAGMENT_ID))
                        auth = SocialParser.parseFbRedirectUrl(url);
                    else
                        return;

                    Intent intent=new Intent();
                    intent.putExtra(VkAccount.ACCOUNT_ACCESS_TOKEN, auth[0]);
                    intent.putExtra(VkAccount.ACCOUNT_USER_ID, Long.parseLong(auth[1]));
                    setResult(SherlockActivity.RESULT_OK, intent);
                    Log.d(VkAccount.LOG_TAG, "SEND RESULT!");

                    if(pProviderId.equals(VkLoginFragment.FRAGMENT_ID))
                        loadUrl(RestParams.VK_AUTORITHE_URL+auth[0]);
                    else if(pProviderId.equals(FbLoginFragment.FRAGMENT_ID))
                        loadUrl(RestParams.FB_AUTORITHE_URL+auth[0]);
                    else
                        return;
                }
                else {
                    finish();
                }
            }
            if(url.startsWith(RestParams.VK_AUTORITHE_URL) || url.startsWith(RestParams.FB_AUTORITHE_URL))
            {
                Log.d(VkAccount.LOG_TAG, "GET AUTORITHED PAGE! ");
                finish();
            }
        } catch (Exception e) {
            Log.d(VkAccount.LOG_TAG, "EXCEPTION! " + e.toString());
            e.printStackTrace();
        }
    }

}
