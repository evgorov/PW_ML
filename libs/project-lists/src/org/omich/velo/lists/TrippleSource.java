package org.omich.velo.lists;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.events.PairListeners.INistenerBooleanObject;
import org.omich.velo.handlers.IListenerInt;
import org.omich.velo.lists.ILoadingQueue.IBgDownloader;
import org.omich.velo.lists.ILoadingQueue.ResultPair;
import org.omich.velo.log.Log;

import android.util.Pair;

abstract public class TrippleSource <Quick, Slow, SlowSmall, SlowParam>
implements ISlowSource<Quick, Slow>
{
	public static class SourceException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		private static final String TRY_TO_OPEN_DESTROYED = "There was try to open destroyed ListSlowSource."; //$NON-NLS-1$
				
		public SourceException(String message)
		{
			super(message);
		}
	}
	
	private static enum SlowStatus
	{
		LOADING, LOADED, RELEASED;//null if null
	}

	public static class Tripple<Quick, Slow, SlowSmall>
	{
		final public @Nonnull Quick quick;
		final public @Nullable Slow slow;
		final public @Nullable SlowSmall slowSmall;
		
		public Tripple(@Nonnull Quick quick, @Nullable Slow slow, @Nullable SlowSmall slowSmall)
		{
			this.quick = quick;
			this.slow = slow;
			this.slowSmall = slowSmall;
		}
	}

	
	

	private final int mLength;
	private final @Nonnull Tripple<Quick, Slow, SlowSmall>[] mItems;
	private final @Nonnull SlowStatus[] mSlowStatuses;
	private final @Nonnull List<Integer> mLoadedIndexes = new LinkedList<Integer>();//Благодаря этому массиву, мы можем понимать, какие объекты были загружены раньше, а какие - позже.
	
	private @Nonnull LoadingQueue<SlowParam, Pair<Slow, SlowSmall>> mLoadingQueue;
	private final int maxLoadedCount;

	private boolean mIsDestroyed;
	
	public TrippleSource(final @Nonnull Tripple<Quick, Slow, SlowSmall>[] items,
			@Nonnull IBgDownloader<SlowParam, Pair<Slow, SlowSmall>> bgDownloader,
			int maxLoadedCount)
	{
		this(items.length,
				new ArrayIterator<Tripple<Quick, Slow, SlowSmall>>(items),
				bgDownloader, maxLoadedCount);
	}

	public TrippleSource(@Nonnull Collection<Tripple<Quick, Slow, SlowSmall>> items,
			@Nonnull IBgDownloader<SlowParam, Pair<Slow, SlowSmall>> bgDownloader,
			int maxLoadedCount)
	{
		this(items.size(), items.iterator(), bgDownloader, maxLoadedCount);
	}
	
	public TrippleSource (int size, 
			Iterator<Tripple<Quick, Slow, SlowSmall>> iterator,
			@Nonnull IBgDownloader<SlowParam, Pair<Slow, SlowSmall>> bgDownloader,
			int maxLoadedCount)
	{
		mLoadingQueue = new LoadingQueue<SlowParam, Pair<Slow, SlowSmall>>(bgDownloader);
		this.maxLoadedCount = maxLoadedCount; 
		mSlowStatuses = new SlowStatus[size];
		@SuppressWarnings("unchecked")
		Tripple<Quick, Slow, SlowSmall>[] localItems = new Tripple[size];
		mItems = localItems;
		
		int nulls = 0;
		int i = 0;
		while(iterator.hasNext() && i < size)
		{
			Tripple<Quick, Slow, SlowSmall> item = iterator.next();
			nulls = appendItemAndGetNulls(item, i, nulls);
			++i;
		}

		mLength = i - nulls;
	}
	
	public void pauseResource (boolean clearReleased)
	{
		mLoadingQueue.pauseResource();
		mLoadingQueue.resetQueue();
		removeLoadingStatuses();
		if(clearReleased)
		{
			clearReleased();
		}
//		mLoadingQueue.pauseResource();
	}

	public void resumeResource ()
	{
		if(mIsDestroyed)
		{
			throw new SourceException(SourceException.TRY_TO_OPEN_DESTROYED); 
		}
		mLoadingQueue.resumeResource();
	}

	public void close ()
	{
		pauseResource(false);

		for(int i = 0; i < mLength; ++i)
		{
			Tripple<Quick, Slow, SlowSmall> item = mItems[i];
			Slow slow = item.slow;
			if(slow != null)
			{
				destroySlowItem(slow);
			}
			destroyQuickItem(item.quick);
		}

		mIsDestroyed = true;
	}
	
	public @Nonnull Tripple<Quick, Slow, SlowSmall> getTripple(int position)
	{
		if(!isCorrectPos(position))
			throw new ArrayIndexOutOfBoundsException(position);
		
		Tripple<Quick, Slow, SlowSmall> item = mItems[position];
		if(item == null)
		{
			throw new NullPointerException("Must not be null elements in the mItems array. That mean some bug in ListSlowSource"); //$NON-NLS-1$
		}
		return item;		
	}

	//==== protected interface ===============================================
	abstract protected long getItemId(@Nonnull Quick quick, int position);
	abstract protected @Nullable SlowParam getSlowParam(@Nonnull Quick quick, @Nullable SlowSmall slowSmall, int position);
	abstract protected void destroyQuickItem (@Nonnull Quick quick);
	abstract protected void destroySlowItem (@Nonnull Slow slowItem);
	abstract protected void destroySlowSmallItem (@Nonnull SlowSmall slowSmallItem);


	//==== ISource ============================================================
	final public long getItemId(int position)
	{
		return getItemId(mItems[position].quick, position);
	}

	final public int getLenght()
	{
		return mLength;
	}

	//==== ISlowSource ========================================================
	public @Nonnull	Item<Quick, Slow> getItem(int position)
	{
		Tripple<Quick, Slow, SlowSmall> item = getTripple(position);
		return new Item<Quick, Slow>(item.quick, item.slow);
	}

	/**
	 * @param position
	 * @param handler
	 * @return false, если данных нет, и true, если данные уже были загружены
	 */
	public boolean startLoadSlowData(final int position, final @Nonnull IListenerInt handler)
	{
		if(!isCorrectPos(position))
			return false;

		Slow slow = mItems[position].slow;
		Quick quick = mItems[position].quick;
		SlowSmall slowSmall = mItems[position].slowSmall;
		if(slow != null)
		{
			mSlowStatuses[position] = SlowStatus.LOADED;
			handler.handle(position);
			return true;
		}
		
		SlowParam slowParam = getSlowParam(quick, slowSmall, position);
		if(slowParam != null && mSlowStatuses[position] != SlowStatus.LOADING)
		{
			mSlowStatuses[position] = SlowStatus.LOADING;
			mLoadingQueue.pushOrder(slowParam, new INistenerBooleanObject<ResultPair<SlowParam,Pair<Slow, SlowSmall>>>()
			{
				public void handle(boolean isSuccess, @Nonnull ResultPair<SlowParam, Pair<Slow, SlowSmall>> result)
				{
					if(mIsDestroyed)
						return;

					handleSlowDataLoaded(position, isSuccess, result, handler);
				}
			});
		}
		return false;
	}

	public void releaseSlowData(int position)
	{
		if(!isCorrectPos(position))
			return;

		if(mItems[position].slow != null)
		{
			mSlowStatuses[position] = SlowStatus.RELEASED;
		}
		else if(mSlowStatuses[position] == SlowStatus.LOADING)
		{
			SlowParam slowParam = getSlowParam(mItems[position].quick, mItems[position].slowSmall, position);
			if(slowParam != null && mLoadingQueue.popOrder(slowParam))
			{
				mSlowStatuses[position] = null;
			}
		}		
	}
	
	//=========================================================================
	private void removeLoadingStatuses()
	{
		for(int i = 0; i < mSlowStatuses.length; ++i)
		{
			if(mSlowStatuses[i] == SlowStatus.LOADING)
			{
				mSlowStatuses[i] = null;
				if(mItems[i].slow != null)
				{
					Log.e("Can't be here, if I'm loading the slow item, then I need it, so, it must b null"); //$NON-NLS-1$
				}
			}
		}
	}

	private void handleSlowDataLoaded (int position, boolean isSuccess,
			@Nonnull ResultPair<SlowParam, Pair<Slow, SlowSmall>> result, @Nonnull IListenerInt handler)
	{
		Pair<Slow, SlowSmall> res = result.result;
		Slow slow = res == null ? null : res.first;
		SlowSmall slowSmall = res == null ? null : res.second;

		if(!isSuccess || slow == null)
		{
			if(slow != null)
			{
				destroySlowItem(slow);
			}
			mSlowStatuses[position] = null;
			updateSlowItem(position, null, slowSmall);
		}
		else
		{
			updateSlowItem(position, slow, slowSmall);
			mSlowStatuses[position] = SlowStatus.LOADED;
			handler.handle(position);
		}
		cleanLoadedLimit();
	}
	
	private void updateSlowItem (int position, @Nullable Slow replaceBySlow,
			@Nullable SlowSmall replaceBySlowSmall)
	{
		Tripple<Quick, Slow, SlowSmall> item = mItems[position];
		Slow slow = item.slow;
		if(slow != null && slow != replaceBySlow)
		{
			destroySlowItem(slow);
		}
		SlowSmall slowSmall = item.slowSmall;
		if(slowSmall != null && slowSmall != replaceBySlowSmall)
		{
			destroySlowSmallItem(slowSmall);
		}

		mItems[position] = new Tripple<Quick, Slow, SlowSmall>(item.quick, replaceBySlow, replaceBySlowSmall);
		mLoadedIndexes.remove(Integer.valueOf(position));
		if(replaceBySlow != null)
		{
			mLoadedIndexes.add(Integer.valueOf(position));
		}
	}

	private void clearReleased ()
	{
		for(int i = 0; i < mLength; ++i)
		{
			if(mSlowStatuses[i] == SlowStatus.RELEASED)
			{
				Slow slow = mItems[i].slow;
				if(slow == null)
				{
					Log.e("It must not be null here"); //$NON-NLS-1$
				}
				assert slow != null;
				destroySlowItem(slow);
				mItems[i] = new Tripple<Quick, Slow, SlowSmall>(mItems[i].quick, null, mItems[i].slowSmall);
				mSlowStatuses[i] = null;
			}
		}
	}
	
	private void cleanLoadedLimit()
	{
		int size = mLoadedIndexes.size();
		while(size > maxLoadedCount)
		{
			int index = mLoadedIndexes.get(0).intValue();
			if(mSlowStatuses[index] == SlowStatus.RELEASED)
			{
				mLoadedIndexes.remove(0);
				Tripple<Quick, Slow, SlowSmall> item = mItems[index];
				updateSlowItem(index, null, item.slowSmall);
				mSlowStatuses[index] = null;
				--size;
			}
			else
			{
				size = 0;
			}
		}
	}

	private boolean isCorrectPos (int position)
	{
		return position >= 0 && position < mLength;
	}

	//==== part of constructor ================================================
	private int appendItemAndGetNulls (@Nullable Tripple<Quick, Slow, SlowSmall> item, int index, int nulls)
	{
		if(item != null)
		{
			mItems[index - nulls] = item;
			if(item.slow != null)
			{
				mSlowStatuses[index - nulls] = SlowStatus.RELEASED;
				mLoadedIndexes.add(Integer.valueOf(index-nulls));
			}
		}
		else
		{
			Log.i("Null item for source construction. Index: " + index); //$NON-NLS-1$
		}
		
		return nulls;
	}

	private static class ArrayIterator<E> implements Iterator<E>
	{
		private int mI = 0;
		private final @Nonnull E[] mItems;
		
		ArrayIterator(@Nonnull E[] items)
		{
			mItems = items;
		}

		public boolean hasNext()
		{
			return mI < mItems.length;
		}

		public E next()
		{
			return mItems[mI++];
		}

		public void remove()
		{
			Log.wtf("Never use this method in ArraySlowSource"); //$NON-NLS-1$
			throw new ListsRuntimeException(ListsRuntimeException.IMPOSSIBLE_EXCEPTION);
		}
	}
}
