package org.omich.velo.bcops;

import javax.annotation.Nonnull;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

/**
 * Класс, реализующий вполне абстрактный IBcToaster
 * и отображающий настоящие Toast сообщения.
 */
public class BcToaster implements IBcToaster
{
	private @Nonnull Handler mMainThreadHandler;
	private @Nonnull Context mContext;
	
	/**
	 * Важно, чтобы Handler был создан в главном потоке.
	 * 
	 * @param context
	 * @param mth
	 */
	public BcToaster (@Nonnull Context context, @Nonnull Handler mth)
	{
		mContext = context;
		mMainThreadHandler = mth;
	}
	
	/**
	 * Просьба отобразить сообщение. Может быть и не из главного потока.
	 */
	public void showToast (@Nonnull String msg)
	{
		mMainThreadHandler.post(new BcToasterRunnable(mContext, msg));
	}
	
	//=========================================================================
	private static class BcToasterRunnable implements Runnable
	{
		private @Nonnull String mMsg;
		private @Nonnull Context mContext;

		BcToasterRunnable (@Nonnull Context context, @Nonnull String msg)
		{
			mMsg = msg;
			mContext = context;
		}

		public void run ()
		{
			Toast.makeText(mContext, mMsg, Toast.LENGTH_SHORT).show();
		}
	}
}
