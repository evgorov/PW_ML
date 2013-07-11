package org.omich.velo.lists;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.events.PairListeners.IListenerBooleanObject;
import org.omich.velo.events.PairListeners.INistenerBooleanObject;

public class LoadingQueue<Param, Result> implements ILoadingQueue<Param, Result>
{
	private final @Nonnull IBgDownloader<Param, Result> mBgDownloader;

	private @Nullable QueuePair mCurrentLoadingPair;
	private boolean mIsLoading = false;//Если происходит процесс загрузки прямо сейчас.
	private boolean mIsClosed = false;//Если очередь была закрыта.
	
	private @Nonnull List<QueuePair> mLoadingQueue = new ArrayList<QueuePair>();
	private @Nonnull Set<Param> mLoadingQueueIndexes = new HashSet<Param>();
	
	public LoadingQueue (@Nonnull IBgDownloader<Param, Result> bgDownloader)
	{
		mBgDownloader = bgDownloader;
	}
	
	/**
	 * Временно запретить возможность загрузки, до вызова метода {@link #resumeResource()};
	 */
	public void pauseResource()
	{
		stopLoad();
		mIsClosed = true;
	}

	/**
	 * Снова разрешить возможность загрузки.
	 */
	public void resumeResource ()
	{
		mIsClosed = false;
	}
	
	//==== ILoadingQueue implementation ========================================
	public void pushOrder(@Nonnull Param param,
			@Nonnull INistenerBooleanObject<ResultPair<Param, Result>> handler)
	{
		//Если элемент уже был добавлен или грузится, то не добавляем его второй раз.
		QueuePair curPair = mCurrentLoadingPair;

		if(curPair != null && curPair.param.equals(param))
		{
			curPair.addHandler(handler);
		}
		else if(mLoadingQueueIndexes.contains(param))
		{
			for(QueuePair pair : mLoadingQueue)
			{
				if(pair.param.equals(param))
				{
					pair.addHandler(handler);
				}
			}
		}
		else
		{
			mLoadingQueue.add(new QueuePair(param, handler));
			mLoadingQueueIndexes.add(param);
		}

		startLoad();
	}

	public boolean popOrder(@Nonnull Param param)
	{
		if(mLoadingQueueIndexes.contains(param))
		{
			int i = 0;
			for(QueuePair pair : mLoadingQueue)
			{
				if(pair.param.equals(param))
				{
					mLoadingQueue.remove(i);
					mLoadingQueueIndexes.remove(param);
					return true;
				}
				++i;
			}
		}
		return false;
	}

	public void resetQueue()
	{
		mLoadingQueue.clear();
		mLoadingQueueIndexes.clear();
		stopLoad();
	}
	
	//=========================================================================
	/**
	 * Если загрузка не шла, то она начнётся.
	 * Если был заказ на остановку загрузки, но загрузка не была прервана,
	 * то заказ отменится и загрузка будет продолжаться.
	 */
	private void startLoad ()
	{
		if(!mIsLoading)
		{
			load();
		}
	}
	
	/**
	 * Остановить текущую загрузку.
	 */
	private void stopLoad ()
	{
		//Просим bgDownloader отменить загрузку slowData.
		//После этого мы уже не ждём, что в onLoaded что-то придёт.
		if(mIsLoading)
		{
			mBgDownloader.cancelLoadingSlowData();
			mCurrentLoadingPair = null;
			mIsLoading = false;
		}
	}
	
	private void load ()
	{
		if(mIsLoading || mLoadingQueue.isEmpty() || mIsClosed)
			return;
		
		mIsLoading = true;
		
		final QueuePair pair = mLoadingQueue.remove(0);
		mLoadingQueueIndexes.remove(pair.param);
		mCurrentLoadingPair = pair;

		mBgDownloader.loadSlowData(pair.param,
			new IListenerBooleanObject<Result>()
			{
				public void handle(boolean isSuccess, @Nullable Result result)
				{
					onLoaded(isSuccess, result);
				}
			} 
		);
	}
	
	private void onLoaded (boolean isSuccess, @Nullable Result result)
	{
		//Очередной элемент загружен, начинаем грузить очередной элемент: load();

		QueuePair pair = mCurrentLoadingPair;
		assert mIsLoading == true;
		assert pair != null;

		mIsLoading = false;
		mCurrentLoadingPair = null;

		for(INistenerBooleanObject<ResultPair<Param, Result>> handler : pair.handlers)
		{
			if(handler != null)
			{
				handler.handle(isSuccess, new ResultPair<Param, Result>(pair.param, result));
			}
		}

		load();
	}
	
	private class QueuePair
	{
		final @Nonnull Param param;
		final @Nonnull List<INistenerBooleanObject<ResultPair<Param, Result>>> handlers = 
				new ArrayList<INistenerBooleanObject<ResultPair<Param,Result>>>();
		
		public QueuePair(@Nonnull Param param, @Nonnull INistenerBooleanObject<ResultPair<Param, Result>> handler)
		{
			this.param = param;
			this.handlers.add(handler);
		}
		
		public void addHandler(@Nonnull INistenerBooleanObject<ResultPair<Param, Result>> handler)
		{
			handlers.add(handler);
		}
	}
}
