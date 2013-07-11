package org.omich.velo.bcops;

import javax.annotation.Nonnull;

/**
 * Принимает заказы на toast сообщения из любого потока.
 */
public interface IBcToaster
{
	/**
	 * Пустая реализация, которая не показывает никаких сообщений.
	 */
	public static final @Nonnull IBcToaster EMPTY_TOASTER = new IBcToaster()
	{	
		public void showToast(@Nonnull String msg)
		{
			//Do nothing. It's an empty toaster;
		}
	};

	void showToast (@Nonnull String msg);
}
