package org.omich.velo.log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoggableOfThrowable implements ILoggable
{
	private final @Nullable Throwable mEr;
	public LoggableOfThrowable (@Nullable Throwable er) {mEr = er;}

	public @Nonnull String getShortLogMessage (){return LogUtil.getShortLogMessage(mEr);}
	public @Nonnull String getFullLogMessage (){return LogUtil.getFullLogMessage(mEr);}
}
