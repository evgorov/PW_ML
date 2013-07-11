package org.omich.velo.lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.handlers.IListenerInt;

/**
 * Интерфейс взаимодействия адаптера с моделью приспособленный для отложенной загрузки данных.
 * 
 * Не приспособлен для одновременного взаимодействия с несколькими адаптерами. 
 */
public interface ISlowSource<Quick, Slow> extends ISource
{
	final public static class Item<Quick, Slow>
	{
		final public @Nonnull Quick quick;
		final public @Nullable Slow slow;
		
		public Item(@Nonnull Quick quick, @Nullable Slow slow)
		{
			this.quick = quick;
			this.slow = slow;
		}
	}
	
	int getLenght ();
	@Nonnull Item<Quick, Slow> getItem (int position);
	

	/**
	 * Если прямо сейчас данных нет, то возвращается false,
	 * 	когда данные загрузятся, вызовется handler.
	 * Если они уже загружены, то handler вызывается немедленно и возвращается true,
	 *  при этом данные ,объявленные освобождёнными снова объявляются как занятые,
	 *  таким образом нельзя использовать один SlowSource для разных источников.
	 *  TODO: вообще-то этоплохо, надо избавляться от такого недостатка.
	 * Если данные не загрузятся, то handler не вызовется.
	 * 
	 * @param position
	 * @param handler
	 * @return false, если данных нет, и true, если данные уже были загружены
	 */
	boolean startLoadSlowData (int position, @Nonnull IListenerInt handler);
	
	/**
	 * Объявление о том, что данные больше не используются.
	 *
	 * @param position
	 */
	void releaseSlowData (int position);
}
