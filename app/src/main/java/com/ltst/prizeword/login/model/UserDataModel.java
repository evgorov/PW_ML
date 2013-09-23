package com.ltst.prizeword.login.model;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.crossword.model.ClearDataBaseTask;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.dowloading.LoadImageTask;
import com.ltst.prizeword.navigation.NavigationActivity;
import com.ltst.prizeword.rest.RestParams;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 26.07.13.
 */
public class UserDataModel implements IUserDataModel
{

    private @Nonnull Context mContext;
    private @Nonnull IBcConnector mBcConnector;
    private @Nullable UserData mUserData;
    private @Nullable byte[] mUserPic;
    private @Nullable ArrayList<UserProvider> mProviders;
    private int mStatusCodeAnswer;
    private @Nonnull String mStatusMessageAnswer;
    private @Nonnull String mProvider;
    private boolean mIsDestroyed;

    public UserDataModel(@Nonnull Context context, @Nonnull IBcConnector bcConnector)
    {
        this.mContext = context;
        this.mBcConnector = bcConnector;

        mClearDataBaseUpdater.setIntent(ClearDataBaseTask.createIntent());
    }

    @Nullable
    public UserData getUserData()
    {
        return mUserData;
    }

    @Nullable
    public byte[] getUserPic()
    {
        return mUserPic;
    }

    @Nullable
    public ArrayList<UserProvider> getProviders()
    {
        return mProviders;
    }

    public int getStatusCodeAnswer()
    {
        return mStatusCodeAnswer;
    }

    @Nonnull
    public String getStatusMessageAnswer()
    {
        return mStatusMessageAnswer;
    }

    @Nonnull
    public String getProvider()
    {
        return mProvider;
    }

    public void setProvider(@Nonnull String mProvider)
    {
        this.mProvider = mProvider;
    }

    @Override
    public void close()
    {
        Log.i("UserDataModel.destroy() begin"); //$NON-NLS-1$
        if (mIsDestroyed)
        {
            Log.w("UserDataModel.destroy() called more than once"); //$NON-NLS-1$
        }

        mResetUserNameUpdater.close();
        mResetUserImageUpdater.close();
        mLoadUserImageFromDb.close();
        mLoadUserImageFromServer.close();
        mLoadProvidersFromDbUpdater.close();
        mMergeAccountsUpdater.close();
        mUserDataUpdater.close();
        mClearDataBaseUpdater.close();

        mIsDestroyed = true;
        Log.i("UserDataModel.destroy() end"); //$NON-NLS-1$
    }

    @Override
    public void loadUserDataFromInternet(@Nonnull IListenerVoid handler)
    {
        if (mIsDestroyed)
            return;
        String sessionKey = SharedPreferencesValues.getSessionKey(mContext);
        Log.i(NavigationActivity.LOG_TAG, "RELOAD USERDATA SessionKey = " + sessionKey);

        mUserDataUpdater.setIntent(LoadUserDataFromInternetTask.createIntent(sessionKey));
        mUserDataUpdater.update(handler);
    }

    @Override
    public void loadUserImageFromServer(@Nonnull final String url, @Nonnull IListenerVoid handler)
    {
        if (mIsDestroyed)
            return;
        mLoadUserImageFromServer.setIntent(LoadImageTask.createIntent(url));
        mLoadUserImageFromServer.update(handler);
    }

    @Override
    public void loadUserImageFromDB(final long user_id, @Nonnull IListenerVoid handler)
    {
        if (mIsDestroyed)
            return;
        mLoadUserImageFromDb.setIntent(LoadUserDataFromDataBase.createIntentLoadingImage(user_id));
        mLoadUserImageFromDb.update(handler);
    }

    @Override
    public void resetUserImage(final byte[] userPic, @Nonnull IListenerVoid handler)
    {
        if (mIsDestroyed)
            return;
        final String sessionKey = SharedPreferencesValues.getSessionKey(mContext);
        mResetUserImageUpdater.setIntent(ResetUserDataOnServerTask.createUserPicIntent(sessionKey, userPic));
        mResetUserImageUpdater.update(handler);
    }

    @Override
    public void resetUserName(final String userName, @Nonnull IListenerVoid handler)
    {
        if (mIsDestroyed)
            return;
        final String sessionKey = SharedPreferencesValues.getSessionKey(mContext);
        mResetUserNameUpdater.setIntent(ResetUserDataOnServerTask.createUserName(sessionKey, userName));
        mResetUserNameUpdater.update(handler);
    }

    @Override
    public void loadProvidersFromDB(final long user_id, @Nonnull IListenerVoid handler)
    {
        if (mIsDestroyed)
            return;
        mLoadProvidersFromDbUpdater.setIntent(LoadUserDataFromDataBase.createIntentLoadingProviders(user_id));
        mLoadProvidersFromDbUpdater.update(handler);
    }

    @Override
    public void mergeAccounts(final @Nonnull String sessionKey1, final @Nonnull String sessionKey2, @Nonnull IListenerVoid handler)
    {
        if (mIsDestroyed)
            return;
        mMergeAccountsUpdater.setIntent(MergeAccountsTask.createIntentMergeAccounts(sessionKey1, sessionKey2));
        mMergeAccountsUpdater.update(handler);
    }

    @Override
    public void clearDataBase(@Nonnull IListenerVoid handler)
    {
        if (mIsDestroyed)
            return;
        mClearDataBaseUpdater.update(handler);
    }

    private Updater mLoadUserImageFromServer = new Updater(LoadImageTask.class)
    {
        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null)
            {
                mUserPic = null;
                return;
            }
            mUserPic = result.getByteArray(LoadUserDataFromDataBase.BF_IMAGE_DATA);
        }
    };

    private Updater mLoadUserImageFromDb = new Updater(LoadUserDataFromDataBase.class)
    {
        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null)
            {
                mUserPic = null;
                return;
            }
            mUserPic = result.getByteArray(LoadUserDataFromDataBase.BF_IMAGE_DATA);
        }
    };

    private Updater mResetUserImageUpdater = new Updater(ResetUserDataOnServerTask.class)
    {
        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null)
            {
                mUserData = null;
                return;
            }
            mUserData = result.getParcelable(ResetUserDataOnServerTask.BF_USER_DATA);
        }
    };

    private Updater mResetUserNameUpdater = new Updater(ResetUserDataOnServerTask.class)
    {
        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null)
            {
                mUserData = null;
                return;
            }
            mUserData = result.getParcelable(ResetUserDataOnServerTask.BF_USER_DATA);
        }
    };

    private Updater mLoadProvidersFromDbUpdater = new Updater(LoadUserDataFromDataBase.class)
    {
        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null)
            {
                mProviders = null;
                return;
            }
            mProviders = result.getParcelableArrayList(LoadUserDataFromDataBase.BF_PROVIDERS);
        }
    };

    private Updater mMergeAccountsUpdater = new Updater(MergeAccountsTask.class)
    {
        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null)
            {
                mStatusCodeAnswer = RestParams.SC_ERROR;
                return;
            }
            mStatusCodeAnswer = result.getInt(MergeAccountsTask.BF_STATUS_CODE);
            mStatusMessageAnswer = result.getString(MergeAccountsTask.BF_STATUS_MESSAGE);
        }
    };

    private Updater mUserDataUpdater = new Updater(LoadUserDataFromInternetTask.class)
    {
        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null)
            {
                mUserData = null;
                return;
            }
            mUserData = result.getParcelable(LoadUserDataFromInternetTask.BF_USER_DATA);
        }
    };

    private Updater mClearDataBaseUpdater = new Updater(ClearDataBaseTask.class)
    {
        @Override
        protected void handleData(@Nullable Bundle result)
        {
            return;
        }
    };


    private abstract class Updater extends ModelUpdater<DbService.DbTaskEnv>
    {
        private @Nullable Intent mIntent;
        private @Nonnull Class<? extends IBcBaseTask<DbService.DbTaskEnv>> mTaskClass;

        public Updater(@Nonnull Class<? extends IBcBaseTask<DbService.DbTaskEnv>> taskClass)
        {
            mTaskClass = taskClass;
        }

        public void setIntent(@Nullable Intent intent)
        {
            mIntent = intent;
        }

        @Nullable
        @Override
        protected Intent createIntent()
        {
            return mIntent;
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return mTaskClass;
        }

        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<DbService.DbTaskEnv>> getServiceClass()
        {
            return DbService.class;
        }

    }

}
