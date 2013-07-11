package org.omich.velo.lists;

/**
 *  Используется, когда невозможное Exception хочется превратить в RuntimeException,
 *  чтобы не таскать по всей цепочке вызовов хвостик throws.
 *  В этом случае применять с аргументом message равным IMPOSSIBLE_EXCEPTION
 *  
 *  Или когда мы пишем библиотеку и надо бросить исключение пользователю библиотеки,
 *  но сигнатуры методов Android-библиотеки не позволяют бросать обычный Exception.
 */
public class ListsRuntimeException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public static final String IMPOSSIBLE_EXCEPTION = "Impossible exception"; //$NON-NLS-1$
	
	public ListsRuntimeException (String message)
	{
		super(message);
	}
	
	public ListsRuntimeException (String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public ListsRuntimeException (String message, String moreMessage)
	{
		super(message + ": " + moreMessage); //$NON-NLS-1$
	}
	
	public ListsRuntimeException (String message, String moreMessage, Throwable cause)
	{
		super(message + ": " + moreMessage, cause); //$NON-NLS-1$
	}
}
