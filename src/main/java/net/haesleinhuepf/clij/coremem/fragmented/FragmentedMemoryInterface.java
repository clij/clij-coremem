package net.haesleinhuepf.clij.coremem.fragmented;

import java.nio.Buffer;

import net.haesleinhuepf.clij.coremem.ContiguousMemoryInterface;
import net.haesleinhuepf.clij.coremem.interfaces.ReadWriteBytesFileChannel;
import net.haesleinhuepf.clij.coremem.interfaces.SizedInBytes;
import net.haesleinhuepf.clij.coremem.offheap.OffHeapMemory;
import net.haesleinhuepf.clij.coremem.rgc.Freeable;

/**
 * Fragmented memory objects are lists of contiguous memory regions. Overall,
 * the referenced memory is not necessarily (but can be) contiguous.
 *
 * @author royer
 */
public interface FragmentedMemoryInterface extends
                                           Iterable<ContiguousMemoryInterface>,
        ReadWriteBytesFileChannel,
        SizedInBytes,
        Freeable
{

  /**
   * Returns the number of fragments.
   * 
   * @return number of fragments
   */
  int getNumberOfFragments();

  /**
   * Returns the contiguous memory at a given index.
   * 
   * @param pFragmentIndex
   *          index
   * @return contiguous memory
   */
  ContiguousMemoryInterface get(int pFragmentIndex);

  /**
   * Adds a contiguous memory fragment to this fragmented memory
   * 
   * @param pContiguousMemory
   *          contiguous fragment
   */
  void add(ContiguousMemoryInterface pContiguousMemory);

  /**
   * Removes a counties fragment from this fragmented memory.
   * 
   * @param pContiguousMemory ?
   */
  void remove(ContiguousMemoryInterface pContiguousMemory);

  /**
   * Adds a NIO buffer to this fragmented memory.
   * 
   * @param pNIOBuffer
   *          NIO buffer
   * @return the actual contiguous memory used internally.
   */
  net.haesleinhuepf.clij.coremem.offheap.OffHeapMemory add(Buffer pNIOBuffer);

  /**
   * Returns a consolidated contiguous copy of this fragmented memory - this is
   * done by simply concatenating the contiguous memory regions together in the
   * list order.
   * 
   * @return consolidated memory
   */
  OffHeapMemory makeConsolidatedCopy();

  /**
   * Consolidates (copies) the contents of this fragmented memory into a given
   * contiguous buffer.
   * 
   * @param pDestinationMemory
   *          destination memory
   */
  void makeConsolidatedCopy(ContiguousMemoryInterface pDestinationMemory);

}
