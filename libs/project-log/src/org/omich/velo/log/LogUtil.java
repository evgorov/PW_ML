package org.omich.velo.log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.cast.NonnullableCasts;

public class LogUtil 
{
	public static final @Nonnull String NULL_STRING_E = "E__NULL__"; //$NON-NLS-1$
	public static final @Nonnull String NULL_STRING_S = "S__NULL__"; //$NON-NLS-1$
	public static final @Nonnull String NULL_STRING_C = "C__NULL__"; //$NON-NLS-1$
	
	public static @Nonnull String getShortLogMessage (@Nullable Throwable er)
	{	
		if(er == null)
			return NULL_STRING_E;
		
		return er.getClass().getSimpleName() + ": " + getNotNullMessage(er.getMessage()); //$NON-NLS-1$
	}
	
	public static @Nonnull String getFullLogMessage (@Nullable Throwable er)
	{	
		if(er == null)
			return NULL_STRING_E;

		return er.getClass().getSimpleName() + ": "  //$NON-NLS-1$
				+ getNotNullMessage(er.getMessage()) + "\n" //$NON-NLS-1$
				+ getStackTrace(er.getStackTrace());
	}

	public static @Nonnull String getNotNullMessage (@Nullable String msg)
	{
		return msg == null ? NULL_STRING_S : msg;
	}
	
	public static @Nonnull String getStackTrace (@Nullable StackTraceElement[] ste)
	{
		return getLongTraceMessage(ste, "\n\t"); //$NON-NLS-1$
	}

	public static @Nonnull String getLongTraceMessage (@Nullable StackTraceElement[] ste, @Nonnull String sep)
	{
		if(ste == null)
			return ""; //$NON-NLS-1$

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < 10 && i < ste.length; ++i)
		{
			sb.append(ste[i].toString());
			sb.append(sep);
		}
		return NonnullableCasts.stringBuilderToString(sb);
	}

	public static @Nonnull String getShortTraceMessage (int start, int end)
	{
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		StringBuilder sb = new StringBuilder();
		for(int i = start; i <= end && i < trace.length; i++)
		{
			StackTraceElement elem = trace[i];
			if(elem != null)
			{
				sb.append(getTraceMessageItem(elem)).append(' ');
			}
			else
			{
				sb.append(" -- "); //$NON-NLS-1$
			}
		}
		return NonnullableCasts.stringBuilderToString(sb);
	}

	private static @Nonnull String getTraceMessageItem (@Nonnull StackTraceElement elem)
	{
		String fileName = elem.getFileName();
		int ind = fileName.indexOf('.');
		if(ind >= 0)
		{
			fileName = fileName.substring(0, ind);
		}
		return fileName + ":" + elem.getLineNumber(); //$NON-NLS-1$
	}
}
