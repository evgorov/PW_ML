package org.omich.velo.bcops;

public class BcEnvException extends Exception
{
	private static final long serialVersionUID = 1L;

	public BcEnvException(String message)
	{
		super(message);
	}

	public BcEnvException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
