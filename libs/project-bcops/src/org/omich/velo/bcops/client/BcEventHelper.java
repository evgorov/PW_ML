package org.omich.velo.bcops.client;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerInt;
import org.omich.velo.handlers.IListenerVoid;

import android.content.Intent;
import android.os.Bundle;

import static org.omich.velo.bcops.BcBaseService.*;

/**
 * Методы, упрощающие пользвателям обрабатывать событие, пришедшее от IBcConnector
 */
public class BcEventHelper
{
	/**
	 * Парсит общее событие и в зависимости от результата вызывает нужный обработчик.
	 * 
	 * @param intent
	 * @param start
	 * @param progress
	 * @param finish
	 * @param cancel
	 * @param other
	 */
	public static void parseEvent (@Nonnull Intent intent, 
			@Nullable IListenerVoid start,
			@Nullable IListenerInt progress,
			@Nullable IListener<Bundle> finish,
			@Nullable IListenerVoid cancel,
			@Nullable IListener<Intent> other)
	{
		Bundle extras = intent.getExtras();
		
		String event = extras.getString(BF_EVENT);
		
		if(EVT_START.equals(event))
		{
			if(start != null)
			{
				start.handle();
			}
		}
		else if(EVT_PROGRESS.equals(event))
		{
			if(progress != null)
			{
				progress.handle(extras.getInt(BF_PROGRESS_DATA));
			}
		}
		else if(EVT_FINISH.equals(event))
		{
			if(finish != null)
			{
				finish.handle(extras.getBundle(BF_RESULT));
			}
		}
		else if(EVT_CANCEL.equals(event))
		{
			if(cancel != null)
			{
				cancel.handle();
			}
		}
		else
		{
			if(other != null)
			{
				other.handle(intent);
			}
		}
	}
}
