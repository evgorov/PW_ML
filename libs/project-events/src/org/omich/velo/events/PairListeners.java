package org.omich.velo.events;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final public class PairListeners
{
	public static interface IListenerIntBoolean
	{
		void handle(int paramI, boolean paramB);
	}
	
	public static interface INistenerIntObject<Param>
	{
		void handle(int paramI, @Nonnull Param paramO);
	}
	
	public static interface INistenerBooleanObject<Param>
	{
		void handle(boolean paramB, @Nonnull Param paramO);
	}
	
	public static interface IListenerBooleanObject<Param>
	{
		void handle(boolean paramI, @Nullable Param paramO);
	}
	
	public static interface INistenerOO<Param1, Param2>
	{
		void handle(@Nonnull Param1 p1, @Nonnull Param2 p2);
	}
}
