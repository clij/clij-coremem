package coremem.interfaces;

public interface PointerAccessible extends
																	MemoryTyped,
																	ByteBufferWrappable,
																	BridJPointerWrappable
{
	long getAddress();

	long getSizeInBytes();
}
