package org.omich.velo.bcops;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.log.Log;

import android.content.Intent;

public class ByNameTaskCreator implements ITaskCreator
{
	public static @Nonnull String getTaskTypeId (@Nonnull Class<?> classs)
	{
//		return NonnullableCasts.classGetCanonicalName(classs);
		return NonnullableCasts.classGetName(classs);
	}
	//==== ITaskCreator ======================================================
	@SuppressWarnings("unchecked")
	@Override
	public @Nullable<T> T getTaskByIntent(@Nonnull Intent intent)
	{		
		try
		{
			String className = intent.getExtras().getString(BcBaseService.BF_OP_TYPE_ID);
			return (T)Class.forName(className).newInstance();
		}
		catch (Throwable e)
		{
			Log.w(e);
		}
		return null;
	}
}
