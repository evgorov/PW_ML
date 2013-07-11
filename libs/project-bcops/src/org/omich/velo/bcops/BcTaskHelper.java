package org.omich.velo.bcops;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.handlers.IListenerInt;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Помогает таску с типичными операциями.
 */
public class BcTaskHelper
{
	/**
	 * Чекает хэндлер на null и если что, узнаёт у него информацию.
	 * 
	 * @param ci
	 * @return
	 */
	public static boolean isCancelled (@Nullable ICancelledInfo ci)
	{
		return ci != null && ci.isCancelled();
	}
	
	/**
	 * Выясняет, доступна ли сеть.
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable (@Nonnull Context context)
	{
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}
	
	/**
	 * Чекает обработчик прогресса на null и, если всё норм, передаёт ему прогресс.
	 * 
	 * @param ph
	 * @param progress
	 */
	public static void handleProgress (@Nullable IListenerInt ph, int progress)
	{
		if(ph != null)
		{
			ph.handle(progress);
		}
	}
}
