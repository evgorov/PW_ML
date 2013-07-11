package org.omich.velo.activity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;

public class ForResultFragmentActivity extends FragmentActivity
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
