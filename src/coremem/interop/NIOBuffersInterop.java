package coremem.interop;

import java.lang.reflect.Field;
import java.nio.Buffer;

import coremem.exceptions.UnsupportedWrappingException;
import coremem.offheap.OffHeapMemory;
import coremem.util.Size;

public class NIOBuffersInterop
{
	public static OffHeapMemory getContiguousMemoryFrom(Buffer pBuffer)
	{
		if (!pBuffer.isDirect())
			throw new UnsupportedWrappingException("Cannot wrap a non-native NIO Buffer");

		final long lBufferAddress = getAddress(pBuffer);
		final long lSizeInBytes = Size.of(pBuffer);

		final OffHeapMemory lOffHeapMemory = new OffHeapMemory(	pBuffer,
																														lBufferAddress,
																														lSizeInBytes);
		return lOffHeapMemory;
	}

	private static long getAddress(Buffer buffer)
	{
		Field lField = null;
		try
		{
			lField = Buffer.class.getDeclaredField("address");
			lField.setAccessible(true);
			return lField.getLong(buffer);
		}
		catch (final Throwable e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (lField != null)
			{
				lField.setAccessible(false);
			}
		}
		return 0;
	}
}
