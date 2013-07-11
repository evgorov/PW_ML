package org.omich.velo.bcops.log;

import javax.annotation.Nonnull;


/**
 * Исключения, которые способны генерить из себя экземпляры ErrorParcelable. 
 */
public class ParcException extends Exception
{
	private static final long serialVersionUID = 1L;

	public ParcException (String message)
	{
		super(message);
	}
	
	public ParcException (String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public @Nonnull ErrorParcelable createErrorParcelable ()
	{
		ErrorParcelable err = new ErrorParcelable(this);
		return err;
	}
}
