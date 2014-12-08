package coremem.interfaces;

import java.nio.ByteBuffer;

public interface ByteBufferWrappable
{
	public ByteBuffer passNativePointerToByteBuffer(Class<?> pTargetClass);
}
