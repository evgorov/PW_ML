package org.omich.velo.log;

import javax.annotation.Nonnull;

public class StackTraceLoggable implements ILoggable
{
	private final @Nonnull StackTraceElement[] mTrace;
	
	public StackTraceLoggable (@Nonnull StackTraceElement[] trace)
	{
		mTrace = trace;
	}

	public @Nonnull String getShortLogMessage()
	{
		return LogUtil.getShortTraceMessage(0, 100);
	}

	public @Nonnull String getFullLogMessage()
	{
		return LogUtil.getStackTrace(mTrace);
	}
}
