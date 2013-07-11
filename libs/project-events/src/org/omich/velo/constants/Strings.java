package org.omich.velo.constants;

import javax.annotation.Nonnull;

/**
 * Строковые константы на все случаи жизни.
 */
@SuppressWarnings("nls")
final public class Strings
{
	public static final @Nonnull String BOOL_FALSE   = "false"; //$NON-NLS-1$
	public static final @Nonnull String BOOL_TRUE    = "true"; //$NON-NLS-1$

	// Как мне стало недавно понятно, сами по себе такие константы смысла не имеют,
	// только ухудшается чтение, а выгоды для производительности или чего-то ещё нету.
	public static final @Deprecated @Nonnull String CH_BRACE_L   = "{"; //$NON-NLS-1$
	public static final @Deprecated @Nonnull String CH_BRACE_R   = "}"; //$NON-NLS-1$
	public static final @Deprecated @Nonnull String CH_BRACKET_L = "["; //$NON-NLS-1$
	public static final @Deprecated @Nonnull String CH_BRACKET_R = "]";	 //$NON-NLS-1$
	public static final @Deprecated @Nonnull String CH_COLON        = ":"; //$NON-NLS-1$
	public static final @Deprecated @Nonnull String CH_ENTER     = "\n"; //$NON-NLS-1$
	public static final @Deprecated @Nonnull String CH_SPACE     = " ";

	// Константа пустой строки.
	public static final @Nonnull String EMPTY        = ""; //$NON-NLS-1$

	// Константы формата чисел.
	public static final @Nonnull String FORMAT_0     = "%d"; //$NON-NLS-1$
	public static final @Nonnull String FORMAT_00    = "%02d"; //$NON-NLS-1$
}
