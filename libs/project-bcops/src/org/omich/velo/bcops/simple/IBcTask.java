package org.omich.velo.bcops.simple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.IBcToaster;
import org.omich.velo.bcops.ICancelledInfo;
import org.omich.velo.handlers.IListenerInt;

import android.content.Context;
import android.os.Bundle;


/**
 * Интерфейс для однопоточной задачи, которая должна выполниться
 * в другом потоке или в сервисе.
 * 
 * В коде инициируется Intent для запуска, а экземпляр создаётся непосредственно
 * в среде выполнения, например в BcService.
 * 
 * Рекомендуется каждый наследник снабжать статичным
 * public static @Nonnull Intent createIntent (some params),
 * который генерирует нужный интент.
 * 
 * Пользователи создают Intent и передают его в IBcConnector.
 */
public interface IBcTask extends IBcBaseTask<IBcTask.BcTaskEnv>
{
	/**
	 * Окружение выполнения IBcTask 
	 */
	public static class BcTaskEnv
	{
		/**
		 * Параметры extras из переданного Intent.
		 */
		public @Nullable Bundle extras;
		/**
		 * Контекст.
		 */
		public @Nonnull Context context;
		/**
		 * Средство вывода текстовых сообщений, по умолчанию Toast, но теоретически может быть чем угодно.
		 */
		public @Nonnull IBcToaster bcToaster;
		/**
		 * Слушатель прогресса. Его может прослушивать тот, кто вызвал исполнение этого таска и что-нибудь там отображать.
		 * Например, прогресс.
		 */
		public @Nullable IListenerInt ph;
		/**
		 * Информер о том, что операция была отменена.
		 * Длинные операции могут прослушивать его и побырому сворачивать свою деятельность.
		 * Насильно вас никто прерывать не будет.
		 */
		public @Nullable ICancelledInfo ci;
		
		public BcTaskEnv(
				@Nullable Bundle extras, 
				@Nonnull Context context,
				@Nonnull  IBcToaster bcToaster,
				@Nullable IListenerInt ph, 
				@Nullable ICancelledInfo ci)
		{
			this.extras = extras;
			this.context = context;
			this.bcToaster = bcToaster;
			this.ph = ph;
			this.ci = ci;
		}
	}
}
