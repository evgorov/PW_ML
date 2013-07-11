package org.omich.velo.bcops;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import android.content.Intent;

public interface ITaskCreator
{
	@Nullable<T> T getTaskByIntent (@Nonnull Intent intent);
}
