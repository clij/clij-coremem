package net.haesleinhuepf.clij.coremem.interop.test;

import static org.junit.Assert.assertEquals;

import net.haesleinhuepf.clij.coremem.offheap.OffHeapMemory;
import org.junit.Test;

/**
 * BridJ interop tests
 *
 * @author royer
 */
public class BridJInteropTests
{

  private static final int cBufferLength = 32;

  /**
   * Tests returning a JNA pointer
   */
  @Test
  public void testBridJPointer()
  {
    final net.haesleinhuepf.clij.coremem.offheap.OffHeapMemory lOffHeapMemory =
                                       OffHeapMemory.allocateBytes(cBufferLength);

    lOffHeapMemory.setLong(0, 1234);

    final org.bridj.Pointer lBridJPointer =
                                          lOffHeapMemory.getBridJPointer(Long.class);

    assertEquals(1234L, lBridJPointer.getLong());
  }

}
