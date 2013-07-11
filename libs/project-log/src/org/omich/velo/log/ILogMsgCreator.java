package org.omich.velo.log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ILogMsgCreator
{
	@Nonnull String getMsg (@Nullable Throwable er, @Nonnull Level level);
	@Nonnull String getMsg (@Nullable ILoggable er, @Nonnull Level level);
}
