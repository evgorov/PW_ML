package org.omich.velo.log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.cast.NonnullableCasts;

public class Log 
{
	private static final String BOOL_TRUE = "true"; //$NON-NLS-1$
	private static final String BOOL_FALSE = "false"; //$NON-NLS-1$

	private static final String TPL_MET = "%1$s;\n%2$s %3$s"; //$NON-NLS-1$
	private static final String TPL_ET  = "%1$s;\n%2$s"; //$NON-NLS-1$
	private static final String TPL_MT  = "%1$s;\n%2$s"; //$NON-NLS-1$
	
	private static @Nonnull ILog msLog = new AndroidLog();
	private static @Nonnull ILogMsgCreator messageCreator = new StdLogMsgCreator();
	
	public static void init (@Nonnull ILog log)
	{
		msLog = log;
	}

	//==== public Log methods =================================================
	public static void wtf (@Nullable String msg, @Nullable Throwable er)
	{
		log(msg, er, lvl(Level.WTF));
	}

	public static void wtf (@Nullable String msg)
	{
		log(msg, lvl(Level.WTF));
	}
	
	public static void wtf (@Nonnull String tag, @Nullable String msg)
	{
		log(tag, msg, lvl(Level.WTF));
	}

	public static void e (@Nullable String msg)
	{
		log(msg, lvl(Level.E));
	}

	public static void e (@Nonnull String tag, @Nullable String msg)
	{
		log(tag, msg, lvl(Level.E));
	}
	
	public static void e (@Nullable String msg, @Nullable Throwable er)
	{
		log(msg, er, lvl(Level.E));
	}

	public static void e (@Nullable String msg, @Nullable ILoggable er)
	{
		log(msg, er, lvl(Level.E));
	}

	public static void e (@Nullable Throwable er)
	{
		log(er, lvl(Level.E));
	}

	public static void w (@Nullable String msg)
	{
		log(msg, lvl(Level.W));
	}

	public static void w (@Nonnull String tag, @Nullable String msg)
	{
		log(tag, msg, lvl(Level.W));
	}

	public static void w (@Nullable ILoggable er)
	{
		log(er, lvl(Level.W));
	}
	
	public static void w (@Nullable Throwable er)
	{
		log(er, lvl(Level.W));
	}

	public static void w (@Nullable String msg, @Nullable Throwable er)
	{
		log(msg, er, lvl(Level.W));
	}	

	public static void w (@Nullable String msg, @Nullable ILoggable er)
	{
		log(msg, er, lvl(Level.W));
	}

	public static void i (@Nullable String msg)
	{
		log(msg, lvl(Level.I));
	}

	public static void i (@Nonnull String tag, @Nullable String msg)
	{
		log(tag, msg, lvl(Level.I));
	}

	public static void i (@Nullable String msg, @Nullable Throwable er)
	{
		log(msg, er, lvl(Level.I));
	}	

	public static void i (@Nullable String msg, @Nullable ILoggable er)
	{
		log(msg, er, lvl(Level.I));
	}

	//==== Debug log methods ==================================================	
	@Deprecated
	public static void d (boolean bool)
	{
		d(bool ? BOOL_TRUE : BOOL_FALSE);
	}

	@Deprecated
	public static void d (long msg)
	{
		d(Long.toString(msg));
	}
	
	@Deprecated
	public static void d (double msg)
	{
		d(Double.toString(msg));
	}
	
	@Deprecated
	public static void d (@Nullable Throwable er)
	{
		log(er, lvl(Level.D));
	}
	
	@Deprecated
	public static void d (@Nullable String msg, @Nullable Throwable er)
	{
		log(msg, er, lvl(Level.D));
	}
	
	@Deprecated
	public static void d (@Nullable String msg)
	{
		log(msg, lvl(Level.D));
	}

//=============================================================================
	private static void log (@Nullable Throwable er,@Nonnull Level level)
	{
		String m = NonnullableCasts.stringFormat(TPL_ET,
				generateTraceMessage(),
				messageCreator.getMsg(er, level));
		msLog.log(m, level);
	}
	
	private static void log (@Nullable String msg, @Nonnull Level level)
	{
		String m = NonnullableCasts.stringFormat(TPL_MT,
				generateTraceMessage(),
				LogUtil.getNotNullMessage(msg));
		msLog.log(m, level);
	}

	private static void log (@Nonnull String tag, @Nullable String msg, @Nonnull Level level)
	{
		String m = NonnullableCasts.stringFormat(TPL_MT,
				generateTraceMessage(),
				LogUtil.getNotNullMessage(msg));
		msLog.log(tag, m, level);
	}

	private static void log (@Nullable String msg, @Nullable ILoggable er, @Nonnull Level level)
	{
		String m = NonnullableCasts.stringFormat(TPL_MET,
				generateTraceMessage(),
				LogUtil.getNotNullMessage(msg),
				messageCreator.getMsg(er, level));
		msLog.log(m, level);
	}

	private static void log (@Nullable ILoggable er, @Nonnull Level level)
	{
		String m = NonnullableCasts.stringFormat(TPL_ET,
				generateTraceMessage(),
				messageCreator.getMsg(er, level));
		msLog.log(m, level);
	}
	
	private static void log (@Nullable String msg, @Nullable Throwable er, @Nonnull Level level)
	{
		String m = NonnullableCasts.stringFormat(TPL_MET,
				generateTraceMessage(),
				LogUtil.getNotNullMessage(msg),
				messageCreator.getMsg(er, level));
		msLog.log(m, level);
	}
	
	@SuppressWarnings("null")
	private static @Nonnull Level lvl(Level level)
	{
		return level;
	}
	
	private static @Nonnull String generateTraceMessage()
	{
		return LogUtil.getShortTraceMessage(6, 10);
	}
}
