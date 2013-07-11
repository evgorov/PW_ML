package org.omich.velo.lists;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;

/**
 * Призван следить за тем, какие элементы сейчас используются в различных View,
 * а какие - нет.
 * 
 * Если какой-то элемент освобождён всеми View, то вызывается
 * метод releaseSlowData(position), который говорит наследнику,
 * что данные можно не грузить, если они ещё не загрузились
 * или что можно очистить от них память, если они тяжёлые.
 */
abstract public class SlowAdapter<ViewHolder> extends KeepViewHolderAdapter<ViewHolder>
{
	private Map<View, ViewWithPosition> mViewItemsByView = new HashMap<View, ViewWithPosition>();
	private SparseArray<Set<ViewWithPosition>> mViewItemsByPosition = new SparseArray<Set<ViewWithPosition>>();

	protected SlowAdapter(@Nonnull Context context)
	{
		super(context);
	}
	
	//==== protected abstract methods =========================================
	/**
	 * Заполнить View с учётом SlowData.
	 * Может вызываться сразу для нескольких View, если у нас в данных они претендуют на отображение
	 * элемента с конкретным индексом (position).
	 * 
	 * @param view - View, которое надо заполнить.
	 * @param position - индекс в адаптере
	 */
	abstract protected void updateViewBySlowData (@Nonnull ViewHolder viewHolder, 
			@Deprecated @Nonnull View view,
			int position);
	
	/**
	 * Заполнить элемент теми данными, которые есть и вызвать загрузку SlowData.
	 * После того, как SlowData будет загружена, или она уже была загружена,
	 * нужно вызвать метод onSlowDataLoaded(), чтобы заполнить обновлёнными данными
	 * все претендующие View. 
	 * 
	 * @param view
	 * @param position
	 */
	abstract protected void fillViewWithoutSlowData (@Nonnull ViewHolder viewHolder, 
			@Deprecated @Nonnull View view,
			int position);
	
	/**
	 * Через этот метод родитель сообщает наследнику, что не осталось ни одного View,
	 * предендующего на отображение элемента данной position.
	 * Данные можно удалить или перестать грузить, если они ещё не были загружены.
	 * @param position
	 */
	abstract protected void releaseSlowData (int position);

	//==== protected events ===================================================
	/**
	 * Наследник сообщает родителю,
	 * что все данные были перезагружены, всё что было сохранено - устарело.
	 */
	protected void onSourceRefreshed ()
	{
		mViewItemsByView.clear();
		mViewItemsByPosition.clear();
		notifyDataSetChanged();
	}

	/**
	 * Наследник сообщает родителю, что загрузилась SlowData,
	 * надо обновить претендующие View.
	 * 
	 * @param data
	 */
	public void onSlowDataLoaded (int position)
	{
		//Когда slowData загрузилась находим View, которые соответствуют данной позиции
		//и заполняем их данными.

		Set<ViewWithPosition> items = getViewsSetByPosition(position);
		for(ViewWithPosition item : items)
		{
			if(item.position == position)
			{
				updateViewBySlowData(item.viewHolder, item.v, position);
			}
		}
	}

	//==== KeepViewAdapter implementation =====================================
	@Override
	final protected void fillViewHolderByData (@Nonnull ViewHolder viewHolder, 
			@Deprecated @Nonnull View view,
			int position)
	{
		//Смотрим, существовал ли данный View раньше.
		//Если существовал, то освобождаем те ресурсы, которые он использовал.
		ViewWithPosition item = mViewItemsByView.get(view);
		if(item != null)
		{
			cleanupViewItem(item);
		}

		//Сохраняем информацию о том, что в данный View загружент элемент с данной position.
		setViewPosition(position, view, viewHolder);
		fillViewWithoutSlowData(viewHolder, view, position);
	}

	//=========================================================================
	private @Nonnull Set<ViewWithPosition> getViewsSetByPosition (int position)
	{
		Set<ViewWithPosition> set = mViewItemsByPosition.get(position);
		if(set == null)
		{
			set = new HashSet<ViewWithPosition>();
			mViewItemsByPosition.put(position, set);
		}
		return set;
	}

	private void cleanupViewItem (@Nonnull ViewWithPosition item)
	{
		//При освобождении очередного View проверяем, остались ли ещё View, соответствующие данному элементу,
		//Если нет, то помечаем его в источнике, как освобождённый, при необходимости его там освободят.
		int position = item.position;

		Set<ViewWithPosition> set = getViewsSetByPosition(position);
		set.remove(item);
		item.position = -1;
		
		if(set.isEmpty())
		{
			releaseSlowData(position);
		}
	}

	/**
	 * Застолбить соответствие между View и position.
	 * Так в будущем мы сможем по View определить position,
	 * а по position выколупаем все View, которые к нему относятся.
	 */
	private void setViewPosition (int position, @Nonnull View view, @Nonnull ViewHolder viewHolder)
	{
		ViewWithPosition vItem;
		if(!mViewItemsByView.containsKey(view))
		{
			vItem = new ViewWithPosition(view, viewHolder);
			mViewItemsByView.put(view, vItem);
		}
		else
		{
			vItem = mViewItemsByView.get(view);
		}
		vItem.position = position;
		getViewsSetByPosition(position).add(vItem);
	}

	/**
	 * Пара: View view + int position
	 */
	private class ViewWithPosition
	{
		@Nonnull final View v;
		@Nonnull final ViewHolder viewHolder;
		int position;
		
		public ViewWithPosition(@Nonnull View v, @Nonnull ViewHolder viewHolder)
		{
			this.v = v;
			this.viewHolder = viewHolder;
		}
	}
}
