package org.omich.velo.bcops.client;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.handlers.IListener;

import android.content.Intent;
import android.os.Bundle;

/**
 * Для пользователя - это связующее звено между заказом задачи и исполнителем.
 * 
 *  Принимает Intent, заботится о запуске задачи. Передаёт события от него в
 *  обработчики пользователя.
 */
public interface IBcConnector
{
	public @Nonnull <T>String startTaskFull (@Nonnull Class<? extends BcBaseService<T>> serviceClass,
									@Nonnull Class<? extends IBcBaseTask<T>> taskClass,
									@Nonnull Intent intent,
									@Nullable IListener<Intent> handler);

	public @Nonnull <T>String startTask (@Nonnull Class<? extends BcBaseService<T>> serviceClass,
			@Nonnull Class<? extends IBcBaseTask<T>> taskClass,
			@Nonnull Intent intent,
			@Nonnull IListener<Bundle> finishHandler);

	
	public @Nonnull String startTypicalTask (@Nonnull Class<? extends IBcTask> taskClass,
									@Nonnull Intent intent,
									@Nonnull IListener<Bundle> finishHandler);
	/**
	 * Попросить отменить выполнение задачи. Задача может реагировать на эту
	 * информацию и аккуратно завершаться. После завершения обработчикам придёт
	 * событие, как если бы задача не отменялась.
	 * 
	 * @param opId
	 */
	public void cancelTask (@Nonnull String opId);
	
	/**
	 * Отписаться от задачи, но не отменять её выполнение.
	 * Метод гарантирует, что отписывание произойдёт тот час же,
	 * Поэтому после вызова уже не нужно ожидать ответа в обработчиках.
	 * 
	 * @param opId
	 */
	public void unsubscribeTask (@Nonnull String opId);
}
