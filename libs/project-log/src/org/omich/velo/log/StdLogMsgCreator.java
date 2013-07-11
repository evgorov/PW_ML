package org.omich.velo.log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StdLogMsgCreator implements ILogMsgCreator
{
	public @Nonnull String getMsg (@Nullable Throwable er, @Nonnull Level level)
	{
		return getMsg (new LoggableOfThrowable(er), level);
	}

	public @Nonnull String getMsg (@Nullable ILoggable lg, @Nonnull Level level)
	{
		if(lg == null)
		{
			return LogUtil.NULL_STRING_E;
		}

		switch(level)
		{
			case WTF:
			case E:
			case W:
				return lg.getFullLogMessage();
			case I:
			case D:
			default:
				return lg.getShortLogMessage();
		}
	}
}
