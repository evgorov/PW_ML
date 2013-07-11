package org.omich.velo.log;

import javax.annotation.Nonnull;

/**
 * Доносит конкретные сообщения до конечного хранилища логов: файла, интернета, стандартного выхода и т.п.
 */
public interface ILog
{
	void log (@Nonnull String msg, @Nonnull Level level);
	void log (@Nonnull String tag, @Nonnull String msg, @Nonnull Level level);
}
