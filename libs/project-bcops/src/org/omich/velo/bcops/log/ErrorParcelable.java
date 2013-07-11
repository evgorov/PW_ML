package org.omich.velo.bcops.log;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.constants.Strings;
import org.omich.velo.log.ILoggable;
import org.omich.velo.log.LogUtil;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Класс для передачи ошибок через Bundle.
 * Сами Throwable они не Parcelable, поэтому мы забираем оттуда интересную информацию
 * и передаём через Bundle.
 * 
 * Их можно легко записывать в наш лог, прям как настоящие Throwable.
 */
public class ErrorParcelable implements Parcelable, ILoggable
{
	public static class TraceElem
	{
		private static final String FMT = "%s (%s:%d)"; //$NON-NLS-1$

		public final @Nonnull String className;
		public final @Nonnull String fileName;
		public final int lineNumber;
		public final @Nonnull String methodName;

		public TraceElem(String className, String fileName, int lineNumber,
				String methodName)
		{
			super();
			this.className = getNonnullString(className);
			this.fileName = getNonnullString(fileName);
			this.lineNumber = lineNumber;
			this.methodName = getNonnullString(methodName);
		}

		@Override
		public String toString ()
		{
			return String.format(Locale.US, FMT, methodName, className, Integer.valueOf(lineNumber));
		}
	}
	
	public static final Parcelable.Creator<ErrorParcelable> CREATOR
			= new Parcelable.Creator<ErrorParcelable>()
			{
				public ErrorParcelable createFromParcel(Parcel source)
				{
					return new ErrorParcelable(source);
				}

				public ErrorParcelable[] newArray(int size)
				{
					return new ErrorParcelable[size];
				}
			};

	//========================================================================
	public final @Nonnull String className;
	public final @Nonnull String simpleName;
	public final @Nonnull String message;
	public final @Nonnull TraceElem[] trace;

	public ErrorParcelable (@Nonnull Throwable error)
	{
		className = getNonnullString(error.getClass().getCanonicalName());
		simpleName = getNonnullString(error.getClass().getSimpleName());
		message = getNonnullString(error.getMessage());
		
		StackTraceElement[] tr = error.getStackTrace();
		trace = new TraceElem[tr.length];

		for(int i = 0; i < tr.length; ++i)
		{
			StackTraceElement el = tr[i];
			trace[i] = new TraceElem(
					el.getClassName(),
					el.getFileName(),
					el.getLineNumber(),
					el.getMethodName());
		}
	}
	
	public ErrorParcelable (Parcel source)
	{
		className = getNonnullString(source.readString());
		simpleName = getNonnullString(source.readString());
		message = getNonnullString(source.readString());

		trace = new TraceElem[source.readInt()];

		for(int i = 0; i < trace.length; ++i)
		{
			String traceElemClassName = source.readString();
			String fileName = source.readString();
			int lineNumber = source.readInt();
			String methodName = source.readString();

			trace[i] = new TraceElem(traceElemClassName,
					fileName, lineNumber, methodName);
		}		
	}

	//==== Parcelable =========================================================
	public int describeContents (){return 0;}

	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(className);
		dest.writeString(simpleName);
		dest.writeString(message);
		dest.writeInt(trace.length);
		for(int i = 0; i < trace.length; ++i)
		{
			TraceElem te = trace[i];
			dest.writeString(te.className);
			dest.writeString(te.fileName);
			dest.writeInt(te.lineNumber);
			dest.writeString(te.methodName);
		}
	}

	//==== ILoggable =========================================================
	public @Nonnull String getShortLogMessage()
	{
		return simpleName + ": " + LogUtil.getNotNullMessage(message); //$NON-NLS-1$
	}

	public @Nonnull String getFullLogMessage()
	{               
		return simpleName + ": "  //$NON-NLS-1$
			+ LogUtil.getNotNullMessage(message) + "\n"  //$NON-NLS-1$
			+ getStackTraceMsg();
	}

	//========================================================================
	private @Nonnull String getStackTraceMsg ()
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < 10 && i < trace.length; ++i)
		{
			sb.append(trace[i].toString());
			sb.append(trace);
		}

		@SuppressWarnings("null")
		@Nonnull String result = sb.toString();
		return result;
	}
	
	private static @Nonnull String getNonnullString(@Nullable String string)
	{
		return string != null ? string : Strings.EMPTY;
	}
}
