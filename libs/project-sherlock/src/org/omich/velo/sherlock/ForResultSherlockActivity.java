package org.omich.velo.sherlock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.activity.ForResultStarter;
import org.omich.velo.activity.IForResultStarter;

import com.actionbarsherlock.app.SherlockActivity;

import android.content.Intent;

/**
 * Аналог ForResultActivity, только наследник от SherlockActivity.
 * 
 * Необязательно наследоваться от этого класса или от ForResultActivity.
 * Достаточно встроить работу с экземпляром ForResultStarter в методы onActivityResult и onDestroy.
 *
 */
public class ForResultSherlockActivity extends SherlockActivity
{
	private @Nonnull ForResultStarter mForResultStarter = new ForResultStarter(this);

	//==== live cycle =========================================================
	@Override
	protected void onActivityResult (int reqCode, int resCode, @Nullable Intent data)
	{
		if(!mForResultStarter.onActivityResult(reqCode, data))
		{
			//Если mForResultStarter не нашёл обработчика, то передаём обработку вверх по иерархии
			super.onActivityResult(reqCode, resCode, data);
		}
	}

	//==== protected interface ===============================================
	protected @Nonnull IForResultStarter getForResultStarter (){return mForResultStarter;}
}
