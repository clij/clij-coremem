package net.haesleinhuepf.clij.coremem.memmap;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import net.haesleinhuepf.clij.coremem.ContiguousMemoryInterface;
import net.haesleinhuepf.clij.coremem.MappedMemoryBase;
import net.haesleinhuepf.clij.coremem.enums.MemoryType;
import net.haesleinhuepf.clij.coremem.exceptions.InvalidNativeMemoryAccessException;
import net.haesleinhuepf.clij.coremem.exceptions.MemoryMapFileException;
import net.haesleinhuepf.clij.coremem.exceptions.UnsupportedMemoryResizingException;
import net.haesleinhuepf.clij.coremem.interfaces.MappableMemory;
import net.haesleinhuepf.clij.coremem.interfaces.Resizable;
import net.haesleinhuepf.clij.coremem.interfaces.SizedInBytes;
import net.haesleinhuepf.clij.coremem.offheap.OffHeapMemory;
import net.haesleinhuepf.clij.coremem.rgc.Cleaner;
import net.haesleinhuepf.clij.coremem.rgc.Freeable;

/**
 * File mapped memory region. Implements the contiguous memory interface but is
 * stored in a file.
 *
 * @author royer
 */
public class FileMappedMemoryRegion extends MappedMemoryBase
                                    implements
                                    MappableMemory,
        Resizable,
        SizedInBytes,
                                    ContiguousMemoryInterface,
        Freeable

{

  private final FileChannel mFileChannel;
  private final StandardOpenOption[] mStandardOpenOption;
  private final long mFilePositionInBytes;
  private MemoryMappedFile mMemoryMappedFile;

  /**
   * Creates a new file of given length and maps it to memory.
   * 
   * @param pFile
   *          file length
   * @param pLengthInBytes
   *          length in bytes
   * @return file mapped memory region
   * @throws IOException
   *           thrown if problem while creating file or memory mapping
   */
  public FileMappedMemoryRegion createNewFileMappedMemoryRegion(File pFile,
                                                                final long pLengthInBytes) throws IOException
  {
    return new FileMappedMemoryRegion(pFile,
                                      0,
                                      pLengthInBytes,
                                      StandardOpenOption.CREATE_NEW,
                                      StandardOpenOption.READ,
                                      StandardOpenOption.WRITE);
  }

  /**
   * Creates new sparse file and maps it to memory.
   * 
   * @param pFile
   *          file
   * @param pLengthInBytes
   *          length in bytes
   * @return file mapped memory region
   * @throws IOException
   *           thrown if problem while creating file or memory mapping
   */
  public FileMappedMemoryRegion createNewSparseFileMappedMemoryRegion(File pFile,
                                                                      final long pLengthInBytes) throws IOException
  {
    return new FileMappedMemoryRegion(pFile,
                                      0,
                                      pLengthInBytes,
                                      StandardOpenOption.CREATE_NEW,
                                      StandardOpenOption.READ,
                                      StandardOpenOption.WRITE,
                                      StandardOpenOption.SPARSE);
  }

  /**
   * Opens an existing file and maps it to memory
   * 
   * @param pFile
   *          file
   * @param pLengthInBytes
   *          length in bytes
   * @return file mapped memory region
   * @throws IOException
   *           thrown if problem while creating file or memory mapping
   */
  public FileMappedMemoryRegion openExistingFileMappedMemoryRegion(File pFile,
                                                                   final long pLengthInBytes) throws IOException
  {
    return openExistingFileMappedMemoryRegion(pFile,
                                              0,
                                              pLengthInBytes);
  }

  /**
   * Opens an existing file and maps it to memory.
   * 
   * @param pFile
   *          file
   * @param pPositionInBytes
   *          position in bytes
   * @param pLengthInBytes
   *          length in bytes
   * @return file mapped memory region
   * @throws IOException
   *           thrown if problem while creating file or memory mapping
   */
  public FileMappedMemoryRegion openExistingFileMappedMemoryRegion(File pFile,
                                                                   final long pPositionInBytes,
                                                                   final long pLengthInBytes) throws IOException
  {
    return new FileMappedMemoryRegion(pFile,
                                      pPositionInBytes,
                                      pLengthInBytes,
                                      StandardOpenOption.READ,
                                      StandardOpenOption.WRITE);
  }

  /**
   * Opens an existing file read-only and maps it to memory.
   * 
   * @param pFile
   *          file
   * @param pLengthInBytes
   *          length in bytes
   * @return file mapped memory region
   * @throws IOException
   *           thrown if problem while creating file or memory mapping
   */
  public FileMappedMemoryRegion openReadOnlyExistingFileMappedMemoryRegion(File pFile,
                                                                           final long pLengthInBytes) throws IOException
  {
    return openReadOnlyExistingFileMappedMemoryRegion(pFile,
                                                      0,
                                                      pLengthInBytes);
  }

  /**
   * Opens an existing file read-only and maps it to memory.
   * 
   * @param pFile
   *          file
   * @param pPositionInBytes
   *          position in bytes
   * @param pLengthInBytes
   *          length in bytes
   * @return file mapped memory region
   * @throws IOException
   *           thrown if problem while creating file or memory mapping
   */
  public FileMappedMemoryRegion openReadOnlyExistingFileMappedMemoryRegion(File pFile,
                                                                           final long pPositionInBytes,
                                                                           final long pLengthInBytes) throws IOException
  {
    return new FileMappedMemoryRegion(pFile,
                                      pPositionInBytes,
                                      pLengthInBytes,
                                      StandardOpenOption.READ);
  }

  /**
   * Instanciates a file mapped memory region given a file, length in bytes, and
   * standard open options.
   * 
   * @param pFile
   *          file
   * @param pLengthInBytes
   *          length in bytes
   * @param pStandardOpenOption
   *          standard open options
   * @throws IOException
   *           thrown if problem while creating file or memory mapping
   */
  public FileMappedMemoryRegion(File pFile,
                                final long pLengthInBytes,
                                StandardOpenOption... pStandardOpenOption) throws IOException
  {
    this(pFile, 0, pLengthInBytes, pStandardOpenOption);
  }

  /**
   * Instanciates a file mapped memory region given a file, position in file,
   * length in bytes, and standard open options.
   * 
   * @param pFile
   *          file
   * @param pPositionInBytes
   *          position in bytes within file
   * @param pLengthInBytes
   *          length in bytes
   * @param pStandardOpenOption
   *          standard open options
   * @throws IOException
   *           thrown if problem while creating file or memory mapping
   */
  public FileMappedMemoryRegion(File pFile,
                                final long pPositionInBytes,
                                final long pLengthInBytes,
                                StandardOpenOption... pStandardOpenOption) throws IOException
  {
    this(FileChannel.open(pFile.toPath(),
                          obtainStandardOptions(pFile,
                                                pStandardOpenOption)),
         pPositionInBytes,
         pLengthInBytes,
         pStandardOpenOption);

  }

  /**
   * Instanciates a file mapped memory region given a file channel, position in
   * file, length in bytes, and standard open options.
   * 
   * @param pFileChannel
   *          file channel
   * @param pPositionInBytes
   *          position in bytes within file
   * @param pLengthInBytes
   *          length in bytes
   * @param pStandardOpenOption
   *          standard open options
   * @throws IOException
   *           thrown if problem while creating file or memory mapping
   */
  public FileMappedMemoryRegion(FileChannel pFileChannel,
                                final long pPositionInBytes,
                                final long pLengthInBytes,
                                StandardOpenOption... pStandardOpenOption) throws IOException
  {
    super();
    mFileChannel = pFileChannel;
    mFilePositionInBytes = pPositionInBytes;
    mLengthInBytes = pLengthInBytes;
    mStandardOpenOption = pStandardOpenOption;
  }

  /**
   * @param pFile
   * @param pStandardOpenOption
   * @return
   */
  static StandardOpenOption[] obtainStandardOptions(File pFile,
                                                    StandardOpenOption... pStandardOpenOption)
  {
    StandardOpenOption[] lStandardOpenOption = pStandardOpenOption;
    if (pStandardOpenOption == null
        || pStandardOpenOption.length == 0)
    {
      if (pFile.exists())
        lStandardOpenOption = new StandardOpenOption[]
        { StandardOpenOption.READ, StandardOpenOption.WRITE };
      else
        lStandardOpenOption = new StandardOpenOption[]
        { StandardOpenOption.CREATE_NEW,
          StandardOpenOption.READ,
          StandardOpenOption.WRITE };
    }
    return lStandardOpenOption;
  }

  /* (non-Javadoc)
   * @see MappedMemoryBase#map()
   */
  @Override
  public long map()
  {
    if (isCurrentlyMapped() && mAddressInBytes != 0)
      return mAddressInBytes;
    try
    {
      mMemoryMappedFile =
                        new MemoryMappedFile(mFileChannel,
                                             MemoryMappedFileUtils.bestMode(mStandardOpenOption),
                                             mFilePositionInBytes,
                                             mLengthInBytes,
                                             mFileChannel.size() < mFilePositionInBytes
                                                                   + mLengthInBytes);
      mAddressInBytes =
                      mMemoryMappedFile.getAddressAtFilePosition(mFilePositionInBytes);
      setCurrentlyMapped(true);
      return mAddressInBytes;
    }
    catch (MemoryMapFileException | IOException e)
    {
      throw new MemoryMapFileException("Could not map file channel "
                                       + mFileChannel, e);
    }

  }

  /* (non-Javadoc)
   * @see MappableMemory#force()
   */
  @Override
  public void force()
  {
    try
    {
      mFileChannel.force(true);
    }
    catch (final IOException e)
    {
      final String lErrorMessage =
                                 String.format("Could not force memory mapping consistency! ");
      throw new MemoryMapFileException(lErrorMessage, e);
    }
  }

  /* (non-Javadoc)
   * @see MappedMemoryBase#unmap()
   */
  @Override
  public void unmap()
  {
    if (!isCurrentlyMapped())
      return;

    // force();
    try
    {
      mMemoryMappedFile.close();
      setCurrentlyMapped(false);
    }
    catch (final IOException e)
    {
      throw new RuntimeException("Exception while unmapping "
                                 + this.getClass().getSimpleName(),
                                 e);
    }
    finally
    {
      mMemoryMappedFile = null;
      mAddressInBytes = 0;
      mLengthInBytes = 0;
    }
  }

  /* (non-Javadoc)
   * @see ContiguousMemoryInterface#subRegion(long, long)
   */
  @Override
  public net.haesleinhuepf.clij.coremem.offheap.OffHeapMemory subRegion(long pOffset, long pLenghInBytes)
  {
    if (mAddressInBytes + pOffset + pLenghInBytes > mAddressInBytes
                                                    + mLengthInBytes)
      throw new InvalidNativeMemoryAccessException(String.format("Cannot instanciate OffHeapMemory from FileMappedMemoryRegion on subregion staring at offset %d and length %d  ",
                                                                 pOffset,
                                                                 pLenghInBytes));
    final net.haesleinhuepf.clij.coremem.offheap.OffHeapMemory lOffHeapMemory =
                                       new OffHeapMemory(this,
                                                         mAddressInBytes
                                                               + pOffset,
                                                         pLenghInBytes);
    return lOffHeapMemory;
  }

  /* (non-Javadoc)
   * @see MemoryBase#getMemoryType()
   */
  @Override
  public MemoryType getMemoryType()
  {
    complainIfFreed();
    return MemoryType.FILERAM;
  }

  /* (non-Javadoc)
   * @see Resizable#resize(long)
   */
  @Override
  public long resize(long pNewLength)
  {
    final String lErrorMessage =
                               String.format("Could not resize memory mapped file! ");
    throw new UnsupportedMemoryResizingException(lErrorMessage);
  }

  /* (non-Javadoc)
   * @see MemoryBase#free()
   */
  @Override
  public void free()
  {
    try
    {
      unmap();
      super.free();
    }
    catch (final Throwable e)
    {
      final String lErrorMessage =
                                 String.format("Could not unmap memory mapped file! ");
      throw new MemoryMapFileException(lErrorMessage, e);
    }

  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return "FileMappedMemoryRegion [mFileChannel=" + mFileChannel
           + ", mStandardOpenOption="
           + Arrays.toString(mStandardOpenOption)
           + ", mFilePositionInBytes="
           + mFilePositionInBytes
           + ", mLengthInBytes="
           + mLengthInBytes
           + ", mIsMapped="
           + isCurrentlyMapped()
           + ", mAddressInBytes="
           + mAddressInBytes
           + ", mLengthInBytes="
           + mLengthInBytes
           + ", mIsFree="
           + mIsFree
           + ", getMemoryType()="
           + getMemoryType()
           + "]";
  }

  /* (non-Javadoc)
   * @see Cleanable#getCleaner()
   */
  @Override
  public Cleaner getCleaner()
  {
    // no need to return a cleaner since MemoryMappedFile already cleans behind
    // itself.
    return null;
  }

}
