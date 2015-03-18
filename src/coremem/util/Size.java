package coremem.util;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import coremem.types.NativeTypeEnum;

public class Size
{

	public static int DOUBLE = 8;
	public static int FLOAT = 4;
	public static int LONG = 8;
	public static int INT = 4;
	public static int SHORT = 2;
	public static int CHAR = 2;
	public static int BYTE = 1;

	public static int DOUBLESHIFT = 3;
	public static int FLOATSHIFT = 2;
	public static int LONGSHIFT = 3;
	public static int INTSHIFT = 2;
	public static int SHORTSHIFT = 1;
	public static int CHARSHIFT = 1;
	public static int BYTESHIFT = 0;

	public static int of(final Class<?> pClass)
	{
		if (pClass == Character.class || pClass == char.class
				|| pClass == Character.TYPE)
			return CHAR;
		else if (pClass == Byte.class || pClass == byte.class
							|| pClass == Byte.TYPE)
			return BYTE;
		else if (pClass == Short.class || pClass == short.class
							|| pClass == Short.TYPE)
			return SHORT;
		else if (pClass == Integer.class || pClass == int.class
							|| pClass == Integer.TYPE)
			return INT;
		else if (pClass == Long.class || pClass == long.class
							|| pClass == Long.TYPE)
			return LONG;
		else if (pClass == Float.class || pClass == float.class
							|| pClass == Float.TYPE)
			return FLOAT;
		else if (pClass == Double.class || pClass == double.class
							|| pClass == Double.TYPE)
			return DOUBLE;
		else
			throw new RuntimeException("Invalid Class!");

	}

	public static long of(final Buffer pBuffer)
	{
		final int lCapacity = pBuffer.capacity();
		if (pBuffer instanceof ByteBuffer)
			return of(byte.class) * lCapacity;

		else if (pBuffer instanceof CharBuffer)
			return of(char.class) * lCapacity;

		else if (pBuffer instanceof ShortBuffer)
			return of(short.class) * lCapacity;

		else if (pBuffer instanceof IntBuffer)
			return of(int.class) * lCapacity;

		else if (pBuffer instanceof LongBuffer)
			return of(long.class) * lCapacity;

		else if (pBuffer instanceof FloatBuffer)
			return of(float.class) * lCapacity;

		else if (pBuffer instanceof DoubleBuffer)
			return of(double.class) * lCapacity;

		else
			throw new RuntimeException("Invalid Class!");

	}

	public static int of(NativeTypeEnum pNativeType)
	{
		if (pNativeType == NativeTypeEnum.Byte)
			return of(byte.class);

		else if (pNativeType == NativeTypeEnum.UnsignedByte)
			return of(byte.class);

		else if (pNativeType == NativeTypeEnum.Short)
			return of(short.class);

		else if (pNativeType == NativeTypeEnum.UnsignedShort)
			return of(short.class);

		else if (pNativeType == NativeTypeEnum.Int)
			return of(int.class);

		else if (pNativeType == NativeTypeEnum.UnsignedInt)
			return of(int.class);

		else if (pNativeType == NativeTypeEnum.Long)
			return of(long.class);

		else if (pNativeType == NativeTypeEnum.UnsignedLong)
			return of(long.class);

		else if (pNativeType == NativeTypeEnum.HalfFloat)
			return of(float.class) / 2;

		else if (pNativeType == NativeTypeEnum.Float)
			return of(float.class);

		else if (pNativeType == NativeTypeEnum.Double)
			return of(double.class);

		else
			throw new RuntimeException("Invalid Class!");
	}

	public static Class<?> integralTypeFromSize(final int pNumberOfBytes,
																							final boolean pSigned)
	{
		switch (pNumberOfBytes)
		{
		case 1:
			return byte.class;
		case 2:
			if (pSigned)
				return short.class;
			else
				return char.class;
		case 4:
			return int.class;
		case 8:
			return long.class;
		}
		throw new RuntimeException("Invalid number of bytes!");
	}

	public static Class<?> floatTypeFromSize(final int pNumberOfBytes)
	{
		switch (pNumberOfBytes)
		{
		case 4:
			return float.class;
		case 8:
			return double.class;
		}
		throw new RuntimeException("Invalid number of bytes!");
	}

}
