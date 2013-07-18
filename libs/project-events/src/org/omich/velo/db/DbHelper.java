package org.omich.velo.db;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.handlers.INistener;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Полезные функции для работы с базой данных
 */
public class DbHelper
{
	/**
	 * Пример использования: 
	 * 
	 * 	//==== SQLiteOpenHelper ===================================================
	 *  @Override
	 *  public void onCreate(SQLiteDatabase database) 
	 *  {
	 *      database.execSQL(CREATE_N_PREVIEWS_QUERY);
	 *      database.execSQL(CREATE_NEWS_QUERY);
	 *
	 *      database.execSQL(String.format(CREATE_INDEX_TEMPLATE, "pictureServerId", "pictures"));
	 *  }
	 */
	public static final @Nonnull String CREATE_INDEX_TEMPLATE = "create index %1$s_index on %2$s (%1$s)"; //$NON-NLS-1$
	
	/**
	 *Итератор курсора. Используется в методе iterateCursorAndClose
	 */
	abstract public static class CursorIterator implements INistener<Cursor>
	{
		private boolean mIsTerminated;
		
		protected void terminate () {mIsTerminated = true;}
		public boolean isTerminated () {return mIsTerminated;}
	}

	public static Cursor queryBySingleColumn (
			@Nonnull SQLiteDatabase db,
			@Nonnull String table, 
			@Nonnull String [] columns,
			@Nonnull String searchColumn, 
			@Nullable String searchValue)
	{
		return db.query(table, columns, 
				searchColumn + "= ? ", new String[]{searchValue}, //$NON-NLS-1$
				null, null, null);
	}

	public static Cursor queryBySingleColumn (
			@Nonnull SQLiteDatabase db,
			@Nonnull String table, 
			@Nonnull String [] columns,
			@Nonnull String searchColumn, 
			long searchValue)
	{
		return db.query(table, columns, 
				searchColumn + "= " + searchValue,  //$NON-NLS-1$
				null, null, null, null);
	}

	/**
	 * Метод, который для каждой позиции курсора запускает итератор.
	 * По окончании работы НЕ вызывает cursor.close();
	 * Тот кто вызывает метод, сам отвечает за закрытие курсора.
	 * 
	 * Если в процессе обработки был вызван iterator.terminate(), то выполнение
	 * прерывается
	 * 
	 * 
	 * Пример использования:
	 * 
	 * 	Cursor cursor = getDb().query(TNAME_TICKETS, 
	 *		TICKETS_COLUMNS, 
	 *		null, null, null, null, TicketsCols.NAME);
	 *
	 *	final List<Ticket> tickets = new ArrayList<Ticket>();
	 *
	 *	if(cursor != null)
	 *	{
	 *		DbHelper.iterateCursorAndClose(cursor, new CursorIterator()
	 *		{
	 *			@Override
	 *			public void handle (@Nonnull Cursor cursor)
	 *			{
	 *				if (!isCancelled())
	 *				{
	 *					tickets.add(createTicketByCursor(cursor));
	 *				}
	 *				else
	 *				{
	 *					tickets.clear();
	 *					this.terminate();
	 *				}
	 *			}
	 *		});
	 *
	 *      cursor.close();
	 *	}
	 *	return tickets;
	 * 
	 * @param cursor
	 * @param iterator
	 */
	public static void iterateCursor(
			@Nonnull Cursor cursor,
			@Nonnull CursorIterator iterator)
	{
		cursor.moveToFirst();
		while(!cursor.isAfterLast() && !iterator.isTerminated())
		{
			iterator.handle(cursor);
			cursor.moveToNext();
		}
	}

	@Deprecated
	public static void iterateCursorAndClose (
			@Nonnull Cursor cursor,
			@Nonnull CursorIterator iterator)
	{
		iterateCursor(cursor, iterator);
		cursor.close();
	}
}
