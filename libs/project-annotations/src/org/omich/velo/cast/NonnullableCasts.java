package org.omich.velo.cast;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.json.JSONArray;

/**
 * Методы избавляющие от написания @SuppressWarnings("null"),
 * в этом классе все методы действительно возвращают nonnull объекты,
 * независимо от действий пользователя библиотеки. 
 *
 */
@SuppressWarnings("null")//Используем тут только такие преобразования, которые не дают null.
final public class NonnullableCasts
{
	public static final @Nonnull String EMPTY_STRING = ""; //$NON-NLS-1$
	public static @Nonnull String getRandomUUIDString()
	{
		return UUID.randomUUID().toString();
	}

	//valueOf всегда возвращает какой-то объект не NULL
	public static @Nonnull Integer getNonnulInteger (int value)
	{
		return Integer.valueOf(value);
	}
	
	public static @Nonnull String getStringOrEmpty (@Nullable String value)
	{
		return value == null ? EMPTY_STRING : value;
	}
	
	// Документация не говорит о возможности возвращать null этим методом.
	//
	// Находим подтверждение в исходниках: в классе ByteArrayOutputStream видим следующий код:
	//
	// public synchronized byte[] toByteArray() {
    //    byte[] newArray = new byte[count];
    //    System.arraycopy(buf, 0, newArray, 0, count);
    //    return newArray;
    // }
	public static @Nonnull byte[] byteArrayOutputStreamToByteArray (@Nonnull ByteArrayOutputStream baos)
	{
		return baos.toByteArray();
	}
	
	public static @Nonnull String charSequenceToString (@Nonnull CharSequence seq)
	{
		return seq.toString();
	}
	
	public static @Nonnull ClassLoader classGetClassLoader (@Nonnull Class<?> classs)
	{
		return classs.getClassLoader();
	}

	public static @Nonnull String classGetCanonicalName (@Nonnull Class<?> classs)
	{
		return classs.getCanonicalName();
	}
	
	public static @Nonnull String classGetName (@Nonnull Class<?> classs)
	{
		return classs.getName();
	}
	
	public static @Nonnull String jsonArrayToString (@Nonnull JSONArray arr)
	{
		return arr.toString();
	}
	
	public static @Nonnull Object[] listToArray (@Nonnull List<?> list)
	{
		return list.toArray();
	}

	public static @Nonnull <T> T[] listToArray (@Nonnull List<?> list, T[] con)
	{
		return list.toArray(con);
	}

	//Long.toString никогда не даёт NULL.
	public static @Nonnull String longToString (long value)
	{
		return Long.toString(value);
	}

	public static @Nonnull Long longValueOf (long value)
	{
		return Long.valueOf(value);
	}

	//String.format не возвращает null. Он только падает, если аргументы были неправильными.
	public static @Nonnull String stringFormat (@Nonnull String template, Object... args)
	{
		return String.format(template, args);
	}
	
	//String.replaceAll не возвращает null.
	public static @Nonnull String stringReplaceAll (@Nonnull String string,
			@Nonnull String regexp, @Nonnull String replacement)
	{
		return string.replaceAll(regexp, replacement);
	}
	
	public static @Nonnull String[] stringSplit (@Nonnull String string,
			@Nonnull String regexp)
	{
		return string.split(regexp);
	}
	
	// Документация не говорит о возможности возвращать null этим методом.
	//
	// Находим подтверждение в исходниках: в классе StringBuilder видим следующий код:
	//
	//    403       public String toString() {
	//    404       // Create a copy, don't share the array
	//	  405           return new String(value, 0, count);
	//	  406       }
	//
	// http://www.docjar.com/html/api/java/lang/StringBuilder.java.html
	//
	public static @Nonnull String stringBuilderToString (@Nonnull StringBuilder sb)
	{
		return sb.toString();
	}

	//=========================================================================
	private NonnullableCasts ()
	{
		// Нельзя создавать экземпляр этого класса, это лишь набор
		// статичных методов.
	}
}
