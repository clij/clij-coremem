package net.haesleinhuepf.clij.coremem.interop.test;

import static org.junit.Assert.assertEquals;

import com.sun.jna.Pointer;

import net.haesleinhuepf.clij.coremem.offheap.OffHeapMemory;
import org.junit.Test;

/**
 * NIO buffers interop tests
 *
 * @author royer
 */
public class JNABuffersInteropTests
{

  private static final int cBufferLength = 32;

  /**
   * Tests returning a JNA pointer
   */
  @Test
  public void testJNAPointer()
  {
    final net.haesleinhuepf.clij.coremem.offheap.OffHeapMemory lOffHeapMemory =
                                       OffHeapMemory.allocateBytes(cBufferLength);

    lOffHeapMemory.setLong(0, 1234);

    final Pointer lJNAPointer = lOffHeapMemory.getJNAPointer();

    assertEquals(1234L, lJNAPointer.getLong(0));
  }

}
