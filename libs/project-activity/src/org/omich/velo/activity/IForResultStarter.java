package org.omich.velo.activity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.handlers.IListener;

import android.content.Intent;

public interface IForResultStarter
{
	/**
	 * Делает context.startActivityForResult, а полученный впоследствии ответ
	 * передаёт в обработчик handler.
	 * 
	 * Надо понимать, что resCode никак не обрабатывается.
	 * 
	 * @param intent
	 * @param handler
	 */
	void startForResult(@Nonnull Intent intent, @Nullable IListener<Intent> handler);
}
