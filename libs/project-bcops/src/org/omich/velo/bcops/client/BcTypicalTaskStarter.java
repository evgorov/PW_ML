package org.omich.velo.bcops.client;

import javax.annotation.Nonnull;

import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.handlers.IListener;

import android.content.Intent;
import android.os.Bundle;

public class BcTypicalTaskStarter
{
	public final @Nonnull Intent intent;
	public final @Nonnull Class<? extends IBcTask> taskClass;
	public final @Nonnull IListener<Bundle> handler;
	
	public BcTypicalTaskStarter(@Nonnull Intent intent,
			@Nonnull Class<? extends IBcTask> taskClass,
			@Nonnull IListener<Bundle> handler)
	{
		this.intent = intent;
		this.taskClass = taskClass;
		this.handler = handler;
	}
}
