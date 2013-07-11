package org.omich.velo.log;

import java.io.OutputStream;

import javax.annotation.Nonnull;

public interface INetLogStorage 
{
	@Nonnull byte[] getLogs();
	void writeLogsToOutputStream(@Nonnull OutputStream os);
	void clearLogs();
}
