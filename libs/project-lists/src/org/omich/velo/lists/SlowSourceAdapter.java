package org.omich.velo.lists;

import javax.annotation.Nonnull;

import org.omich.velo.handlers.IListenerInt;
import org.omich.velo.lists.ISlowSource.Item;

import android.content.Context;
import android.view.View;

/**
 * Адаптер, который берёт быстрые и долгие данные из ISlowSource.
 * Насследуется от SlowAdapter, т.е. поддерживает предложенный механизм загрузки долгих данных.
 * 
 * Наследники реализуют:
 * - заполнение элемента View по данным,
 * - Выдачу ViewItemRsourceId,
 * - Передачу в конструктор конкретного экземпляра ISlowSource
 */
abstract public class SlowSourceAdapter<ViewHolder, Quick, Slow>
	extends SlowAdapter<ViewHolder>
{
	private @Nonnull ISlowSource<Quick, Slow> mSource;

	public SlowSourceAdapter (@Nonnull Context context,
			@Nonnull ISlowSource<Quick, Slow> slowSource)
	{
		super(context);
		mSource = slowSource;
	}

	//==== protected interface ===============================================
	/**
	 * –ó–∞–ø–æ–ª–Ω–∏—Ç—å view, —Å—á–∏—Ç–∞—è, —á—Ç–æ slowData e—â—ë –Ω–µ –∑–∞–≥—Ä—É–∂–µ–Ω–∞.
	 * 
	 * @param v
	 * @param item
	 * @param slowItem
	 * @param position
	 */
	abstract protected void appendQuickDataToView (@Nonnull ViewHolder viewHolder,
			@Nonnull Quick quick,
			@Deprecated @Nonnull View view,
			@Deprecated int position);
	
	abstract protected void appendSlowDataToView (@Nonnull ViewHolder viewHolder,
			@Nonnull Slow slow,
			@Deprecated @Nonnull View view,
			@Deprecated int position);
	
	final protected void setSlowSource (@Nonnull ISlowSource<Quick, Slow> source)
	{
		mSource = source;
		notifyDataSetChanged();
	}

	//==== SlowAdapter =======================================================
	@Override
	final protected void fillViewWithoutSlowData (@Nonnull ViewHolder viewHolder, 
			@Deprecated @Nonnull View view,
			int position)
    {
		Item<Quick, Slow> item = mSource.getItem(position);
		appendQuickDataToView(viewHolder, item.quick, view, position);
		Slow slow = item.slow;
		if(slow == null)
		{
			mSource.startLoadSlowData(position, mpOnSlowLoadedListener);
		}
		else
		{
			appendSlowDataToView(viewHolder, slow, view, position);
		}
    }
	
	@Override
	final protected void updateViewBySlowData (@Nonnull ViewHolder viewHolder, 
			@Deprecated @Nonnull View view,
			int position)
	{
		Slow slow = mSource.getItem(position).slow;
		if(slow != null)
		{
			appendSlowDataToView(viewHolder, slow, view, position);
		}
	}

	@Override
	final protected void releaseSlowData (int position)
	{
		mSource.releaseSlowData(position);
	}

	//==== BaseAdapter ========================================================
	@Override
	public int getCount () 
	{
		return mSource.getLenght();
	}

	@Override
	public long getItemId (int position) 
	{
		return mSource.getItemId(position);
	}
	
	public Object getItem (int position)
	{
		return mSource.getItem(position);
	}
	
	private final @Nonnull IListenerInt mpOnSlowLoadedListener = new IListenerInt()
	{
		public void handle (int position){onSlowDataLoaded(position);}
	};
}
