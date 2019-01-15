package net.haesleinhuepf.clij.coremem.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import net.haesleinhuepf.clij.coremem.ContiguousMemoryInterface;
import net.haesleinhuepf.clij.coremem.enums.MemoryType;
import net.haesleinhuepf.clij.coremem.interfaces.Copyable;
import net.haesleinhuepf.clij.coremem.interfaces.MappableMemory;
import net.haesleinhuepf.clij.coremem.interfaces.RangeCopyable;
import net.haesleinhuepf.clij.coremem.interfaces.Resizable;
import net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess;

/**
 * Helper class for contiguous memory tests
 *
 * @author royer
 */
public class ContiguousMemoryTestsHelper
{

  /**
   * Tests basic functionality of a contiguous memory interface
   * 
   * @param pContiguousMemoryInterface
   *          contiguous memory
   * @param pMemoryType
   *          memory type
   * @param pResize
   *          true -> attempt resize
   */
  public static void testBasics(net.haesleinhuepf.clij.coremem.ContiguousMemoryInterface pContiguousMemoryInterface,
                                MemoryType pMemoryType,
                                final boolean pResize)
  {
    final long lLength = pContiguousMemoryInterface.getSizeInBytes();

    if (pContiguousMemoryInterface instanceof net.haesleinhuepf.clij.coremem.interfaces.MappableMemory)
      ((net.haesleinhuepf.clij.coremem.interfaces.MappableMemory) pContiguousMemoryInterface).map();

    assertTrue(pContiguousMemoryInterface.getAddress() != 0L);

    assertTrue(pContiguousMemoryInterface.getMemoryType() == pMemoryType);

    assertFalse(pContiguousMemoryInterface.isFree());

    if (pResize)
    {
      if (pContiguousMemoryInterface instanceof net.haesleinhuepf.clij.coremem.interfaces.Resizable)
        ((net.haesleinhuepf.clij.coremem.interfaces.Resizable) pContiguousMemoryInterface).resize(lLength / 2);

      assertTrue(pContiguousMemoryInterface.getSizeInBytes() == lLength
                                                                / 2);

      if (pContiguousMemoryInterface instanceof net.haesleinhuepf.clij.coremem.interfaces.Resizable)
        ((Resizable) pContiguousMemoryInterface).resize(lLength);

      assertTrue(pContiguousMemoryInterface.getSizeInBytes() == lLength);
    }

    if (pContiguousMemoryInterface instanceof net.haesleinhuepf.clij.coremem.interfaces.MappableMemory)
      ((net.haesleinhuepf.clij.coremem.interfaces.MappableMemory) pContiguousMemoryInterface).unmap();

    pContiguousMemoryInterface.free();

  }

  /**
   * Tests the copy from one memory to another
   * 
   * @param pContiguousMemoryInterface1
   *          memory 1
   * @param pContiguousMemoryInterface2
   *          memory 2
   */
  public static void testCopySameSize(net.haesleinhuepf.clij.coremem.ContiguousMemoryInterface pContiguousMemoryInterface1,
                                      net.haesleinhuepf.clij.coremem.ContiguousMemoryInterface pContiguousMemoryInterface2)
  {
    if (pContiguousMemoryInterface1 instanceof net.haesleinhuepf.clij.coremem.interfaces.Copyable<?>)
    {
      if (pContiguousMemoryInterface1 instanceof net.haesleinhuepf.clij.coremem.interfaces.MappableMemory)
        ((net.haesleinhuepf.clij.coremem.interfaces.MappableMemory) pContiguousMemoryInterface1).map();
      if (pContiguousMemoryInterface2 instanceof net.haesleinhuepf.clij.coremem.interfaces.MappableMemory)
        ((net.haesleinhuepf.clij.coremem.interfaces.MappableMemory) pContiguousMemoryInterface2).map();

      // OffHeapMemory lRAMDirect1 = new OffHeapMemory(1L *
      // Integer.MAX_VALUE);
      // OffHeapMemory lRAMDirect2 = new OffHeapMemory(1L *
      // Integer.MAX_VALUE);
      // System.out.println(lRAMDirect1.getLength() / (1024 * 1024));

      net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setByte(pContiguousMemoryInterface1.getAddress(),
                                  (byte) 123);

      net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setByte(pContiguousMemoryInterface1.getAddress()
                                  + pContiguousMemoryInterface1.getSizeInBytes()
                                    / 2,
                                  (byte) 456);

      pContiguousMemoryInterface1.copyTo(pContiguousMemoryInterface2);

      assertEquals((byte) 123,
                   net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getByte(pContiguousMemoryInterface2.getAddress()));
      assertEquals((byte) 456,
                   net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getByte(pContiguousMemoryInterface2.getAddress()
                                               + pContiguousMemoryInterface1.getSizeInBytes()
                                                 / 2));

      pContiguousMemoryInterface1.free();
      pContiguousMemoryInterface2.free();

      try
      {
        pContiguousMemoryInterface1.copyTo(pContiguousMemoryInterface2);
        fail();
      }
      catch (final Throwable e)
      {
      }

      if (pContiguousMemoryInterface1 instanceof net.haesleinhuepf.clij.coremem.interfaces.MappableMemory)
        ((net.haesleinhuepf.clij.coremem.interfaces.MappableMemory) pContiguousMemoryInterface1).unmap();
      if (pContiguousMemoryInterface2 instanceof net.haesleinhuepf.clij.coremem.interfaces.MappableMemory)
        ((net.haesleinhuepf.clij.coremem.interfaces.MappableMemory) pContiguousMemoryInterface2).unmap();
    }
  }

  /**
   * Tests the copy of a range between two different memory objects
   * 
   * @param pContiguousMemoryInterface1
   *          memory 1
   * @param pContiguousMemoryInterface2
   *          memory 2
   */
  @SuppressWarnings("unchecked")
  public static void testCopyRange(net.haesleinhuepf.clij.coremem.ContiguousMemoryInterface pContiguousMemoryInterface1,
                                   net.haesleinhuepf.clij.coremem.ContiguousMemoryInterface pContiguousMemoryInterface2)
  {
    // OffHeapMemory lRAMDirect1 = new OffHeapMemory(4);
    // OffHeapMemory lRAMDirect2 = new OffHeapMemory(8);
    // System.out.println(lRAMDirect1.getLength() / (1024 * 1024));

    if (pContiguousMemoryInterface1 instanceof net.haesleinhuepf.clij.coremem.interfaces.MappableMemory)
      ((net.haesleinhuepf.clij.coremem.interfaces.MappableMemory) pContiguousMemoryInterface1).map();
    if (pContiguousMemoryInterface2 instanceof net.haesleinhuepf.clij.coremem.interfaces.MappableMemory)
      ((net.haesleinhuepf.clij.coremem.interfaces.MappableMemory) pContiguousMemoryInterface2).map();

    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setByte(pContiguousMemoryInterface1.getAddress()
                                + 2, (byte) 123);

    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setByte(pContiguousMemoryInterface1.getAddress()
                                + 3, (byte) 111);

    if (pContiguousMemoryInterface1 instanceof net.haesleinhuepf.clij.coremem.interfaces.Copyable<?>)
    {
      ((net.haesleinhuepf.clij.coremem.interfaces.RangeCopyable<ContiguousMemoryInterface>) pContiguousMemoryInterface1).copyRangeTo(2,
                                                                                           pContiguousMemoryInterface2,
                                                                                           4,
                                                                                           2);

      assertEquals((byte) 123,
                   net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getByte(pContiguousMemoryInterface2.getAddress()
                                               + 4));
      assertEquals((byte) 111,
                   OffHeapMemoryAccess.getByte(pContiguousMemoryInterface2.getAddress()
                                               + 5));

      pContiguousMemoryInterface1.free();
      pContiguousMemoryInterface2.free();

      try
      {
        ((net.haesleinhuepf.clij.coremem.interfaces.RangeCopyable<ContiguousMemoryInterface>) pContiguousMemoryInterface1).copyRangeTo(2,
                                                                                             pContiguousMemoryInterface2,
                                                                                             4,
                                                                                             2);
        fail();
      }
      catch (final Throwable e)
      {
      }
    }

    if (pContiguousMemoryInterface1 instanceof net.haesleinhuepf.clij.coremem.interfaces.MappableMemory)
      ((net.haesleinhuepf.clij.coremem.interfaces.MappableMemory) pContiguousMemoryInterface1).unmap();
    if (pContiguousMemoryInterface2 instanceof net.haesleinhuepf.clij.coremem.interfaces.MappableMemory)
      ((net.haesleinhuepf.clij.coremem.interfaces.MappableMemory) pContiguousMemoryInterface2).unmap();
  }

  /**
   * tests that copy range checks work
   * 
   * @param pContiguousMemoryInterface1
   *          memory 1
   * @param pContiguousMemoryInterface2
   *          memory 2
   */
  @SuppressWarnings("unchecked")
  public static void testCopyChecks(net.haesleinhuepf.clij.coremem.ContiguousMemoryInterface pContiguousMemoryInterface1,
                                    net.haesleinhuepf.clij.coremem.ContiguousMemoryInterface pContiguousMemoryInterface2)
  {
    // OffHeapMemory lRAMDirect1 = new OffHeapMemory(4);
    // OffHeapMemory lRAMDirect2 = new OffHeapMemory(8);
    if (pContiguousMemoryInterface1 instanceof Copyable<?>)
    {
      try
      {
        ((net.haesleinhuepf.clij.coremem.interfaces.RangeCopyable<ContiguousMemoryInterface>) pContiguousMemoryInterface1).copyRangeTo(2,
                                                                                             pContiguousMemoryInterface2,
                                                                                             4,
                                                                                             3);
        fail();
      }
      catch (final Throwable e1)
      {
      }

      try
      {
        ((net.haesleinhuepf.clij.coremem.interfaces.RangeCopyable<ContiguousMemoryInterface>) pContiguousMemoryInterface1).copyRangeTo(2,
                                                                                             pContiguousMemoryInterface2,
                                                                                             4,
                                                                                             -2);
        fail();
      }
      catch (final Throwable e1)
      {
      }

      try
      {
        ((net.haesleinhuepf.clij.coremem.interfaces.RangeCopyable<ContiguousMemoryInterface>) pContiguousMemoryInterface1).copyRangeTo(2,
                                                                                             pContiguousMemoryInterface1,
                                                                                             4,
                                                                                             9);
        fail();
      }
      catch (final Throwable e1)
      {
      }

      pContiguousMemoryInterface1.free();
      pContiguousMemoryInterface2.free();

      try
      {
        ((RangeCopyable<ContiguousMemoryInterface>) pContiguousMemoryInterface1).copyRangeTo(2,
                                                                                             pContiguousMemoryInterface2,
                                                                                             4,
                                                                                             2);
        fail();
      }
      catch (final Throwable e)
      {
      }
    }
  }

  /**
   * Checks read write single native types
   * 
   * @param pContiguousMemoryInterface
   *          memory
   */
  public static void testWriteRead(net.haesleinhuepf.clij.coremem.ContiguousMemoryInterface pContiguousMemoryInterface)
  {
    // OffHeapMemory lRAMDirect = new OffHeapMemory(4);

    if (pContiguousMemoryInterface instanceof net.haesleinhuepf.clij.coremem.interfaces.MappableMemory)
      ((net.haesleinhuepf.clij.coremem.interfaces.MappableMemory) pContiguousMemoryInterface).map();

    pContiguousMemoryInterface.setByteAligned(0, (byte) 255);
    pContiguousMemoryInterface.setByteAligned(1, (byte) 255);
    pContiguousMemoryInterface.setByteAligned(2, (byte) 255);
    pContiguousMemoryInterface.setByteAligned(3, (byte) 255);

    assertEquals(Character.MAX_VALUE,
                 pContiguousMemoryInterface.getCharAligned(0));
    assertEquals(-1, pContiguousMemoryInterface.getShortAligned(0));
    assertEquals(-1, pContiguousMemoryInterface.getIntAligned(0));

    assertEquals(Double.NaN,
                 pContiguousMemoryInterface.getFloatAligned(0),
                 0);

    if (pContiguousMemoryInterface instanceof net.haesleinhuepf.clij.coremem.interfaces.MappableMemory)
      ((MappableMemory) pContiguousMemoryInterface).unmap();
  }

}
