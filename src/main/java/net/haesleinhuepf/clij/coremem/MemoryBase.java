package net.haesleinhuepf.clij.coremem;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import net.haesleinhuepf.clij.coremem.ContiguousMemoryInterface;
import net.haesleinhuepf.clij.coremem.enums.MemoryType;
import net.haesleinhuepf.clij.coremem.exceptions.InvalidNativeMemoryAccessException;
import net.haesleinhuepf.clij.coremem.exceptions.InvalidWriteAtReadOnly;
import net.haesleinhuepf.clij.coremem.exceptions.MemoryMapException;
import net.haesleinhuepf.clij.coremem.interfaces.MappableMemory;
import net.haesleinhuepf.clij.coremem.interfaces.PointerAccessible;
import net.haesleinhuepf.clij.coremem.interfaces.RangeCopyable;
import net.haesleinhuepf.clij.coremem.interfaces.SizedInBytes;
import net.haesleinhuepf.clij.coremem.interop.BridJInterop;
import net.haesleinhuepf.clij.coremem.interop.JNAInterop;
import net.haesleinhuepf.clij.coremem.interop.NIOBuffersInterop;
import net.haesleinhuepf.clij.coremem.offheap.OffHeapMemory;
import net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess;
import net.haesleinhuepf.clij.coremem.rgc.Cleanable;
import net.haesleinhuepf.clij.coremem.rgc.Freeable;
import net.haesleinhuepf.clij.coremem.rgc.FreeableBase;
import net.haesleinhuepf.clij.coremem.util.Size;

/**
 * This abstract base class offers basic functionality for off-heap memory
 * access, copying, sizing, and memory life-cycle management and garbage
 * collection.
 * 
 * @author royer
 */
public abstract class MemoryBase extends FreeableBase implements
        PointerAccessible,
        SizedInBytes,
        ContiguousMemoryInterface,
        RangeCopyable<MemoryBase>,
        Freeable,
        Cleanable

{

  protected long mAddressInBytes = 0;
  protected long mLengthInBytes = 0;
  protected boolean mIsFree = false;

  /**
   * Protected parameterless constructor
   */
  protected MemoryBase()
  {
  }

  /**
   * Constructs a MemoryBase given an address and length (absolute and all in
   * bytes).
   * 
   * @param pAddressInBytes
   *          absolute address in bytes
   * @param pLengtInBytes
   *          length in bytes.
   */
  public MemoryBase(long pAddressInBytes, long pLengtInBytes)
  {
    mAddressInBytes = pAddressInBytes;
    mLengthInBytes = pLengtInBytes;
  }

  @Override
  public abstract MemoryType getMemoryType();

  @Override
  public long getSizeInBytes()
  {
    complainIfFreed();
    return mLengthInBytes;
  }

  @Override
  public void copyFrom(ContiguousMemoryInterface pFrom)
  {
    pFrom.copyTo(this);
  }

  @Override
  public void copyTo(ContiguousMemoryInterface pTo)
  {
    complainIfFreed();
    checkMappableMemory(pTo);
    checkMappableMemory(this);

    if (this.getSizeInBytes() != pTo.getSizeInBytes())
    {
      final String lErrorString =
                                String.format("Attempted to copy memory regions of different sizes: src.len=%d, dst.len=%d",
                                              this.getSizeInBytes(),
                                              pTo.getSizeInBytes());
      // error("KAM", lErrorString);
      throw new net.haesleinhuepf.clij.coremem.exceptions.InvalidNativeMemoryAccessException(lErrorString);
    }
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyMemory(this.getAddress(),
                                   pTo.getAddress(),
                                   pTo.getSizeInBytes());
  }

  /* (non-Javadoc)
   * @see RangeCopyable#copyRangeTo(long, java.lang.Object, long, long)
   */
  @Override
  public void copyRangeTo(long pSourceOffset,
                          MemoryBase pTo,
                          long pDestinationOffset,
                          long pLengthToCopy)
  {
    complainIfFreed();
    checkMappableMemory(pTo);
    checkMappableMemory(this);

    if (pLengthToCopy == 0)
      return;

    final long lSrcAddress = this.getAddress();
    final long lDstAddress = pTo.getAddress();

    final long lSrcLength = this.getSizeInBytes();
    final long lDstLength = pTo.getSizeInBytes();

    if ((pLengthToCopy < 0))
    {
      final String lErrorString =
                                "Attempted to copy a region of negative length!";
      // error("KAM", lErrorString);
      throw new net.haesleinhuepf.clij.coremem.exceptions.InvalidNativeMemoryAccessException(lErrorString);
    }

    if ((pLengthToCopy + pSourceOffset > lSrcLength))
    {
      final String lErrorString =
                                "Attempted to read past the end of the source memory region";
      // error("KAM", lErrorString);
      throw new net.haesleinhuepf.clij.coremem.exceptions.InvalidNativeMemoryAccessException(lErrorString);
    }

    if ((pLengthToCopy + pDestinationOffset > lDstLength))
    {
      final String lErrorString =
                                "Attempted to writing past the end of the destination memory region";
      // error("KAM", lErrorString);
      throw new InvalidNativeMemoryAccessException(lErrorString);
    }

    final long lSrcCopyAddress = lSrcAddress + pSourceOffset;
    final long lDstCopyAddress = lDstAddress + pDestinationOffset;

    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyMemory(lSrcCopyAddress,
                                   lDstCopyAddress,
                                   pLengthToCopy);
  }

  /**
   * Checks whether the provided ContiguousMemoryInterface is mappable and
   * currently mapped.
   * 
   * @param pContiguousMemoryInterface
   */
  void checkMappableMemory(ContiguousMemoryInterface pContiguousMemoryInterface)
  {
    if (pContiguousMemoryInterface instanceof net.haesleinhuepf.clij.coremem.interfaces.MappableMemory)
    {
      final net.haesleinhuepf.clij.coremem.interfaces.MappableMemory lMappableMemory =
                                           (MappableMemory) pContiguousMemoryInterface;
      if (!lMappableMemory.isCurrentlyMapped())
        throw new MemoryMapException("Memory is not mapped!");
    }
  }

  @Override
  public long getAddress()
  {
    complainIfFreed();
    checkMappableMemory(this);
    return mAddressInBytes;
  }

  @Override
  public void free()
  {
    mIsFree = true;
  }

  @Override
  public boolean isFree()
  {
    return mIsFree;
  }

  @Override
  public void setByteAligned(long pOffset, byte pValue)
  {
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setByte(mAddressInBytes
                                + (pOffset << net.haesleinhuepf.clij.coremem.util.Size.BYTESHIFT),
                                pValue);
  }

  @Override
  public void setCharAligned(long pOffset, char pValue)
  {
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setChar(mAddressInBytes
                                + (pOffset << net.haesleinhuepf.clij.coremem.util.Size.CHARSHIFT),
                                pValue);
  }

  @Override
  public void setShortAligned(long pOffset, short pValue)
  {
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setShort(mAddressInBytes
                                 + (pOffset << net.haesleinhuepf.clij.coremem.util.Size.SHORTSHIFT),
                                 pValue);
  }

  @Override
  public void setIntAligned(long pOffset, int pValue)
  {
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setInt(mAddressInBytes
                               + (pOffset << net.haesleinhuepf.clij.coremem.util.Size.INTSHIFT), pValue);
  }

  @Override
  public void setLongAligned(long pOffset, long pValue)
  {
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setLong(mAddressInBytes
                                + (pOffset << net.haesleinhuepf.clij.coremem.util.Size.LONGSHIFT),
                                pValue);
  }

  @Override
  public void setFloatAligned(long pOffset, float pValue)
  {
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setFloat(mAddressInBytes
                                 + (pOffset << net.haesleinhuepf.clij.coremem.util.Size.FLOATSHIFT),
                                 pValue);
  }

  @Override
  public void setDoubleAligned(long pOffset, double pValue)
  {
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setDouble(mAddressInBytes
                                  + (pOffset << net.haesleinhuepf.clij.coremem.util.Size.DOUBLESHIFT),
                                  pValue);
  }

  @Override
  public byte getByteAligned(long pOffset)
  {
    return net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getByte(mAddressInBytes
                                       + (pOffset << net.haesleinhuepf.clij.coremem.util.Size.BYTESHIFT));
  }

  @Override
  public char getCharAligned(long pOffset)
  {
    return net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getChar(mAddressInBytes
                                       + (pOffset << net.haesleinhuepf.clij.coremem.util.Size.CHARSHIFT));
  }

  @Override
  public short getShortAligned(long pOffset)
  {
    return net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getShort(mAddressInBytes
                                        + (pOffset << net.haesleinhuepf.clij.coremem.util.Size.SHORTSHIFT));
  }

  @Override
  public int getIntAligned(long pOffset)
  {
    return net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getInt(mAddressInBytes
                                      + (pOffset << net.haesleinhuepf.clij.coremem.util.Size.INTSHIFT));
  }

  @Override
  public long getLongAligned(long pOffset)
  {
    return net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getLong(mAddressInBytes
                                       + (pOffset << net.haesleinhuepf.clij.coremem.util.Size.LONGSHIFT));
  }

  @Override
  public float getFloatAligned(long pOffset)
  {
    return net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getFloat(mAddressInBytes
                                        + (pOffset << net.haesleinhuepf.clij.coremem.util.Size.FLOATSHIFT));
  }

  @Override
  public double getDoubleAligned(long pOffset)
  {
    return net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getDouble(mAddressInBytes
                                         + (pOffset << net.haesleinhuepf.clij.coremem.util.Size.DOUBLESHIFT));
  }

  @Override
  public void setByte(long pOffset, byte pValue)
  {
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setByte(mAddressInBytes + pOffset, pValue);
  }

  @Override
  public void setChar(long pOffset, char pValue)
  {
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setChar(mAddressInBytes + net.haesleinhuepf.clij.coremem.util.Size.CHARSHIFT,
                                pValue);
  }

  @Override
  public void setShort(long pOffset, short pValue)
  {
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setShort(mAddressInBytes + pOffset, pValue);
  }

  @Override
  public void setInt(long pOffset, int pValue)
  {
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setInt(mAddressInBytes + pOffset, pValue);
  }

  @Override
  public void setLong(long pOffset, long pValue)
  {
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setLong(mAddressInBytes + pOffset, pValue);
  }

  @Override
  public void setFloat(long pOffset, float pValue)
  {
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setFloat(mAddressInBytes + pOffset, pValue);
  }

  @Override
  public void setDouble(long pOffset, double pValue)
  {
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setDouble(mAddressInBytes + pOffset, pValue);
  }

  @Override
  public byte getByte(long pOffset)
  {
    return net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getByte(mAddressInBytes + pOffset);
  }

  @Override
  public char getChar(long pOffset)
  {
    return net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getChar(mAddressInBytes + pOffset);
  }

  @Override
  public short getShort(long pOffset)
  {
    return net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getShort(mAddressInBytes + pOffset);
  }

  @Override
  public int getInt(long pOffset)
  {
    return net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getInt(mAddressInBytes + pOffset);
  }

  @Override
  public long getLong(long pOffset)
  {
    return net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getLong(mAddressInBytes + pOffset);
  }

  @Override
  public float getFloat(long pOffset)
  {
    return net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getFloat(mAddressInBytes + pOffset);
  }

  @Override
  public double getDouble(long pOffset)
  {
    return net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getDouble(mAddressInBytes + pOffset);
  }

  @Override
  public void copyFrom(Buffer pBuffer)
  {
    complainIfFreed();
    if (!pBuffer.isDirect())
    {
      if (pBuffer instanceof ByteBuffer)
      {
        final byte[] lByteArray = (byte[]) pBuffer.array();
        this.copyFrom(lByteArray);
      }
    }
    else
    {
      final net.haesleinhuepf.clij.coremem.offheap.OffHeapMemory lContiguousMemoryFrom =
                                                net.haesleinhuepf.clij.coremem.interop.NIOBuffersInterop.getContiguousMemoryFrom(pBuffer);
      this.copyFrom(lContiguousMemoryFrom);
    }
  }

  @Override
  public void copyTo(Buffer pBuffer)
  {
    complainIfFreed();

    if (pBuffer.isReadOnly())
      throw new InvalidWriteAtReadOnly("Cannot write to read-only buffer!");
    if (!pBuffer.isDirect())
    {
      if (pBuffer instanceof ByteBuffer)
      {
        final byte[] lByteArray = (byte[]) pBuffer.array();
        this.copyFrom(lByteArray);
      }
    }
    else
    {
      final OffHeapMemory lContiguousMemoryFrom =
                                                net.haesleinhuepf.clij.coremem.interop.NIOBuffersInterop.getContiguousMemoryFrom(pBuffer);
      this.copyTo(lContiguousMemoryFrom);
    }

  }

  @Override
  public long writeBytesToFileChannel(FileChannel pFileChannel,
                                      long pFilePositionInBytes) throws IOException
  {
    complainIfFreed();
    return writeBytesToFileChannel(0,
                                   pFileChannel,
                                   pFilePositionInBytes,
                                   getSizeInBytes());
  }

  @Override
  public void copyTo(byte[] pTo)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyToArray(getAddress(),
                                    pTo,
                                    0,
                                    getSizeInBytes());
  }

  @Override
  public void copyTo(short[] pTo)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyToArray(getAddress(),
                                    pTo,
                                    0,
                                    getSizeInBytes());
  }

  @Override
  public void copyTo(char[] pTo)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyToArray(getAddress(),
                                    pTo,
                                    0,
                                    getSizeInBytes());
  }

  @Override
  public void copyTo(int[] pTo)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyToArray(getAddress(),
                                    pTo,
                                    0,
                                    getSizeInBytes());
  }

  @Override
  public void copyTo(long[] pTo)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyToArray(getAddress(),
                                    pTo,
                                    0,
                                    getSizeInBytes());
  }

  @Override
  public void copyTo(float[] pTo)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyToArray(getAddress(),
                                    pTo,
                                    0,
                                    getSizeInBytes());
  }

  @Override
  public void copyTo(double[] pTo)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyToArray(getAddress(),
                                    pTo,
                                    0,
                                    getSizeInBytes());
  }

  @Override
  public void copyFrom(byte[] pFrom)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyFromArray(pFrom,
                                      0,
                                      getAddress(),
                                      getSizeInBytes());
  }

  @Override
  public void copyFrom(short[] pFrom)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyFromArray(pFrom,
                                      0,
                                      getAddress(),
                                      getSizeInBytes());
  }

  @Override
  public void copyFrom(char[] pFrom)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyFromArray(pFrom,
                                      0,
                                      getAddress(),
                                      getSizeInBytes());
  }

  @Override
  public void copyFrom(int[] pFrom)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyFromArray(pFrom,
                                      0,
                                      getAddress(),
                                      getSizeInBytes());
  }

  @Override
  public void copyFrom(long[] pFrom)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyFromArray(pFrom,
                                      0,
                                      getAddress(),
                                      getSizeInBytes());
  }

  @Override
  public void copyFrom(float[] pFrom)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyFromArray(pFrom,
                                      0,
                                      getAddress(),
                                      getSizeInBytes());
  }

  @Override
  public void copyFrom(double[] pFrom)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyFromArray(pFrom,
                                      0,
                                      getAddress(),
                                      getSizeInBytes());
  }

  @Override
  public void copyTo(byte[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyToArray(getAddress()
                                    + pSrcOffset * (long) net.haesleinhuepf.clij.coremem.util.Size.BYTE,
                                    pTo,
                                    pDstOffset * (long) net.haesleinhuepf.clij.coremem.util.Size.BYTE,
                                    pLength * (long) net.haesleinhuepf.clij.coremem.util.Size.BYTE);
  }

  @Override
  public void copyTo(short[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyToArray(getAddress()
                                    + pSrcOffset * (long) net.haesleinhuepf.clij.coremem.util.Size.SHORT,
                                    pTo,
                                    pDstOffset * (long) net.haesleinhuepf.clij.coremem.util.Size.SHORT,
                                    pLength * (long) net.haesleinhuepf.clij.coremem.util.Size.SHORT);
  }

  @Override
  public void copyTo(char[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyToArray(getAddress()
                                    + pSrcOffset * (long) net.haesleinhuepf.clij.coremem.util.Size.CHAR,
                                    pTo,
                                    pDstOffset * (long) net.haesleinhuepf.clij.coremem.util.Size.CHAR,
                                    pLength * (long) net.haesleinhuepf.clij.coremem.util.Size.CHAR);
  }

  @Override
  public void copyTo(int[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyToArray(getAddress()
                                    + pSrcOffset * (long) net.haesleinhuepf.clij.coremem.util.Size.INT,
                                    pTo,
                                    pDstOffset * (long) net.haesleinhuepf.clij.coremem.util.Size.INT,
                                    pLength * (long) net.haesleinhuepf.clij.coremem.util.Size.INT);
  }

  @Override
  public void copyTo(long[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyToArray(getAddress()
                                    + pSrcOffset * (long) net.haesleinhuepf.clij.coremem.util.Size.LONG,
                                    pTo,
                                    pDstOffset * (long) net.haesleinhuepf.clij.coremem.util.Size.LONG,
                                    pLength * (long) net.haesleinhuepf.clij.coremem.util.Size.LONG);
  }

  @Override
  public void copyTo(float[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyToArray(getAddress()
                                    + pSrcOffset * (long) net.haesleinhuepf.clij.coremem.util.Size.FLOAT,
                                    pTo,
                                    pDstOffset * (long) net.haesleinhuepf.clij.coremem.util.Size.FLOAT,
                                    pLength * (long) net.haesleinhuepf.clij.coremem.util.Size.FLOAT);
  }

  @Override
  public void copyTo(double[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyToArray(getAddress()
                                    + pSrcOffset * (long) net.haesleinhuepf.clij.coremem.util.Size.DOUBLE,
                                    pTo,
                                    pDstOffset * (long) net.haesleinhuepf.clij.coremem.util.Size.DOUBLE,
                                    pLength * (long) net.haesleinhuepf.clij.coremem.util.Size.DOUBLE);
  }

  @Override
  public void copyFrom(byte[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyFromArray(pFrom,
                                      pSrcOffset,
                                      getAddress() + pDstOffset
                                                     * (long) net.haesleinhuepf.clij.coremem.util.Size.BYTE,
                                      pLength * (long) net.haesleinhuepf.clij.coremem.util.Size.BYTE);
  }

  @Override
  public void copyFrom(short[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyFromArray(pFrom,
                                      pSrcOffset,
                                      getAddress() + pDstOffset
                                                     * (long) net.haesleinhuepf.clij.coremem.util.Size.SHORT,
                                      pLength * (long) net.haesleinhuepf.clij.coremem.util.Size.SHORT);
  }

  @Override
  public void copyFrom(char[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyFromArray(pFrom,
                                      pSrcOffset,
                                      getAddress() + pDstOffset
                                                     * (long) net.haesleinhuepf.clij.coremem.util.Size.CHAR,
                                      pLength * (long) net.haesleinhuepf.clij.coremem.util.Size.CHAR);
  }

  @Override
  public void copyFrom(int[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyFromArray(pFrom,
                                      pSrcOffset,
                                      getAddress() + pDstOffset
                                                     * (long) net.haesleinhuepf.clij.coremem.util.Size.INT,
                                      pLength * (long) net.haesleinhuepf.clij.coremem.util.Size.INT);
  }

  @Override
  public void copyFrom(long[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyFromArray(pFrom,
                                      pSrcOffset,
                                      getAddress() + pDstOffset
                                                     * (long) net.haesleinhuepf.clij.coremem.util.Size.LONG,
                                      pLength * (long) net.haesleinhuepf.clij.coremem.util.Size.LONG);
  }

  @Override
  public void copyFrom(float[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength)
  {
    complainIfFreed();
    net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.copyFromArray(pFrom,
                                      pSrcOffset,
                                      getAddress() + pDstOffset
                                                     * (long) net.haesleinhuepf.clij.coremem.util.Size.FLOAT,
                                      pLength * (long) net.haesleinhuepf.clij.coremem.util.Size.FLOAT);
  }

  @Override
  public void copyFrom(double[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyFromArray(pFrom,
                                      pSrcOffset,
                                      getAddress() + pDstOffset
                                                     * (long) net.haesleinhuepf.clij.coremem.util.Size.DOUBLE,
                                      pLength * (long) Size.DOUBLE);
  }

  @Override
  public long writeBytesToFileChannel(long pPositionInBufferInBytes,
                                      FileChannel pFileChannel,
                                      long pFilePositionInBytes,
                                      long pLengthInBytes) throws IOException
  {
    complainIfFreed();

    ArrayList<ByteBuffer> lByteBuffersForContiguousMemory =
                                                          net.haesleinhuepf.clij.coremem.interop.NIOBuffersInterop.getByteBuffersForContiguousMemory(this,
                                                                                                              pPositionInBufferInBytes,
                                                                                                              pLengthInBytes);

    pFileChannel.position(pFilePositionInBytes);
    for (ByteBuffer lByteBuffer : lByteBuffersForContiguousMemory)
    {
      pFileChannel.write(lByteBuffer);
    }
    return pFilePositionInBytes + pLengthInBytes;

  }

  @Override
  public long readBytesFromFileChannel(FileChannel pFileChannel,
                                       long pFilePositionInBytes,
                                       long pLengthInBytes) throws IOException
  {
    complainIfFreed();
    return readBytesFromFileChannel(0,
                                    pFileChannel,
                                    pFilePositionInBytes,
                                    pLengthInBytes);
  }

  @Override
  public long readBytesFromFileChannel(long pPositionInBufferInBytes,
                                       FileChannel pFileChannel,
                                       long pFilePositionInBytes,
                                       long pLengthInBytes) throws IOException
  {
    complainIfFreed();

    ArrayList<ByteBuffer> lByteBuffersForContiguousMemory =
                                                          NIOBuffersInterop.getByteBuffersForContiguousMemory(this,
                                                                                                              pPositionInBufferInBytes,
                                                                                                              pLengthInBytes);

    pFileChannel.position(pFilePositionInBytes);
    for (ByteBuffer lByteBuffer : lByteBuffersForContiguousMemory)
    {
      pFileChannel.read(lByteBuffer);
    }

    return pFilePositionInBytes + pLengthInBytes;

  }

  @Override
  @SuppressWarnings(
  { "unchecked", "rawtypes" })
  public org.bridj.Pointer getBridJPointer(Class pTargetClass)
  {
    complainIfFreed();
    return BridJInterop.getBridJPointer(this, pTargetClass);
  }

  /**
   * Rerturn a JNA pointer. Usefull when interacting with JNA based bindings.
   * 
   * @return off-heap memory object
   */
  @Override
  public com.sun.jna.Pointer getJNAPointer()
  {
    complainIfFreed();
    return JNAInterop.getJNAPointer(this);
  }

  @Override
  public ByteBuffer getByteBuffer()
  {
    complainIfFreed();
    final ByteBuffer lByteBuffer =
                                 getBridJPointer(Byte.class).getByteBuffer();
    return lByteBuffer;
  }

}
