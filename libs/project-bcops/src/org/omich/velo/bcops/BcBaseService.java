package org.omich.velo.bcops;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.bcops.log.ErrorParcelable;
import org.omich.velo.handlers.IListenerInt;
import org.omich.velo.log.Log;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

abstract public class BcBaseService<TaskEnv> extends IntentService
{
	public static final String BROADCAST_PREFIX = "BcService."; //$NON-NLS-1$
	
	public static final String EVT_PROGRESS = "progress"; //$NON-NLS-1$
	public static final String EVT_CANCEL   = "cancel"; //$NON-NLS-1$
	public static final String EVT_FINISH   = "finish"; //$NON-NLS-1$
	public static final String EVT_START    = "start"; //$NON-NLS-1$
	
	public static final String BF_ERROR            = "BcService.error"; //$NON-NLS-1$
	public static final String BF_EVENT            = "BcService.event"; //$NON-NLS-1$
	public static final String BF_OP_ID            = "BcService.opId"; //$NON-NLS-1$
	public static final String BF_OP_TYPE_ID       = "BcService.opTypeId"; //$NON-NLS-1$
	public static final String BF_PROGRESS_DATA    = "BcService.progressData"; //$NON-NLS-1$
	public static final String BF_RESULT           = "BcService.result"; //$NON-NLS-1$
	public static final String BF_SUCCESS          = "BcService.success"; //$NON-NLS-1$

	//========================================================================
	private final @Nonnull PH mPh;
	private BR mBr;
	protected @Nonnull IBcToaster mBcToaster = IBcToaster.EMPTY_TOASTER;
	
	private String mCurrentOpId;
	private boolean mIsCurrentCancelled;
	private final @Nonnull Set<String> mCancelledOps = new HashSet<String>();
	private final @Nonnull ITaskCreator mTaskCreator;

	protected BcBaseService (@Nonnull String serviceName,
			@Nonnull ITaskCreator taskCreator)
	{
		super(serviceName);
		mPh = new PH();
		mTaskCreator = taskCreator;
	}
	
	//==== protected interface ===============================================
	abstract protected @Nonnull TaskEnv createTaskEnv(
			@Nullable Bundle extras, 
			@Nonnull Context context,
			@Nonnull  IBcToaster bcToaster,
			@Nullable IListenerInt ph, 
			@Nullable ICancelledInfo ci) throws BcEnvException;

	//==== events ============================================================
	@Override
	public void onCreate ()
	{
		super.onCreate();
		registerLocalReceiver();
		mBcToaster = new BcToaster(this, new Handler());
	}
	
	@Override
	public void onDestroy ()
	{
		unregisterLocalReceiver();
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent (Intent intent)
	{//bg thread
		IBcBaseTask<TaskEnv> task = null;
		synchronized (this)
		{
			@SuppressWarnings("null")
			@Nonnull String opId = intent.getExtras().getString(BF_OP_ID);
			mCurrentOpId = opId;
			mIsCurrentCancelled = isCancelled(opId);
			if(!mIsCurrentCancelled)
			{
				task = getTaskByIntent(intent);
			}
		}
		sendStartBroadcast();

		Bundle result = null;
		if(task != null)
		{
			try
			{
				result = task.execute(createTaskEnv(
						intent.getExtras(), 
						this, 
						mBcToaster, 
						mPh, 
						mPh));
			}
			catch (Throwable e)
			{
				Log.w("BcService operation error, which wasn't catched in IBcTask", e); //$NON-NLS-1$
				result = new Bundle();
				result.putParcelable(BF_ERROR, new ErrorParcelable(e));
			}
		}
		
		sendResultBroadcast(result);
		
		synchronized (this)
		{
			if(mIsCurrentCancelled)
			{
				mIsCurrentCancelled = false;
				@SuppressWarnings("null")
				@Nonnull String opId = mCurrentOpId;
				setOpUncancelled(opId);
			}
			mCurrentOpId = null;
		}
	}
	
	//=========================================================================
	private @Nullable IBcBaseTask<TaskEnv> getTaskByIntent (@Nonnull Intent intent)
	{
		return mTaskCreator.getTaskByIntent(intent);
	}

	private void sendProgressBroadcast (int progress)
	{
		Intent intent = createBroadcastIntent();
		intent.putExtra(BF_EVENT, EVT_PROGRESS);
		intent.putExtra(BF_PROGRESS_DATA, progress);
		sendLocalBroadcast(intent);
	}
	
	private void sendStartBroadcast ()
	{
		Intent intent = createBroadcastIntent();
		intent.putExtra(BF_EVENT, EVT_START);
		sendLocalBroadcast(intent);
	}

	private void sendResultBroadcast (@Nullable Parcelable result)
	{
		Intent intent = createBroadcastIntent();
		intent.putExtra(BF_EVENT, EVT_FINISH);
		intent.putExtra(BF_RESULT, result);
		sendLocalBroadcast(intent);
	}
	
	private @Nonnull Intent createBroadcastIntent ()
	{
		if(mCurrentOpId == null)
			return new Intent(BROADCAST_PREFIX);

		return new Intent (BROADCAST_PREFIX + mCurrentOpId);
	}
	
	private void sendLocalBroadcast (@Nonnull Intent intent)
	{
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	
	private void registerLocalReceiver ()
	{
		mBr = new BR();
		LocalBroadcastManager.getInstance(this).registerReceiver(mBr, 
				new IntentFilter(BROADCAST_PREFIX));
	}
	
	private void unregisterLocalReceiver ()
	{
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mBr);
		mBr = null;
	}
	
	private boolean isCancelled (@Nonnull String opId)
	{
		for(String cOpId : mCancelledOps)
		{
			if(cOpId.equals(opId))
				return true;
		}
		return false;
	}
	
	private void setOpCancelled (@Nonnull String opId)
	{
		if(isCancelled(opId))
			return;
		
		mCancelledOps.add(opId);
	}
	
	private void setOpUncancelled (@Nonnull String opId)
	{
		mCancelledOps.remove(opId);
	}
	
	private class PH implements IListenerInt, 
			ICancelledInfo
	{
		//bg thread
		public void handle (int progress){sendProgressBroadcast(progress);}
		public boolean isCancelled () {return mIsCurrentCancelled;}
	}
	
	private class BR extends BroadcastReceiver
	{
		@Override
		public void onReceive (Context context, Intent intent)
		{//main thread
			synchronized (BcBaseService.this)
			{
				if(context != getApplicationContext() && context != BcBaseService.this)
				{
					Log.i("BcService Reciever gets Broadcast of alien context"); //$NON-NLS-1$
					return;
				}
				
				@Nullable String event = intent.getExtras().getString(BF_EVENT);
				boolean cancel = EVT_CANCEL.equals(event);
				
				if(!cancel)
					return;
				
				@Nullable String opId = intent.getExtras().getString(BF_OP_ID);
				if(opId != null)
				{
					setOpCancelled(opId);

					if(opId.equals(mCurrentOpId))
					{
						mIsCurrentCancelled = true;
					}
				}
			}
		}
	}
}
