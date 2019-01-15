package net.haesleinhuepf.clij.coremem;

import net.haesleinhuepf.clij.coremem.interfaces.*;
import net.haesleinhuepf.clij.coremem.interfaces.SizedInBytes;
import net.haesleinhuepf.clij.coremem.rgc.Freeable;

/**
 * ContiguousMemoryInterface represents contiguous chunks of memory that can be
 * accessed, copied, written and read from disk, and can be exchanged with NIO,
 * BridJ.
 * 
 * @author royer
 */
public interface ContiguousMemoryInterface extends
        net.haesleinhuepf.clij.coremem.interfaces.PointerAccessible,
        net.haesleinhuepf.clij.coremem.interfaces.JNAPointerWrappable,
        net.haesleinhuepf.clij.coremem.interfaces.BridJPointerWrappable,
        net.haesleinhuepf.clij.coremem.interfaces.ByteBufferWrappable,
        net.haesleinhuepf.clij.coremem.interfaces.ReadAtAligned,
        net.haesleinhuepf.clij.coremem.interfaces.WriteAtAligned,
        net.haesleinhuepf.clij.coremem.interfaces.ReadAt,
        net.haesleinhuepf.clij.coremem.interfaces.WriteAt,
        net.haesleinhuepf.clij.coremem.interfaces.Copyable<ContiguousMemoryInterface>,
        net.haesleinhuepf.clij.coremem.interfaces.CopyFromToNIOBuffers,
        net.haesleinhuepf.clij.coremem.interfaces.CopyFromToJavaArray,
        net.haesleinhuepf.clij.coremem.interfaces.CopyRangeFromToJavaArray,
        net.haesleinhuepf.clij.coremem.interfaces.ReadWriteBytesFileChannel,
        SizedInBytes,
        Freeable
{

  /**
   * Returns a contiguous memory object representing for a memory sub region.
   * 
   * @param pOffsetInBytes
   *          offset in bytes
   * @param pLenghInBytes
   *          length in bytes
   * @return contiguous memory for sub region
   */
  ContiguousMemoryInterface subRegion(long pOffsetInBytes,
                                      long pLenghInBytes);

}
