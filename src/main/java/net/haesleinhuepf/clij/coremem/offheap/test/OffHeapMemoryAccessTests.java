package net.haesleinhuepf.clij.coremem.offheap.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import net.haesleinhuepf.clij.coremem.exceptions.InvalidNativeMemoryAccessException;
import net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess;
import org.junit.Test;

/**
 * Off heap memory access tests
 *
 * @author royer
 */
public class OffHeapMemoryAccessTests
{
  final private static long cBufferSize =
                                        2 * (long) Integer.MAX_VALUE;

  /**
   * tests max allocation
   */
  @Test
  public void testMaxAllocation()
  {
    try
    {
      int i = 0;
      try
      {
        net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.freeAll();
        net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setMaximumAllocatableMemory(1000L
                                                        * 1000L);
        for (; i < 2000; i++)
        {
          net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.allocateMemory(1000);
        }
        fail();
      }
      catch (final OutOfMemoryError e)
      {
        // System.out.println("i=" + i);
        assertTrue(i >= 1000);
      }
      catch (final Throwable lE)
      {
        fail();
      }

      net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.freeAll();
      assertEquals(0, net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getTotalAllocatedMemory());
    }
    catch (final Throwable e)
    {
      e.printStackTrace();
      fail();
    }
    finally
    {
      net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setMaximumAllocatableMemory(Long.MAX_VALUE);
    }
  }

  /**
   * Tests reallocate and free
   */
  @Test
  public void testAllocateReallocateFree()
  {
    try
    {
      // System.out.println(cBufferSize);
      final long lAddress =
                          net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.allocateMemory(cBufferSize);

      net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setByte(lAddress, (byte) 123);
      assertEquals(net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getByte(lAddress), (byte) 123);

      final long lAddressReallocated =
                                     net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.reallocateMemory(lAddress,
                                                                          10);

      net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setByte(lAddressReallocated + 9,
                                  (byte) 123);
      assertEquals(net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getByte(lAddressReallocated
                                               + 9),
                   (byte) 123);

      net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.freeMemory(lAddressReallocated);
    }
    catch (final net.haesleinhuepf.clij.coremem.exceptions.InvalidNativeMemoryAccessException e)
    {
      e.printStackTrace();
      fail();
    }

  }

  /**
   * Tests super big allocation
   */
  @Test
  public void testSuperBig()
  {
    // System.out.println("begin");

    try
    {
      final long lLength = 1L * 1000L * 1000L * 1000L;

      // System.out.println("allocateMemory");
      final long lAddress =
                          net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.allocateMemory(lLength);
      // System.out.println("lAddress=" + lAddress);
      assertFalse(lAddress == 0);

      // System.out.println("setMemory");
      net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.fillMemory(lAddress, lLength, (byte) 0);

      // System.out.println("setByte(s)");
      for (long i = 0; i < lLength; i += 1000L * 1000L)
      {
        net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setByte(lAddress + i, (byte) i);
      }

      // System.out.println("getByte(s)");
      for (long i = 0; i < lLength; i += 1000L * 1000L)
      {
        final byte lValue = net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getByte(lAddress + i);
        assertEquals((byte) i, lValue);
      }

      // Thread.sleep(10000);

      // System.out.println("freeMemory");
      OffHeapMemoryAccess.freeMemory(lAddress);

      // System.out.println("end");
    }
    catch (final InvalidNativeMemoryAccessException e)
    {
      e.printStackTrace();
      fail();
    }
  }

}
