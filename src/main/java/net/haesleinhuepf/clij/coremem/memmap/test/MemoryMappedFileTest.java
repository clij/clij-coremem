package net.haesleinhuepf.clij.coremem.memmap.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import net.haesleinhuepf.clij.coremem.memmap.MemoryMappedFile;
import net.haesleinhuepf.clij.coremem.memmap.MemoryMappedFileAccessMode;
import net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess;
import org.junit.Test;

/**
 * 
 *
 * @author royer
 */
public class MemoryMappedFileTest
{

  /**
   * Map large memory
   * 
   * @throws IOException
   *           NA
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void testMapLargeFile() throws IOException,
                                 InterruptedException
  {
    File lTempFile = File.createTempFile("MemoryMappedFileTest",
                                         "test1");
    lTempFile.deleteOnExit();

    FileChannel lFileChannel =
                             FileChannel.open(lTempFile.toPath(),
                                              StandardOpenOption.CREATE,
                                              StandardOpenOption.READ,
                                              StandardOpenOption.WRITE);

    long lMappingLength = ((long) Integer.MAX_VALUE - 8L) + 1000L;

    net.haesleinhuepf.clij.coremem.memmap.MemoryMappedFile lMemoryMappedFile =
                                       new net.haesleinhuepf.clij.coremem.memmap.MemoryMappedFile(lFileChannel,
                                                            net.haesleinhuepf.clij.coremem.memmap.MemoryMappedFileAccessMode.ReadWrite,
                                                            0,
                                                            lMappingLength,
                                                            true);
    final long lBaseAddress =
                            lMemoryMappedFile.getAddressAtFilePosition(0);

    for (long i = 0; i < lMappingLength; i += 1)
    {
      net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setByte(lBaseAddress + i, (byte) 123);
    }

    assertEquals((byte) 123,
                 net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getByte(lBaseAddress));
    assertEquals((byte) 123,
                 net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getByte(lBaseAddress
                                             + lMappingLength - 1));

    lMemoryMappedFile.close();
    lFileChannel.close();

  }

  /**
   * 
   * Tests mapping not from the start of file
   * 
   * @throws IOException
   *           NA
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void testMapNotFromStart() throws IOException,
                                    InterruptedException
  {
    File lTempFile = File.createTempFile("MemoryMappedFileTest",
                                         "test1");
    lTempFile.deleteOnExit();

    FileChannel lFileChannel =
                             FileChannel.open(lTempFile.toPath(),
                                              StandardOpenOption.CREATE,
                                              StandardOpenOption.READ,
                                              StandardOpenOption.WRITE);

    long lMappingOffset = 234567;
    long lMappingLength = 123456;

    net.haesleinhuepf.clij.coremem.memmap.MemoryMappedFile lMemoryMappedFile =
                                       new net.haesleinhuepf.clij.coremem.memmap.MemoryMappedFile(lFileChannel,
                                                            net.haesleinhuepf.clij.coremem.memmap.MemoryMappedFileAccessMode.ReadWrite,
                                                            lMappingOffset,
                                                            lMappingLength,
                                                            true);

    final long lBaseAddress =
                            lMemoryMappedFile.getAddressAtFilePosition(lMappingOffset);

    for (long i = 0; i < lMappingLength; i += 1)
    {
      net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setByte(lBaseAddress + i, (byte) i);
    }

    assertEquals((byte) 0, net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getByte(lBaseAddress));
    assertEquals((byte) (lMappingLength - 1),
                 net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getByte(lBaseAddress
                                             + lMappingLength - 1));

    lMemoryMappedFile.close();
    lFileChannel.close();

  }

  /**
   * Test create file and open in the middle
   * 
   * @throws IOException
   *           NA
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void testCreateFileAndOpenInTheMiddle() throws IOException,
                                                 InterruptedException
  {
    File lTempFile = File.createTempFile("MemoryMappedFileTest",
                                         "test1");
    lTempFile.deleteOnExit();

    { // creating file

      FileChannel lFileChannel =
                               FileChannel.open(lTempFile.toPath(),
                                                StandardOpenOption.CREATE,
                                                StandardOpenOption.READ,
                                                StandardOpenOption.WRITE);

      long lFileLength = 123456;

      net.haesleinhuepf.clij.coremem.memmap.MemoryMappedFile lMemoryMappedFile =
                                         new net.haesleinhuepf.clij.coremem.memmap.MemoryMappedFile(lFileChannel,
                                                              net.haesleinhuepf.clij.coremem.memmap.MemoryMappedFileAccessMode.ReadWrite,
                                                              0,
                                                              lFileLength,
                                                              true);

      final long lBaseAddress =
                              lMemoryMappedFile.getAddressAtFilePosition(0);

      for (long i = 0; i < lFileLength; i += 1)
      {
        net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.setByte(lBaseAddress + i, (byte) i);
      }
      assertEquals((byte) 0,
                   net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getByte(lBaseAddress));
      assertEquals((byte) (lFileLength - 1),
                   net.haesleinhuepf.clij.coremem.offheap.OffHeapMemoryAccess.getByte(lBaseAddress
                                               + lFileLength - 1));

      lMemoryMappedFile.close();
      lFileChannel.close();
    }

    FileChannel lFileChannel =
                             FileChannel.open(lTempFile.toPath(),
                                              StandardOpenOption.CREATE,
                                              StandardOpenOption.READ,
                                              StandardOpenOption.WRITE);

    long lMappingOffset = 23456;
    long lMappingLength = 66456;

    net.haesleinhuepf.clij.coremem.memmap.MemoryMappedFile lMemoryMappedFile =
                                       new MemoryMappedFile(lFileChannel,
                                                            MemoryMappedFileAccessMode.ReadWrite,
                                                            lMappingOffset,
                                                            lMappingLength,
                                                            true);

    final long lBaseAddress =
                            lMemoryMappedFile.getAddressAtFilePosition(23456);

    for (long i = 0; i < lMappingLength; i++)
    {
      assertEquals((byte) (lMappingOffset + i),
                   OffHeapMemoryAccess.getByte(lBaseAddress + i));
    }

    lMemoryMappedFile.close();
    lFileChannel.close();

  }

}
