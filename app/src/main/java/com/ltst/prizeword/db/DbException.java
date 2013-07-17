package com.ltst.prizeword.db;

public class DbException extends Exception
{
    private static final long serialVersionUID = 1L;

    public static final String CANT_OPEN_DATABASE = "Can't open database"; //$NON-NLS-1$
    public static final String HELPER_CREATED_NULL_DATABASE = "SQLiteHelper created NULL database"; //$NON-NLS-1$

    public DbException(String message)
    {
        super(message);
    }

    public DbException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
