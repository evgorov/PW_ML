package org.omich.velo.bcops;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import android.os.Bundle;

public interface IBcBaseTask<TaskEnv>
{
	/**
	 * Исполнение задачи. Возвращаемый Bundle будет передан отправителю.
	 * 
	 * @param env
	 * @return
	 */
	@Nullable Bundle execute (@Nonnull TaskEnv env);
}
