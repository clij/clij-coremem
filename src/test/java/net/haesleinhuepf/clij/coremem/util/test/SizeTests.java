package net.haesleinhuepf.clij.coremem.util.test;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import net.haesleinhuepf.clij.coremem.ContiguousMemoryInterface;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.coremem.offheap.OffHeapMemory;
import net.haesleinhuepf.clij.coremem.util.Size;
import org.bridj.Pointer;
import org.junit.Test;

/**
 * Size tests
 *
 * @author royer
 */
public class SizeTests
{

  /**
   * Tests primitive types
   */
  @Test
  public void testPrimitives()
  {

    assertEquals(1, net.haesleinhuepf.clij.coremem.util.Size.of(Byte.class));
    assertEquals(2, net.haesleinhuepf.clij.coremem.util.Size.of(Short.class));
    assertEquals(2, net.haesleinhuepf.clij.coremem.util.Size.of(Character.class));
    assertEquals(4, net.haesleinhuepf.clij.coremem.util.Size.of(Integer.class));
    assertEquals(8, net.haesleinhuepf.clij.coremem.util.Size.of(Long.class));
    assertEquals(4, net.haesleinhuepf.clij.coremem.util.Size.of(Float.class));
    assertEquals(8, net.haesleinhuepf.clij.coremem.util.Size.of(Double.class));

    assertEquals(1, net.haesleinhuepf.clij.coremem.util.Size.of(byte.class));
    assertEquals(2, net.haesleinhuepf.clij.coremem.util.Size.of(short.class));
    assertEquals(2, net.haesleinhuepf.clij.coremem.util.Size.of(char.class));
    assertEquals(4, net.haesleinhuepf.clij.coremem.util.Size.of(int.class));
    assertEquals(8, net.haesleinhuepf.clij.coremem.util.Size.of(long.class));
    assertEquals(4, net.haesleinhuepf.clij.coremem.util.Size.of(float.class));
    assertEquals(8, net.haesleinhuepf.clij.coremem.util.Size.of(double.class));

    {
      byte b = 0;
      short s = 0;
      char c = 0;
      int i = 0;
      long l = 0;
      float f = 0;
      double d = 0;

      assertEquals(1, net.haesleinhuepf.clij.coremem.util.Size.of(b));
      assertEquals(2, net.haesleinhuepf.clij.coremem.util.Size.of(s));
      assertEquals(2, net.haesleinhuepf.clij.coremem.util.Size.of(c));
      assertEquals(4, net.haesleinhuepf.clij.coremem.util.Size.of(i));
      assertEquals(8, net.haesleinhuepf.clij.coremem.util.Size.of(l));
      assertEquals(4, net.haesleinhuepf.clij.coremem.util.Size.of(f));
      assertEquals(8, net.haesleinhuepf.clij.coremem.util.Size.of(d));
    }

    {
      Byte b = new Byte((byte) 0);
      Short s = new Short((short) 0);
      Character c = new Character((char) 0);
      Integer i = new Integer((int) 0);
      Long l = new Long((long) 0);
      Float f = new Float((float) 0);
      Double d = new Double((double) 0);

      assertEquals(1, net.haesleinhuepf.clij.coremem.util.Size.of(b));
      assertEquals(2, net.haesleinhuepf.clij.coremem.util.Size.of(s));
      assertEquals(2, net.haesleinhuepf.clij.coremem.util.Size.of(c));
      assertEquals(4, net.haesleinhuepf.clij.coremem.util.Size.of(i));
      assertEquals(8, net.haesleinhuepf.clij.coremem.util.Size.of(l));
      assertEquals(4, net.haesleinhuepf.clij.coremem.util.Size.of(f));
      assertEquals(8, net.haesleinhuepf.clij.coremem.util.Size.of(d));

    }
  }

  /**
   * Tests native type enum
   */
  @Test
  public void testNativeTypeEnum()
  {
    assertEquals(net.haesleinhuepf.clij.coremem.util.Size.BYTE, net.haesleinhuepf.clij.coremem.util.Size.of(net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum.UnsignedByte));
    assertEquals(net.haesleinhuepf.clij.coremem.util.Size.BYTE, net.haesleinhuepf.clij.coremem.util.Size.of(net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum.Byte));
    assertEquals(net.haesleinhuepf.clij.coremem.util.Size.SHORT, net.haesleinhuepf.clij.coremem.util.Size.of(net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum.UnsignedShort));
    assertEquals(net.haesleinhuepf.clij.coremem.util.Size.SHORT, net.haesleinhuepf.clij.coremem.util.Size.of(net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum.Short));
    assertEquals(net.haesleinhuepf.clij.coremem.util.Size.INT, net.haesleinhuepf.clij.coremem.util.Size.of(net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum.UnsignedInt));
    assertEquals(net.haesleinhuepf.clij.coremem.util.Size.INT, net.haesleinhuepf.clij.coremem.util.Size.of(net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum.Int));
    assertEquals(net.haesleinhuepf.clij.coremem.util.Size.LONG, net.haesleinhuepf.clij.coremem.util.Size.of(net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum.Long));
    assertEquals(net.haesleinhuepf.clij.coremem.util.Size.HALFFLOAT, net.haesleinhuepf.clij.coremem.util.Size.of(net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum.HalfFloat));
    assertEquals(net.haesleinhuepf.clij.coremem.util.Size.FLOAT, net.haesleinhuepf.clij.coremem.util.Size.of(net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum.Float));
    assertEquals(net.haesleinhuepf.clij.coremem.util.Size.DOUBLE, net.haesleinhuepf.clij.coremem.util.Size.of(NativeTypeEnum.Double));
  }

  /**
   * Tests strings
   */
  @Test
  public void testStrings()
  {
    assertEquals(0, net.haesleinhuepf.clij.coremem.util.Size.of(""));
    assertEquals(4 * net.haesleinhuepf.clij.coremem.util.Size.CHAR, net.haesleinhuepf.clij.coremem.util.Size.of("1234"));
  }

  /**
   * Tests NIO Buffers
   */
  @Test
  public void testNIOBuffers()
  {

    assertEquals(11, net.haesleinhuepf.clij.coremem.util.Size.of(ByteBuffer.allocateDirect(11)));

    assertEquals(2 * 11,
                 net.haesleinhuepf.clij.coremem.util.Size.of(ByteBuffer.allocateDirect(2 * 11)
                                   .asCharBuffer()));
    assertEquals(2 * 11,
                 net.haesleinhuepf.clij.coremem.util.Size.of(ByteBuffer.allocateDirect(2 * 11)
                                   .asShortBuffer()));
    assertEquals(4 * 11,
                 net.haesleinhuepf.clij.coremem.util.Size.of(ByteBuffer.allocateDirect(4 * 11)
                                   .asIntBuffer()));
    assertEquals(8 * 11,
                 net.haesleinhuepf.clij.coremem.util.Size.of(ByteBuffer.allocateDirect(8 * 11)
                                   .asLongBuffer()));
    assertEquals(4 * 11,
                 net.haesleinhuepf.clij.coremem.util.Size.of(ByteBuffer.allocateDirect(4 * 11)
                                   .asFloatBuffer()));
    assertEquals(8 * 11,
                 net.haesleinhuepf.clij.coremem.util.Size.of(ByteBuffer.allocateDirect(8 * 11)
                                   .asDoubleBuffer()));

  }

  /**
   * Tests off heap memory
   */
  @Test
  public void testOffHeapMemory()
  {
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemory lOffHeapMemory = net.haesleinhuepf.clij.coremem.offheap.OffHeapMemory.allocateBytes(11);
    ContiguousMemoryInterface lContiguousMemoryInterface =
                                                         OffHeapMemory.allocateBytes(11);
    assertEquals(11, net.haesleinhuepf.clij.coremem.util.Size.of(lOffHeapMemory));
    assertEquals(11, net.haesleinhuepf.clij.coremem.util.Size.of(lContiguousMemoryInterface));
  }

  /**
   * Tests BridJ pointer
   */
  @Test
  public void testBridJPointer()
  {
    Pointer<Byte> lPointer = Pointer.allocateBytes(11);
    assertEquals(11, Size.of(lPointer));
  }

}
