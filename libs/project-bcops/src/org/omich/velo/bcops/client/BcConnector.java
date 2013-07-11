package org.omich.velo.bcops.client;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.ByNameTaskCreator;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.simple.BcService;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.constants.Strings;
import org.omich.velo.handlers.IListener;
import org.omich.velo.log.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import static org.omich.velo.bcops.BcBaseService.*;

/**
 * Реализация IBcConnector для запуска тасков в сервисе BcService.
 */
public class BcConnector implements IBcConnector, Closeable
{
	private @Nullable Context mContext;
	private final @Nonnull Map<String, TaskReceiver> mMap = new HashMap<String, TaskReceiver>();

	public BcConnector (@Nonnull Context context) {mContext = context;}

	/**
	 * Метод destroy() решил оставить, 
	 * чтобы в нём отписывать всех подписчиков,
	 * которые остались в коннекторе.
	 * 
	 * Поскольку не до конца уверен в том, что они не задерживают существование
	 * активити после выхода из приложения.
	 * Потом исследую этот вопрос подробнее.
	 */
	public void close ()
	{
		Log.i("BcConnector.destroy() begin"); //$NON-NLS-1$
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
		Set<Map.Entry<String, TaskReceiver>> set = mMap.entrySet();

		for(Map.Entry<String, TaskReceiver> entry : set)
		{
			lbm.unregisterReceiver(entry.getValue());
			entry.getValue().closed = true;
		}

		mMap.clear();
		mContext = null;
		Log.i("BcConnector.destroy() end"); //$NON-NLS-1$
	}

	//==== IBcConnector =======================================================
	public @Nonnull <T>String startTaskFull (
								@Nonnull Class<? extends BcBaseService<T>> serviceClass, 
								@Nonnull Class<? extends IBcBaseTask<T>> taskClass,
								@Nonnull Intent intent,
								@Nullable IListener<Intent> handler)
	{
		Context context = mContext;
		if(context == null)
		{
			Log.e("Somebody tryes to start task after destroying of bcConnector"); //$NON-NLS-1$
			return Strings.EMPTY;
		}
		
		@Nonnull String opId = NonnullableCasts.getRandomUUIDString();
		subscribe(opId, handler);

		intent.setClass(context, serviceClass);
		intent.putExtra(BF_OP_ID, opId);
		intent.putExtra(BF_OP_TYPE_ID, ByNameTaskCreator.getTaskTypeId(taskClass));
		context.startService(intent);
		
		return opId;
	}

	public @Nonnull <T>String startTask (
			@Nonnull Class<? extends BcBaseService<T>> serviceClass,
			@Nonnull Class<? extends IBcBaseTask<T>> taskClass,
			@Nonnull Intent intent,
			@Nonnull IListener<Bundle> finishHandler)
	{
		return startTaskFull (serviceClass, taskClass, intent, new TypicalTaskHandler(finishHandler));
	}
	
	public @Nonnull String startTypicalTask (@Nonnull Class<? extends IBcTask> taskClass,
										@Nonnull Intent intent,
										@Nonnull IListener<Bundle> finishHandler)
	{
		return startTaskFull (BcService.class, taskClass, intent, new TypicalTaskHandler(finishHandler));
	}
	
	public void cancelTask (@Nonnull String opId)
	{
		Log.i("BcConnector.cancelTask(" + opId + ")");  //$NON-NLS-1$//$NON-NLS-2$
		Context context = mContext;
		if(context == null)
		{
			Log.e("Somebody tryes to cancel task after destroying of bcConnector"); //$NON-NLS-1$
			return;
		}
		Intent intent = new Intent(BROADCAST_PREFIX);
		intent.putExtra(BF_EVENT, EVT_CANCEL);
		intent.putExtra(BF_OP_ID, opId);
		LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
	}
	
	public void unsubscribeTask (@Nonnull String opId)
	{
		Context context = mContext;
		if(context == null)
		{
			Log.e("Somebody tryes to unsubscribe task after destroying of bcConnector"); //$NON-NLS-1$
			return;
		}

		//Отписаться от задачи, но не отменять её выполнение.
		//Мы устанавливаем в TaskReceiver поле closed=true,
		//за счёт чего, даже если Receiver успеет получить ответ,
		//то он не будет передавать его в обработчики. 
		//Поэтому после вызова метода unsubscribeTask уже не нужно ожидать ответа в обработчиках.
		 
		TaskReceiver tr = mMap.get(opId);
		if(tr != null)
		{
			LocalBroadcastManager.getInstance(context).unregisterReceiver(tr);
			tr.closed = true;
		}
		mMap.remove(opId);
	}
	
	//========================================================================
	private void subscribe (@Nonnull String opId, @Nullable IListener<Intent> handler)
	{
		Context context = mContext;
		if(handler != null && context != null)
		{
			TaskReceiver tr = new TaskReceiver(mMap, handler, opId);
			LocalBroadcastManager.getInstance(context)
				.registerReceiver(tr, new IntentFilter(BROADCAST_PREFIX + opId));
			mMap.put(opId, tr);
		}
	}
	
	private static class TaskReceiver extends BroadcastReceiver
	{
		/**
		 * Если closed == true, то полученный ответ не будет передаваться в обработчики.
		 */
		public boolean closed;

		private final @Nonnull Map<String, TaskReceiver> mMapOfBcConnector;
		private final @Nonnull IListener<Intent> mHandler;
		private final @Nonnull String mOpId;
		
		public TaskReceiver (@Nonnull Map<String, TaskReceiver> map, 
								@Nonnull IListener<Intent> handler,
								@Nonnull String opId)
		{
			mMapOfBcConnector = map;
			mHandler = handler;
			mOpId = opId;
		}

		@Override
		public void onReceive (Context context, Intent intent)
		{//main thread
			if(closed)
				return;
			
			String event = intent.getExtras().getString(BF_EVENT);
			if(EVT_FINISH.equals(event))
			{
				if(context != null)
				{
					LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
				}
				else
				{
					Log.i("Context was null in BroadcastReceiver.onReceive()"); //$NON-NLS-1$
				}
				mMapOfBcConnector.remove(mOpId);
				closed = true;
			}
			
			mHandler.handle(intent);
		}
	}
	
	public static class TypicalTaskHandler implements IListener<Intent>
	{
		private @Nonnull IListener<Bundle> mFinishHandler;
		
		public TypicalTaskHandler (@Nonnull IListener<Bundle> finishHandler)
		{
			mFinishHandler = finishHandler;
		}

		public void handle (@Nullable Intent intent)
		{
			if(intent != null)
			{
				BcEventHelper.parseEvent(intent, null, null, mFinishHandler, null, null);
			}
		}
	}
}
