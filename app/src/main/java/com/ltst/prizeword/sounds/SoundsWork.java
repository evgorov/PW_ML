package com.ltst.prizeword.sounds;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import org.omich.velo.log.Log;

public final class SoundsWork
{
    private static SoundPool mSoundPool;
    public static final int MAX_STREAMS = 10;
    public static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;
    public static final int SRC_QUALITY = 0;
    public static boolean mLoaded = false;
    public static float mVolume = 0;
    public static int mSoundId = 0;
    public Context mContext;

    public SoundsWork(Context context)
    {
        //Create SoundPool
        mSoundPool = new SoundPool(MAX_STREAMS, STREAM_TYPE, SRC_QUALITY);
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener()
        {
            @Override public void onLoadComplete(SoundPool soundPool, int sampleId, int status)
            {
                mLoaded = true;
            }
        });

        getUserSetting(context);
        mContext = context;
    }

    public void LoadMusic( int resource)
    {
        mSoundId = mSoundPool.load(mContext,resource, 1);
    }

    public  boolean isLoaded()
    {
        return mLoaded;
    }

    public  int getmSoundId()
    {
        return mSoundId;
    }

    public  float getmVolume()
    {
        return mVolume;
    }

    private void getUserSetting(Context context)
    {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        float actualVolume = (float) audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolume = actualVolume / maxVolume;
    }

    public  void playMusic()
    {
        if (isLoaded())
        {
            mSoundPool.play(getmSoundId(), getmVolume(), getmVolume(), 1, 0, 1f);
            Log.e("Test", "Played sound");
        }
    }
}
