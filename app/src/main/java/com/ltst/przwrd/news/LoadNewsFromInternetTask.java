package com.ltst.przwrd.news;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ltst.przwrd.R;
import com.ltst.przwrd.db.DbService;
import com.ltst.przwrd.rest.IRestClient;
import com.ltst.przwrd.rest.RestClient;
import com.ltst.przwrd.rest.RestNews;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoadNewsFromInternetTask implements DbService.IDbTask
{
    public static final @Nonnull String BF_SESSION_KEY = "LoadCoefficientsFromInternetTask.sessionKey";
    public static final @Nonnull String BF_NEWS = "LoadCoefficientsFromInternetTask.news";

    public static final @Nonnull Intent createIntent(@Nonnull String sessionKey)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        return intent;
    }

    @Override public Bundle execute(DbService.DbTaskEnv env)
    {
        if(!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {
            Bundle extras = env.extras;
            if (extras == null)
            {
                return null;
            }
            @Nullable String sessionKey = extras.getString(BF_SESSION_KEY);
            if (sessionKey != null)
            {
                @Nullable News existingNews = env.dbw.getNews();
                @Nullable String hash = null;
                if(existingNews != null)
                {
                    hash = existingNews.etagHash;
                }
                @Nullable RestNews restNews = loadNews(env.context, sessionKey, hash);
                if (restNews != null)
                {
                    @Nonnull News news = parseNews(restNews);
                    env.dbw.updateNews(news);
                    return packToBundle(news);
                }
            }
        }
        return getFromDatabase(env);
    }

    private @Nullable RestNews loadNews(@Nonnull Context context, @Nonnull String sessionKey,
                                        @Nullable String etag)
    {
        try
        {
            IRestClient client = RestClient.create(context);
            return client.getNews(sessionKey, etag);
        }
        catch (Throwable e)
        {
            Log.i("Can't load news from internet");
        }
        return null;
    }

    private @Nonnull News parseNews(@Nonnull RestNews rest)
    {
        return new News(rest.getMessage1(), rest.getMessage2(), rest.getMessage3(), rest.getEtagHash());
    }

    private static @Nonnull Bundle packToBundle(@Nonnull News news)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable(BF_NEWS, news);
        return bundle;
    }

    private static @Nullable Bundle getFromDatabase(DbService.DbTaskEnv env)
    {
        @Nullable News news = env.dbw.getNews();
        if (news != null)
        {
            return packToBundle(news);
        }
        return null;
    }


}
