package com.ltst.prizeword.sounds;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Message;

import com.ltst.prizeword.R;

import org.omich.velo.log.Log;

import java.util.logging.Handler;

import javax.annotation.Nonnull;

public final class SoundsWork
{
    public static boolean ALL_SOUNDS_FLAG = true;
    private static int count = 1;
    private static MediaPlayer mMediaPlayerBack;
    private static MediaPlayer mMediaPlayerAll;
    public Context mContext;

    public static void startBackgroundMusic(@Nonnull Context context)
    {
        if (mMediaPlayerBack == null)
        {
            mMediaPlayerBack = MediaPlayer.create(context, R.raw.background_music);
            mMediaPlayerBack.setLooping(true);
            mMediaPlayerBack.start();
        } else
            mMediaPlayerBack.start();
    }

    public static void pauseBackgroundMusic()
    {
        if(mMediaPlayerBack != null)
            mMediaPlayerBack.pause();
    }

    public static void sidebarMusic(@Nonnull Context context)
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

    public static void interfaceBtnMusic(@Nonnull Context context)
    {
        if (ALL_SOUNDS_FLAG)
        {
            releaseMPALL();
            mMediaPlayerAll = mMediaPlayerAll.create(context, R.raw.interface_button);
            mMediaPlayerAll.start();
        }
    }

    public static void questionAnswered(@Nonnull Context context)
    {
        if (ALL_SOUNDS_FLAG)
        {
            releaseMPALL();
            mMediaPlayerAll = mMediaPlayerAll.create(context, R.raw.question_answered);
            mMediaPlayerAll.start();
        }
    }

    public static void puzzleSolved(@Nonnull Context context)
    {
        if (ALL_SOUNDS_FLAG)
        {
            releaseMPALL();
            mMediaPlayerAll = mMediaPlayerAll.create(context, R.raw.puzzle_solved);
            mMediaPlayerAll.start();
        }
    }

    public static void buySet(@Nonnull Context context)
    {
        if (ALL_SOUNDS_FLAG)
        {
            releaseMPALL();
            mMediaPlayerAll = mMediaPlayerAll.create(context, R.raw.buy_set);
            mMediaPlayerAll.start();
        }
    }

    public static void openSet(@Nonnull Context context)
    {
        if (ALL_SOUNDS_FLAG)
        {
            releaseMPALL();
            mMediaPlayerAll = mMediaPlayerAll.create(context, R.raw.open_set);
            mMediaPlayerAll.start();
        }
    }

    public static void closeSet(@Nonnull Context context)
    {
        if (ALL_SOUNDS_FLAG)
        {
            releaseMPALL();
            mMediaPlayerAll = mMediaPlayerAll.create(context, R.raw.close_set);
            mMediaPlayerAll.start();
        }
    }

    public static void keyboardBtn(@Nonnull Context context)
    {
        if (ALL_SOUNDS_FLAG)
        {
            releaseMPALL();
            if (count % 3 == 0)
                mMediaPlayerAll = mMediaPlayerAll.create(context, R.raw.type_3);
            else if (count % 2 == 0)
                mMediaPlayerAll = mMediaPlayerAll.create(context, R.raw.type_2);
            else
                mMediaPlayerAll = mMediaPlayerAll.create(context, R.raw.type_1);
            mMediaPlayerAll.start();
        }
        count++;
    }

    public static void scoreSetSound(@Nonnull Context context)
    {
        if (ALL_SOUNDS_FLAG)
        {
            releaseMPALL();
            mMediaPlayerAll = mMediaPlayerAll.create(context, R.raw.secondary_ding);
            mMediaPlayerAll.start();
        }
    }

    public static void scoreAllCountSound(@Nonnull Context context)
    {
        if (ALL_SOUNDS_FLAG)
        {
            releaseMPALL();
            mMediaPlayerAll = mMediaPlayerAll.create(context, R.raw.counting);
            mMediaPlayerAll.start();
        }
    }


    public static void releaseMPALL()
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

    public static void releaseMPBack()
    {
        if (mMediaPlayerBack != null)
        {
            try
            {
                mMediaPlayerBack.release();
                mMediaPlayerBack = null;
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

}
