package org.omich.velo.activity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import android.app.Activity;
import android.content.Intent;

/**
 *  Активити со встроенноым ForResultStarter.
 * 
 *  Хороший родитель для Активити без иерархии.
 *  
 *  Пример того, как надо правильно встраивать ForResultStarter в какое-либо активити.
 *
 */
abstract public class ForResultActivity extends Activity
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
