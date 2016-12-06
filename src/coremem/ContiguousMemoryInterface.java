package coremem;

import coremem.interfaces.BridJPointerWrappable;
import coremem.interfaces.ByteBufferWrappable;
import coremem.interfaces.CopyFromToJavaArray;
import coremem.interfaces.CopyFromToNIOBuffers;
import coremem.interfaces.CopyRangeFromToJavaArray;
import coremem.interfaces.Copyable;
import coremem.interfaces.PointerAccessible;
import coremem.interfaces.ReadAt;
import coremem.interfaces.ReadAtAligned;
import coremem.interfaces.ReadWriteBytesFileChannel;
import coremem.interfaces.SizedInBytes;
import coremem.interfaces.WriteAt;
import coremem.interfaces.WriteAtAligned;
import coremem.rgc.Freeable;

/**
 * ContiguousMemoryInterface represents contiguous chunks of memory that can be
 * accessed, copied, written and read from disk, and can be exchanged with NIO,
 * BridJ.
 * 
 * @author royer
 */
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
																					CopyRangeFromToJavaArray,
																					ReadWriteBytesFileChannel,
																					SizedInBytes,
																					Freeable
{

	/**
	 * Returns a ContiguousMemoryInterface for a sub region.
	 * 
	 * @param pOffsetInBytes
	 *          offset in bytes
	 * @param pLenghInBytes
	 *          length in bytes
	 * @return
	 */
	ContiguousMemoryInterface subRegion(long pOffsetInBytes,
																			long pLenghInBytes);

}
