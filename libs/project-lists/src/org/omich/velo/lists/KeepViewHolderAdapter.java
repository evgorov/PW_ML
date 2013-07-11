package org.omich.velo.lists;

import javax.annotation.Nonnull;


import android.content.Context;
import android.view.View;

abstract public class KeepViewHolderAdapter<ViewHolder> extends KeepViewAdapter
{
	protected KeepViewHolderAdapter (@Nonnull Context context)
	{
		super(context);
	}
	
	//==== protected interface ================================================
	/**
	 * Создаёт ViewHolder для объекта view.
	 * ViewHolder - класс, который содержит прямые ссылки на графические компоненты вьюшки.
	 * @param view - вьюшка внутри которой ищем графические компоненты
	 * @return ViewHolder для этой вьюшки.
	 */
	abstract protected @Nonnull ViewHolder createViewHolderOfView (@Nonnull View view);
	
	/**
	 * Заполняет ViewHolder данными, но на всякий случай также оставляем доступ к view.
	 * 
	 * @param viewHolder
	 * @param view
	 */
	abstract protected void fillViewHolderByData (@Nonnull ViewHolder viewHolder, 
			@Deprecated @Nonnull View view,
			int position);

	//==== KeepViewAdapter ========================================================
	/**
	 * Get a View that displays the data at the specified position in the data set.
	 */
	@Override
	final protected void fillViewByData (@Nonnull View view, int position)
	{
		@SuppressWarnings("unchecked") //Мы устанавливаем его тут и только тут, поэтому уверены в типе.
		ViewHolder holder = (ViewHolder)view.getTag();
		if(holder == null)
		{
			holder = createViewHolderOfView(view);
			view.setTag(holder);
		}
		
		fillViewHolderByData(holder, view, position);
	}
}
