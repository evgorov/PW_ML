package com.ltst.przwrd.push;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.ltst.przwrd.app.ModelUpdater;
import com.ltst.przwrd.navigation.NavigationActivity;
import com.ltst.przwrd.tools.IActivityLifeCycle;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.bcops.simple.BcService;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.log.Log;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GcmHelper implements IActivityLifeCycle
{
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private static final String SENDER_ID = "921729497728";

    private GoogleCloudMessaging gcm;
    private AtomicInteger msgId = new AtomicInteger();
    private @Nullable String mRegistrationId;
    private @Nullable String mSessionKey;
    private @Nullable RegistrationIdSender mSender;

    private Activity mActivity;
    private @Nonnull IBcConnector mBcConnector;

    public GcmHelper(Activity activity, @Nonnull IBcConnector bcConnector)
    {
        mActivity = activity;
        mBcConnector = bcConnector;
    }


    private void performRegistration()
    {
        if (checkPlayServices())
        {
            gcm = GoogleCloudMessaging.getInstance(mActivity);
            mRegistrationId = getRegistrationId(mActivity);

            if (mRegistrationId.isEmpty())
            {
                registerInBackground();
            }
        } else
        {
            Log.i("No valid Google Play Services APK found.");
        }
    }

    public void onAuthorized(@Nullable String sessionKey)
    {
        mSessionKey = sessionKey;
        performRegistration();
    }

    public void unregister()
    {
        try
        {
            if (gcm != null)
            {
                gcm.unregister();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        final SharedPreferences prefs = getGcmPreferences(mActivity);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PROPERTY_REG_ID);
        editor.remove(PROPERTY_APP_VERSION);
        editor.commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        performRegistration();
    }

    @Override
    public void onResume()
    {
        checkPlayServices();
    }

    @Override
    public void onPause()
    {
        if (mSender != null)
        {
            mSender.close();
        }
    }

    @Override
    public void onStop()
    {

    }

    @Override
    public void onDestroy()
    {

    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mActivity);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, mActivity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else
            {
                Log.i("This device is not supported.");
                mActivity.finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId)
    {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i("Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId(Context context)
    {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty())
        {
            Log.i("Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion)
        {
            Log.i("App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground()
    {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected String doInBackground(Void... params)
            {
                String msg = "";
                try
                {
                    if (gcm == null)
                    {
                        gcm = GoogleCloudMessaging.getInstance(mActivity);
                    }
                    mRegistrationId = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + mRegistrationId;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(mActivity, mRegistrationId);
                } catch (IOException ex)
                {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg)
            {
//                Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
            }
        }.execute(null, null, null);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context)
    {
        try
        {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e)
        {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context)
    {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return mActivity.getSharedPreferences(NavigationActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend()
    {
        // Your implementation here.
        mSender = new RegistrationIdSender();
        if (mSessionKey != null)
        {
            if (mRegistrationId != null)
            {
                mSender.setIntent(SendRegistrationIdTask.createIntent(mSessionKey, mRegistrationId));
                mSender.update(null);
            }
        }
        else
        {
            if (mRegistrationId != null)
            {
                mSender.setIntent(SendRegistrationIdTask.createIntent(mRegistrationId));
                mSender.update(null);
            }
        }
    }

    private class RegistrationIdSender extends ModelUpdater<IBcTask.BcTaskEnv>
    {
        private @Nonnull Intent mIntent;

        public void setIntent(@Nonnull Intent intent)
        {
            mIntent = intent;
        }

        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Nullable
        @Override
        protected Intent createIntent()
        {
            return mIntent;
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<IBcTask.BcTaskEnv>> getTaskClass()
        {
            return SendRegistrationIdTask.class;
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

        }
    }
}
