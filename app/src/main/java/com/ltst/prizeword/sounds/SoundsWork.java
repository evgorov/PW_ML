package com.ltst.prizeword.sounds;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Message;

import com.ltst.prizeword.R;

import org.omich.velo.log.Log;

import java.util.logging.Handler;

public final class SoundsWork
{
    public static boolean ALL_SOUNDS_FLAG = true;
    private static MediaPlayer mMediaPlayerBack;
    private static MediaPlayer mMediaPlayerAll;
    public Context mContext;

    public static void startBackgroundMusic(Context context)
    {
        if (mMediaPlayerBack == null)
        {
            mMediaPlayerBack = MediaPlayer.create(context, R.raw.background_music);
            mMediaPlayerBack.setLooping(true);
            mMediaPlayerBack.start();
        } else
            mMediaPlayerBack.start();
    }

    public static void startAllSounds(Context context)
    {
    }

    public static void stopAllSounds()
    {
    }

    public static void pauseBackgroundMusic()
    {
        mMediaPlayerBack.pause();
    }

    public static void resumeBackgroundMusic()
    {
        mMediaPlayerBack.start();
    }

    public static void stopBackgroundMusic()
    {
        mMediaPlayerBack.stop();
    }

    public static void sidebarMusic(Context context)
    {
        if (ALL_SOUNDS_FLAG)
        {
            if (mMediaPlayerAll == null)
            {
                mMediaPlayerAll = MediaPlayer.create(context, R.raw.sidebar);
                mMediaPlayerAll.start();
            } else
            {
                while (true)
                {
                    if (!mMediaPlayerAll.isPlaying())
                    {
                        releaseMPALL();
                        mMediaPlayerAll = MediaPlayer.create(context, R.raw.sidebar);
                        mMediaPlayerAll.start();
                        break;
                    }
                }
            }
        }
    }

    public static void interfaceBtnMusic(Context context)
    {
        if (ALL_SOUNDS_FLAG)
        {
            releaseMPALL();
            mMediaPlayerAll = mMediaPlayerAll.create(context, R.raw.interface_button);
            mMediaPlayerAll.start();
        }
    }

    public static void questionAnswered(Context context)
    {
        if (ALL_SOUNDS_FLAG)
        {
            releaseMPALL();
            mMediaPlayerAll = mMediaPlayerAll.create(context, R.raw.question_answered);
            mMediaPlayerAll.start();
        }
    }

    public static void puzzleSolved(Context context)
    {
        if (ALL_SOUNDS_FLAG)
        {
            releaseMPALL();
            mMediaPlayerAll = mMediaPlayerAll.create(context, R.raw.puzzle_solved);
            mMediaPlayerAll.start();
        }
    }

    public static void buySet(Context context)
    {
        if (ALL_SOUNDS_FLAG)
        {
            releaseMPALL();
            mMediaPlayerAll = mMediaPlayerAll.create(context, R.raw.buy_set);
            mMediaPlayerAll.start();
        }
    }

    public static void openSet(Context context)
    {
        if (ALL_SOUNDS_FLAG)
        {
            releaseMPALL();
            mMediaPlayerAll = mMediaPlayerAll.create(context, R.raw.open_set);
            mMediaPlayerAll.start();
        }
    }

    public static void closeSet(Context context)
    {
        if (ALL_SOUNDS_FLAG)
        {
            releaseMPALL();
            mMediaPlayerAll = mMediaPlayerAll.create(context, R.raw.close_set);
            mMediaPlayerAll.start();
        }
    }

    public static void keyboardBtn(Context context)
    {
        if (ALL_SOUNDS_FLAG)
        {
            releaseMPALL();
            mMediaPlayerAll = mMediaPlayerAll.create(context, R.raw.type_1);
            mMediaPlayerAll.start();
        }
    }

    private static void releaseMPALL()
    {
        if (mMediaPlayerAll != null)
        {
            try
            {
                mMediaPlayerAll.release();
                mMediaPlayerAll = null;
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

}
