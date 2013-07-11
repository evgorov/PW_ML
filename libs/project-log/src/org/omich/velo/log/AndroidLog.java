package org.omich.velo.log;

import javax.annotation.Nonnull;

import org.omich.velo.cast.NonnullableCasts;

/**
 * Класс перенаправляет сообщения лога в стандартный Андроидный лог.
 */
public class AndroidLog implements ILog
{
	//==== fields and constructors ===========================================
	private final @Nonnull String mLogId;
	
	public AndroidLog ()
	{
		this(NonnullableCasts.getStringOrEmpty(AndroidLog.class.getPackage().getName()));
	}
	
	public AndroidLog (@Nonnull String logId)
	{
		mLogId = logId;
	}
	
	
	//==== public interface ===================================================
	public void log (@Nonnull String msg, @Nonnull Level level)
	{
		log(mLogId, msg, level);
	}

	public void log (@Nonnull String tag, @Nonnull String msg, @Nonnull Level level)
	{
		switch(level)
		{
		case WTF:
			android.util.Log.wtf(tag, msg);
			break;
		case E:
			android.util.Log.e(tag, msg);
			break;
		case W:
			android.util.Log.w(tag, msg);
			break;
		case I:
			android.util.Log.i(tag, msg);
			break;
		case D:
			android.util.Log.d(tag, msg);
			break;
		}
	}
}
