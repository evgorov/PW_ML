package org.omich.velo.lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.handlers.IListenerInt;
import org.omich.velo.events.PairListeners.INistenerBooleanObject;
import org.omich.velo.lists.ILoadingQueue.IBgDownloader;
import org.omich.velo.lists.ILoadingQueue.ResultPair;
import org.omich.velo.log.Log;

abstract public class ArraySlowSource<Quick, Slow, SlowParam>
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

	private final int mLength;
	private final @Nonnull Item<Quick, Slow>[] mItems;
	private final @Nonnull SlowStatus[] mSlowStatuses;
	private final @Nonnull List<Integer> mLoadedIndexes = new ArrayList<Integer>();//Благодаря этому массиву, мы можем понимать, какие объекты были загружены раньше, а какие - позже.
	
	private @Nonnull LoadingQueue<SlowParam, Slow> mLoadingQueue;

	private boolean mIsDestroyed;
	
	public ArraySlowSource(final @Nonnull Item<Quick, Slow>[] items,
			@Nonnull IBgDownloader<SlowParam, Slow> bgDownloader)
	{
		this(items.length,
				new ArrayIterator<Item<Quick, Slow>>(items),
				bgDownloader);
	}

	public ArraySlowSource(@Nonnull Collection<Item<Quick, Slow>> items,
			@Nonnull IBgDownloader<SlowParam, Slow> bgDownloader)
	{
		this(items.size(), items.iterator(), bgDownloader);
	}
	
	public ArraySlowSource (int size, 
			Iterator<Item<Quick, Slow>> iterator,
			@Nonnull IBgDownloader<SlowParam, Slow> bgDownloader)
	{
		mLoadingQueue = new LoadingQueue<SlowParam, Slow>(bgDownloader);
		mSlowStatuses = new SlowStatus[size];
		@SuppressWarnings("unchecked")
		Item<Quick, Slow>[] localItems = new Item[size];
		mItems = localItems;
		
		int nulls = 0;
		int i = 0;
		while(iterator.hasNext() && i < size)
		{
			Item<Quick, Slow> item = iterator.next();
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
		mLoadingQueue.pauseResource();
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
			Item<Quick, Slow> item = mItems[i];
			Slow slow = item.slow;
			if(slow != null)
			{
				destroySlowItem(slow);
			}
			destroyQuickItem(item.quick);
		}

		mIsDestroyed = true;
	}

	//==== protected interface ===============================================
	abstract protected long getItemId(Quick quick, int position);
	abstract protected @Nullable SlowParam getSlowParam(Quick quick, int position);
	abstract protected void destroyQuickItem (@Nonnull Quick quick);
	abstract protected void destroySlowItem (@Nonnull Slow slowItem);


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
		if(!isCorrectPos(position))
			throw new ArrayIndexOutOfBoundsException(position);
		
		Item<Quick, Slow> item = mItems[position];
		if(item == null)
		{
			throw new NullPointerException("Must not be null elements in the mItems array. That mean some bug in ListSlowSource"); //$NON-NLS-1$
		}
		return item;
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
		if(slow != null)
		{
			mSlowStatuses[position] = SlowStatus.LOADED;
			handler.handle(position);
			return true;
		}
		
		SlowParam slowParam = getSlowParam(quick, position);
		if(slowParam != null && mSlowStatuses[position] != SlowStatus.LOADING)
		{
			mSlowStatuses[position] = SlowStatus.LOADING;
			mLoadingQueue.pushOrder(slowParam, new INistenerBooleanObject<ResultPair<SlowParam,Slow>>()
			{
				public void handle(boolean isSuccess, @Nonnull ResultPair<SlowParam, Slow> result)
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
			SlowParam slowParam = getSlowParam(mItems[position].quick, position);
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
			}
		}
	}

	private void handleSlowDataLoaded (int position, boolean isSuccess,
			@Nonnull ResultPair<SlowParam, Slow> result, @Nonnull IListenerInt handler)
	{
		Slow slow = result.result;
		if(!isSuccess || slow == null)
		{
			mSlowStatuses[position] = null;
			if(slow != null)
			{
				destroySlowItem(slow);
			}
		}
		else
		{
			clearSlowItem(position, slow);
			mSlowStatuses[position] = SlowStatus.LOADED;
			mLoadedIndexes.add(Integer.valueOf(position));
			handler.handle(position);
		}		
	}
	
	private void clearSlowItem (int position, @Nullable Slow replaceBy)
	{
		Slow slow = mItems[position].slow;
		if(slow != null)
		{
			destroySlowItem(slow);
		}
		mItems[position] = new Item<Quick, Slow>(mItems[position].quick, replaceBy);
		mLoadedIndexes.remove(Integer.valueOf(position));
	}

	private void clearReleased ()
	{
		for(int i = 0; i < mLength; ++i)
		{
			if(mSlowStatuses[i] == SlowStatus.RELEASED)
			{
				Slow slow = mItems[i].slow;
				assert slow != null;
				destroySlowItem(slow);
				mItems[i] = new Item<Quick, Slow>(mItems[i].quick, null);
				mSlowStatuses[i] = null;
			}
		}
	}

	private boolean isCorrectPos (int position)
	{
		return position >= 0 && position < mLength;
	}

	//==== part of constructor ================================================
	private int appendItemAndGetNulls (@Nullable Item<Quick, Slow> item, int index, int nulls)
	{
		if(item != null)
		{
			mItems[index - nulls] = item;
			if(item.slow != null)
			{
				mSlowStatuses[index - nulls] = SlowStatus.RELEASED;
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
