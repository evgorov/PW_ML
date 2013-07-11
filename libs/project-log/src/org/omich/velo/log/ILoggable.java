package org.omich.velo.log;

import javax.annotation.Nonnull;

public interface ILoggable
{
	@Nonnull String getShortLogMessage ();
	@Nonnull String getFullLogMessage ();
}
