package coremem;

import coremem.interfaces.BridJPointerWrappable;
import coremem.interfaces.ByteBufferWrappable;
import coremem.interfaces.CopyFromToJavaArray;
import coremem.interfaces.CopyFromToNIOBuffers;
import coremem.interfaces.Copyable;
import coremem.interfaces.PointerAccessible;
import coremem.interfaces.ReadAt;
import coremem.interfaces.ReadAtAligned;
import coremem.interfaces.ReadWriteBytesFileChannel;
import coremem.interfaces.SizedInBytes;
import coremem.interfaces.WriteAt;
import coremem.interfaces.WriteAtAligned;
import coremem.rgc.Freeable;

public interface ContiguousMemoryInterface extends
																					PointerAccessible,
																					BridJPointerWrappable,
																					ByteBufferWrappable,
																					ReadAtAligned,
																					WriteAtAligned,
																					ReadAt,
																					WriteAt,
																					Copyable<ContiguousMemoryInterface>,
																					CopyFromToNIOBuffers,
																					CopyFromToJavaArray,
																					ReadWriteBytesFileChannel,
																					SizedInBytes,
																					Freeable
{

	ContiguousMemoryInterface subRegion(long pOffset, long pLenghInBytes);

}
