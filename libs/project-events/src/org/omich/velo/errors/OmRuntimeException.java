package org.omich.velo.errors;

/**
 *  Используется, когда невозможное Exception хочется превратить в RuntimeException,
 *  чтобы не таскать по всей цепочке вызовов хвостик throws.
 *  В этом случае применять с аргументом message равным IMPOSSIBLE_EXCEPTION
 *  
 *  Или когда мы пишем библиотеку и надо бросить исключение пользователю библиотеки,
 *  но сигнатуры методов Android-библиотеки не позволяют бросать обычный Exception.
 *  В этом случае рекомендую наследоваться от OmRuntimeException
 *  и в сообщении подробно описывать в чём пользователь неправ. 
 */
@Deprecated
public class OmRuntimeException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public static final String IMPOSSIBLE_EXCEPTION = "Impossible exception"; //$NON-NLS-1$
	public static final String UNSUPPORTED_MD5 = "Unsupported MD5"; //$NON-NLS-1$
	
	public OmRuntimeException (String message)
	{
		super(message);
	}
	
	public OmRuntimeException (String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public OmRuntimeException (String message, String moreMessage)
	{
		super(message + ": " + moreMessage); //$NON-NLS-1$
	}
	
	public OmRuntimeException (String message, String moreMessage, Throwable cause)
	{
		super(message + ": " + moreMessage, cause); //$NON-NLS-1$
	}
}
