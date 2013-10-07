package com.ltst.przwrd.login.view;

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
import com.ltst.przwrd.app.ModelUpdater;
import com.ltst.przwrd.app.SharedPreferencesHelper;
import com.ltst.przwrd.app.SharedPreferencesValues;
import com.ltst.przwrd.login.model.LoadSessionKeyTask;
import com.ltst.przwrd.rest.RestParams;
import com.ltst.przwrd.login.model.SocialParser;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.BcConnector;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.bcops.simple.BcService;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.constants.Strings;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 16.07.13.
 */
public class SocialLoginActivity extends SherlockActivity
{
    static public final @Nonnull String BF_PROVEDER_ID = "prov_id";
    static public final @Nonnull String BF_SESSION_KEY = "session_key";

    private @Nonnull IBcConnector mBcConnector;

    private @Nonnull WebView mWebView;
    private @Nonnull ProgressBar mProgressBar;

    private @Nonnull String pProviderId;
    private @Nonnull String mSessionKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        mBcConnector = new BcConnector(this);
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
        pProviderId = extras.getString(BF_PROVEDER_ID);

        if(pProviderId.equals(RestParams.VK_PROVIDER)) {
            loadUrl(RestParams.URL_VK_LOGIN);
            setTitle(R.string.vk_login_fragment_title);
        }
        else if(pProviderId.equals(RestParams.FB_PROVIDER)) {
            loadUrl(RestParams.URL_FB_LOGIN);
            setTitle(R.string.fb_login_fragment_title);
        }

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

    private void loadUrl(String url){
        mWebView.loadUrl(url);
    }

    private void parseUrl(@Nonnull String url) {
        try {
            if(url==null)
                return;
//            Log.d(NavigationActivity.LOG_TAG, "SocialLogin PARSE URL = "+url);
            if(url.startsWith(RestParams.URL_VK_TOKEN) || url.startsWith(RestParams.URL_FB_TOKEN))
            {
                if(!url.contains("error="))
                {
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
                    SharedPreferencesValues.setFacebookToken(this,access_token);

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
                            Intent intent = new Intent();
                            intent.putExtra(BF_SESSION_KEY, mSessionKey);
                            setResult(SherlockActivity.RESULT_OK, intent);
                            finish();
                        }
                    });
                }
                else
                {
                    Intent intent = new Intent();
                    setResult(SherlockActivity.RESULT_OK, intent);
                    finish();
                }
            }
        } catch (Exception e) {
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
            mSessionKey = sessionKey;
        }
    }

}
