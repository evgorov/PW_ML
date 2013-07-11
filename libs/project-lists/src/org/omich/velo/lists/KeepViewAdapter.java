package org.omich.velo.lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Наследник от BaseAdapter.
 * Призван:
 *  - переиспользовать существующие элементы списка в методе getView.
 *  - все View элементов однотипные и layout этих элементов запрашивается у наследника.
 *  
 *  Методы для переопределения:
 *  
 *	- int getItemViewResId ();
 *	- void fillViewByData (@Nonnull View view, int position);
 *
 * @param <Item> - тип элемента списка.
 */
abstract public class KeepViewAdapter extends BaseAdapter
{
	private @Nonnull Context mContext;

	protected KeepViewAdapter (@Nonnull Context context)
	{
		mContext = context;
	}
	
	//==== protected interface ================================================
	/**
	 * Выдаёт идентификатор layout-ресурса, по которому должен генерироваться View элемента списка
	 * @return
	 */
	abstract protected int getItemViewResId ();

	/**
	 * Заполняет View элемента данными.
	 * 
	 * @param view
	 * @param position
	 */
	abstract protected void fillViewByData (@Nonnull View view, int position);
	
	//==== BaseAdapter ========================================================
	/**
	 * Get a View that displays the data at the specified position in the data set.
	 */
	final public View getView (int position, @Nullable View convertView, ViewGroup parent)
	{
		View v = convertView;
		if(v == null)
		{
			LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(getItemViewResId(), null);
		}

		if(v != null)
		{
			fillViewByData(v, position);
		}

		return v;
	}
}
