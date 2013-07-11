package org.omich.velo.bcops.simple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.ByNameTaskCreator;
import org.omich.velo.bcops.IBcToaster;
import org.omich.velo.bcops.ICancelledInfo;
import org.omich.velo.bcops.simple.IBcTask.BcTaskEnv;
import org.omich.velo.handlers.IListenerInt;

import android.content.Context;
import android.os.Bundle;

/**
 * Сервис, реализующий поочерёдное создание и выполнение экземпляров IBcTask.
 */
public class BcService extends BcBaseService<BcTaskEnv>
{
	private static final String SERVICE_NAME = "BcService"; //$NON-NLS-1$

	//========================================================================
	public BcService ()
	{
		super(SERVICE_NAME, new ByNameTaskCreator());
	}

	@Override
	@Nonnull
	protected BcTaskEnv createTaskEnv(
			@Nullable Bundle extras, 
			@Nonnull Context context,
			@Nonnull  IBcToaster bcToaster,
			@Nullable IListenerInt ph, 
			@Nullable ICancelledInfo ci)
	{
		return new BcTaskEnv(extras, context, bcToaster, ph, ci);
	}
}
