package com.ltst.prizeword.vk;

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
import com.perm.kate.api.Auth;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 16.07.13.
 */
public class VkLoginActivity extends SherlockActivity {

    static public final @Nonnull String PROVEDER_ID = "prov_id";

    private @Nonnull String LOG_TAG = "vkontakte";

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
        Log.d(LOG_TAG, "PROVIDER_ID = "+pProviderId);

        if(pProviderId.equals(VkLoginFragment.FRAGMENT_ID))
            loadUrl(RestParams.VK_LOGIN_URL);
        else
            loadUrl(RestParams.FB_LOGIN_URL);

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
            Log.d(LOG_TAG, "PARSE URL = "+url);
            if(url.startsWith(RestParams.VK_TOKEN_URL) || url.startsWith(RestParams.FB_TOKEN_URL))
            {
                if(!url.contains("error=")){
//                    String[] auth = Auth.parseRedirectUrl(url);
                    String[] auth;
                    if(pProviderId.equals(VkLoginFragment.FRAGMENT_ID))
                        auth = VkParser.parseVkRedirectUrl(url);
                    else
                        auth = VkParser.parseFbRedirectUrl(url);

                    Intent intent=new Intent();
                    intent.putExtra(VkAccount.ACCOUNT_ACCESS_TOKEN, auth[0]);
                    intent.putExtra(VkAccount.ACCOUNT_USER_ID, Long.parseLong(auth[1]));
                    Log.d(LOG_TAG, "SUBSEND RESULT!");
                    setResult(SherlockActivity.RESULT_OK, intent);
                    Log.d(LOG_TAG, "SEND RESULT!");
                    loadUrl(RestParams.VK_AUTORITHE_URL+"="+RestParams.VK_API_ID);
                }
                else {
                    finish();
                }
            }
            if(url.startsWith(RestParams.VK_AUTORITHE_URL) || url.startsWith(RestParams.FB_AUTORITHE_URL))
            {
                Log.d(LOG_TAG, "GET AUTORITHED PAGE! ");
                finish();
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "EXCEPTION! " + e.toString());
            e.printStackTrace();
        }
    }

}
