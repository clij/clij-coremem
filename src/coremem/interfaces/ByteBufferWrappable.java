package coremem.interfaces;

import java.nio.ByteBuffer;

public interface ByteBufferWrappable<T>
{
	public ByteBuffer passNativePointerToByteBuffer();
}
