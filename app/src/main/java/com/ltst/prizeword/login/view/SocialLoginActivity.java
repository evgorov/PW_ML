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
import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.app.SharedPreferencesHelper;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.login.model.LoadUserDataFromInternetTask;
import com.ltst.prizeword.rest.RestParams;
import com.ltst.prizeword.login.model.SocialParser;
import com.ltst.prizeword.login.vk.VkAccount;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.BcConnector;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 16.07.13.
 */
public class SocialLoginActivity extends SherlockActivity
{

    static public final @Nonnull String PROVEDER_ID = "prov_id";

    private @Nonnull IBcConnector mBcConnector;

    private @Nonnull WebView mWebView;

    private String pProviderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        mBcConnector = new BcConnector(this);
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
            loadUrl(RestParams.URL_VK_LOGIN);
            setTitle(R.string.vk_login_fragment_title);
        }
        else if(pProviderId.equals(FbLoginFragment.FRAGMENT_ID)) {
            loadUrl(RestParams.URL_FB_LOGIN);
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
            if(url.startsWith(RestParams.URL_VK_TOKEN) || url.startsWith(RestParams.URL_FB_TOKEN))
            {
                if(!url.contains("error=")){
//                    String[] auth = Auth.parseRedirectUrl(url);
                    String[] auth;
                    boolean isVK = false;
                    boolean isFb = false;
                    if(pProviderId.equals(VkLoginFragment.FRAGMENT_ID))
                    {
                        auth = SocialParser.parseVkRedirectUrl(url);
                        isVK = true;
                    }
                    else if(pProviderId.equals(FbLoginFragment.FRAGMENT_ID))
                    {
                        auth = SocialParser.parseFbRedirectUrl(url);
                        isFb = true;
                    }
                    else
                        return;

                    final @Nullable String access_token = auth[0];

                    final boolean VK = isVK;
                    final boolean FB = isFb;
                    SessionKeyLoader loader = new SessionKeyLoader()
                    {
                        @Nonnull
                        @Override
                        protected Intent createIntent()
                        {
                            String provider = VK ? RestParams.VK_PROVIDER : FB ?  RestParams.FB_PROVIDER : null;
                            return LoadUserDataFromInternetTask.createIntent(null, provider, access_token);
                        }
                    };

                    loader.update(new IListenerVoid()
                    {
                        @Override
                        public void handle()
                        {
                            Log.i(VkAccount.LOG_TAG, "handling");
                            finish();
                        }
                    });

//                    Intent intent=new Intent();
//                    intent.putExtra(VkAccount.ACCOUNT_ACCESS_TOKEN, auth[0]);
//                    intent.putExtra(VkAccount.ACCOUNT_USER_ID, Long.parseLong(auth[1]));
//                    setResult(SherlockActivity.RESULT_OK, intent);
//                    Log.d(VkAccount.LOG_TAG, "SEND RESULT!");
//
//                    if(pProviderId.equals(VkLoginFragment.FRAGMENT_ID))
//                        loadUrl(RestParams.URL_VK_AUTORITHE+auth[0]);
//                    else if(pProviderId.equals(FbLoginFragment.FRAGMENT_ID))
//                        loadUrl(RestParams.URL_FB_AUTORITHE+auth[0]);
//                    else
//                        return;
                }
                else {
                    finish();
                }
            }
            if(url.startsWith(RestParams.URL_VK_AUTORITHE) || url.startsWith(RestParams.URL_FB_AUTORITHE))
            {
                Log.d(VkAccount.LOG_TAG, "GET AUTORITHED PAGE! ");
                finish();
            }
        } catch (Exception e) {
            Log.d(VkAccount.LOG_TAG, "EXCEPTION! " + e.toString());
            e.printStackTrace();
        }
    }

    private abstract class SessionKeyLoader extends ModelUpdater<DbService.DbTaskEnv>
    {
        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return LoadUserDataFromInternetTask.class;
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<DbService.DbTaskEnv>> getServiceClass()
        {
            return DbService.class;
        }

        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if(result == null)
                return;

            @Nullable String sessionKey = result.getString(LoadUserDataFromInternetTask.BF_SESSION_KEY);
            if(sessionKey != null)
            {
                SharedPreferencesHelper spref = SharedPreferencesHelper.getInstance(SocialLoginActivity.this);
                spref.putString(SharedPreferencesValues.SP_SESSION_KEY, sessionKey);
                spref.commit();
                Log.i("SESSION KEY", sessionKey);
            }
        }
    }

}
