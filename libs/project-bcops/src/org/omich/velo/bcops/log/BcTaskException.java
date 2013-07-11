package org.omich.velo.bcops.log;


/**
 * Типичное исключение, которое может бросить IBcTask в методе execute();
 */
public class BcTaskException extends ParcException
{
	private static final long serialVersionUID = 1L;

	public static final String ERRTYPE_DATABASE    = "BcTaskException.database"; //$NON-NLS-1$
	public static final String ERRTYPE_NETWORK     = "BcTaskException.network"; //$NON-NLS-1$
	public static final String ERRTYPE_NO_INTERNET = "BcTaskException.noInternet"; //$NON-NLS-1$
	public static final String ERRTYPE_OTHER       = "BcTaskException.other"; //$NON-NLS-1$

	public BcTaskException (String message)
	{
		super(message);
	}
	
	public BcTaskException (String message, Throwable cause)
	{
		super(message, cause);
	}
}
