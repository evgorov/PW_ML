package org.omich.velo.lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.omich.velo.events.PairListeners.IListenerBooleanObject;
import org.omich.velo.events.PairListeners.INistenerBooleanObject;

public interface ILoadingQueue<Param, Result>
{
	@Immutable
	public static class ResultPair<First, Second>
	{
		public final @Nonnull First param;
		public final @Nullable Second result;
		
		public ResultPair(@Nonnull First param, @Nullable Second result)
		{
			this.param = param;
			this.result = result;
		}
	}
	
	public static interface IBgDownloader<Param, Result>
	{
		/**
		 * Загрузить элемент, после завершения загрузки (успешной или неуспешной)
		 * вызывать handler.
		 * 
		 * В хэндлер передаёт флаг об успехе, а также пару: {объект param, результат загрузки}.
		 * 
		 * Если был вызван до окончания предыдущей загрузки, то предыдущая загрузка отменяется.
		 *
		 * @param position
		 * @param handler
		 */
		void loadSlowData (@Nonnull Param param,
				@Nonnull IListenerBooleanObject<Result> handler);
		
		/**
		 * Отменить загрузку элемента, если это возможно.
		 * 
		 * После этого очередь уже не ждёт никаких ответов.
		 * Если загрузка не отменяется, а успевает завершиться, то не следует вызывать handler очереди.
		 */
		void cancelLoadingSlowData ();
		
	}

	public void pushOrder (@Nonnull Param param, @Nonnull INistenerBooleanObject<ResultPair<Param, Result>> handler);
	
	/**
	 * Вытащить элемент из очереди, если она до него ещё не дошла.
	 * Иными словами, отменить загрузку этого элемента.
	 * 
	 * @param param
	 * @return
	 */
	public boolean popOrder (@Nonnull Param param);
	
	/**
	 * Очистить очередь, и отказаться от загрузки текущего элемента.
	 */
	public void resetQueue ();
}
