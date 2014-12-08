package coremem;

import coremem.interfaces.BridJPointerWrappable;
import coremem.interfaces.ByteBufferWrappable;
import coremem.interfaces.PointerAccessible;
import coremem.interfaces.ReadAt;
import coremem.interfaces.ReadAtAligned;
import coremem.interfaces.ReadWriteBytesFileChannel;
import coremem.interfaces.SizedInBytes;
import coremem.interfaces.WriteAt;
import coremem.interfaces.WriteAtAligned;
import coremem.rgc.Freeable;

public interface MemoryRegionInterface<T> extends
																					PointerAccessible,
																					BridJPointerWrappable<T>,
																					ByteBufferWrappable<T>,
																					ReadAtAligned,
																					WriteAtAligned,
																					ReadAt,
																					WriteAt,
																					ReadWriteBytesFileChannel,
																					SizedInBytes,
																					Freeable
{

	MemoryRegionInterface<T> subRegion(long pOffset, long pLenghInBytes);

}
