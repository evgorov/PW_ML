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
import com.ltst.prizeword.login.model.LoadSessionKeyTask;
import com.ltst.prizeword.rest.RestParams;
import com.ltst.prizeword.login.model.SocialParser;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.BcConnector;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.bcops.simple.BcService;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 16.07.13.
 */
public class SocialLoginActivity extends SherlockActivity
{
    final private @Nonnull String LOG_TAG = "vkontakte";

    static public final @Nonnull String PROVEDER_ID = "prov_id";

    private @Nonnull IBcConnector mBcConnector;

    private @Nonnull WebView mWebView;

    private @Nonnull String pProviderId;

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
        Log.d(LOG_TAG, "PROVIDER_ID = "+pProviderId);

        if(pProviderId.equals(RestParams.VK_PROVIDER)) {
            loadUrl(RestParams.URL_VK_LOGIN);
            setTitle(R.string.vk_login_fragment_title);
        }
        else if(pProviderId.equals(RestParams.FB_PROVIDER)) {
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
            Log.d(LOG_TAG, "PARSE URL = "+url);
            if(url.startsWith(RestParams.URL_VK_TOKEN) || url.startsWith(RestParams.URL_FB_TOKEN))
            {
                if(!url.contains("error=")){
//                    String[] auth = Auth.parseRedirectUrl(url);
                    String[] auth;
                    boolean isVK = false;
                    boolean isFb = false;
                    if(pProviderId.equals(RestParams.VK_PROVIDER))
                    {
                        auth = SocialParser.parseVkRedirectUrl(url);
                        isVK = true;
                    }
                    else if(pProviderId.equals(RestParams.FB_PROVIDER))
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
                            return LoadSessionKeyTask.createProviderIntent(provider, access_token);
                        }
                    };

                    loader.update(new IListenerVoid()
                    {
                        @Override
                        public void handle()
                        {
                            Log.i(LOG_TAG, "handling");
                            Intent intent=new Intent();
//                            intent.putExtra(VkAccount.ACCOUNT_ACCESS_TOKEN, auth[0]);
//                            intent.putExtra(VkAccount.ACCOUNT_USER_ID, Long.parseLong(auth[1]));
                            setResult(SherlockActivity.RESULT_OK, intent);

                            finish();
                        }
                    });
                }
                else {
                    finish();
                }
            }
//            if(url.startsWith(RestParams.URL_VK_AUTORITHE) || url.startsWith(RestParams.URL_FB_AUTORITHE))
//            {
//                Log.d(LOG_TAG, "GET AUTORITHED PAGE! ");
//                finish();
//            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "EXCEPTION! " + e.toString());
            e.printStackTrace();
        }
    }

    private abstract class SessionKeyLoader extends ModelUpdater<IBcTask.BcTaskEnv>
    {
        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<IBcTask.BcTaskEnv>> getTaskClass()
        {
            return LoadSessionKeyTask.class;
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<IBcTask.BcTaskEnv>> getServiceClass()
        {
            return BcService.class;
        }

        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if(result == null)
                return;

            @Nullable String sessionKey = result.getString(LoadSessionKeyTask.BF_SESSION_KEY);
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
