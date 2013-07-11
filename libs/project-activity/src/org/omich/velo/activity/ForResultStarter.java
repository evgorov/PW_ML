package org.omich.velo.activity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.handlers.ConstantListeners;
import org.omich.velo.handlers.IListener;

import android.app.Activity;
import android.content.Intent;

/**
 * Предназначен для лёгкого встраивания в Активити на любом уровне иерархии.
 * 
 * Позволяет просто запускать startActivityForResult 
 * и принимает обработчик IListener<Intent>, который вызывается после получения 
 * ответа.
 * 
 * Пример использования: ForResultActivity.
 * 
 * 
 * 
 * Принцип работы:
 * 
 * Регистрирует обработчик IListener, присваивает ему числовой listenerId.
 * Этот listenerId передаётся в вызов startActivityForResult в качестве recCode.
 * 
 * Когда приходит ответ, одним из параметров метода onActivityResult 
 * является recCode - тот самый listenerId. Находим нужный обработчик
 * и передаём ответ ему.
 */
public class ForResultStarter implements IForResultStarter
{
	private @Nonnull Activity mContextActivity;
	private @Nonnull List<IListener<Intent>> mHandlers = new ArrayList<IListener<Intent>>();	
	@SuppressWarnings("unchecked")//Мы точно знаем, что с EMPTY_LISTENER заработает любой параметр, т.к. он ничего с ними не делает
	private @Nonnull IListener<Intent> mEmptyHandler = ConstantListeners.EMPTY_LISTENER;
	
	public ForResultStarter (@Nonnull Activity contextActivity)
	{
		mContextActivity = contextActivity;
	}

	/**
	 * Обрабатывает activityResult одним из обработчиков.
	 * 
	 * @param reqCode
	 * @param data
	 * @return Возвращает true, если подходящий обработчик был найден, false - если нет
	 */
	public boolean onActivityResult (int reqCode, @Nullable Intent data)
	{
		//
		// Когда приходит ответ, одним из параметров метода onActivityResult 
		// является recCode - тот самый listenerId. Находим нужный обработчик
		// и передаём ответ ему.
		//

		if(reqCode >= 0 && reqCode < mHandlers.size() && mHandlers.get(reqCode) != null)
		{
			mHandlers.get(reqCode).handle(data);
			mHandlers.set(reqCode, null);
			return true;
		}
		return false;
	}
	
	//==== IForResultStarter ==================================================
	public void startForResult(@Nonnull Intent intent, @Nullable IListener<Intent> hand)
	{
		//
		// Регистрирует обработчик IListener, присваивает ему числовой listenerId.
		// Этот listenerId передаётся в вызов startActivityForResult в качестве recCode.
		//
		
		@Nonnull IListener<Intent> handler = (hand != null ? hand : mEmptyHandler);

		int reqCode = -1;

		int size = mHandlers.size();
		for(int i = 0; i < size; ++i)
		{
			if(mHandlers.get(i) == null)
			{
				mHandlers.set(i, handler);
				reqCode = i;
				break;
			}
		}
		if(reqCode == -1)
		{
			reqCode = size;
			mHandlers.add(handler);
		}

		mContextActivity.startActivityForResult(intent, reqCode);
	}
}
