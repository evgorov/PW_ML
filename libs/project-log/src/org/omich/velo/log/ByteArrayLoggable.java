package org.omich.velo.log;

import javax.annotation.Nonnull;

import org.omich.velo.cast.NonnullableCasts;

public class ByteArrayLoggable implements ILoggable
{
	private final @Nonnull byte [] mArr;
	private final boolean mIsSigned;
	
	public ByteArrayLoggable (@Nonnull byte [] arr)
	{
		this(arr, false);
	}

	public ByteArrayLoggable (@Nonnull byte [] arr, boolean isSigned)
	{
		mArr = arr;
		mIsSigned = isSigned;
	}




	public @Nonnull String getShortLogMessage ()
	{
		StringBuilder sb = new StringBuilder("{"); //$NON-NLS-1$
		byte [] arr = mArr;
		for(int i = 0; i < arr.length; ++i)
		{
			sb.append( (arr[i] >= 0 || mIsSigned) 
					? Byte.toString(arr[i]) 
					: Integer.toString(256 + arr[i]));
			if(i < arr.length -1)
			{
				sb.append(", "); //$NON-NLS-1$
			}
		}
		sb.append("}"); //$NON-NLS-1$

		return NonnullableCasts.stringBuilderToString(sb);
	}

	public @Nonnull String getFullLogMessage ()
	{
		return getShortLogMessage();
	}	
}
