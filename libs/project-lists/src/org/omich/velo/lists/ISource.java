package org.omich.velo.lists;

import javax.annotation.Nonnull;

public interface ISource
{
	@Nonnull Object getItem (int position);
	int getLenght ();
	long getItemId (int position);
}
