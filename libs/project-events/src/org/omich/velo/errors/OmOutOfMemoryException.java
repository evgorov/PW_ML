package org.omich.velo.errors;

/**
 * OutOfMemoryError является наследником от RutimeException,
 * поэтому теоретически может вылезти везде.
 *
 * В дорогих по памяти местах, где эта ошибка вероятна (например, работа с изображениями),
 * мы ловим это исключение и кидаем своё, которое уже нужно объявлять
 * в методах во throws-поле, и всем явно видно, что тут может упасть.
 *
 */
@Deprecated
public class OmOutOfMemoryException extends Exception
{
        private static final long serialVersionUID = 1L;

        public OmOutOfMemoryException (String message)
        {
                super(message);
        }

        public OmOutOfMemoryException (String message, Throwable cause)
        {
                super(message, cause);
        }
}