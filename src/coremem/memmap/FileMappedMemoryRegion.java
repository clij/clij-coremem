package coremem.memmap;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import coremem.ContiguousMemoryInterface;
import coremem.MappedMemoryBase;
import coremem.enums.MemoryType;
import coremem.exceptions.InvalidNativeMemoryAccessException;
import coremem.exceptions.MemoryMapFileException;
import coremem.exceptions.UnsupportedMemoryResizingException;
import coremem.interfaces.MappableMemory;
import coremem.interfaces.Resizable;
import coremem.interfaces.SizedInBytes;
import coremem.offheap.OffHeapMemory;
import coremem.rgc.Cleaner;
import coremem.rgc.Freeable;

/**
 *
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
   * @param pFile
   * @param pLengthInBytes
   * @return
   * @throws IOException
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
   * @param pFile
   * @param pLengthInBytes
   * @return
   * @throws IOException
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
   * @param pFile
   * @param pLengthInBytes
   * @return
   * @throws IOException
   */
  public FileMappedMemoryRegion openExistingFileMappedMemoryRegion(File pFile,
                                                                   final long pLengthInBytes) throws IOException
  {
    return openExistingFileMappedMemoryRegion(pFile,
                                              0,
                                              pLengthInBytes);
  }

  /**
   * @param pFile
   * @param pPositionInBytes
   * @param pLengthInBytes
   * @return
   * @throws IOException
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
   * @param pFile
   * @param pLengthInBytes
   * @return
   * @throws IOException
   */
  public FileMappedMemoryRegion openReadOnlyExistingFileMappedMemoryRegion(File pFile,
                                                                           final long pLengthInBytes) throws IOException
  {
    return openReadOnlyExistingFileMappedMemoryRegion(pFile,
                                                      0,
                                                      pLengthInBytes);
  }

  /**
   * @param pFile
   * @param pPositionInBytes
   * @param pLengthInBytes
   * @return
   * @throws IOException
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
   * @param pFile
   * @param pLengthInBytes
   * @param pStandardOpenOption
   * @throws IOException
   */
  public FileMappedMemoryRegion(File pFile,
                                final long pLengthInBytes,
                                StandardOpenOption... pStandardOpenOption) throws IOException
  {
    this(pFile, 0, pLengthInBytes, pStandardOpenOption);
  }

  /**
   * @param pFile
   * @param pPositionInBytes
   * @param pLengthInBytes
   * @param pStandardOpenOption
   * @throws IOException
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
   * @param pFileChannel
   * @param pPositionInBytes
   * @param pLengthInBytes
   * @param pStandardOpenOption
   * @throws IOException
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
   * @see coremem.MappedMemoryBase#map()
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
   * @see coremem.interfaces.MappableMemory#force()
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
   * @see coremem.MappedMemoryBase#unmap()
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
   * @see coremem.ContiguousMemoryInterface#subRegion(long, long)
   */
  @Override
  public OffHeapMemory subRegion(long pOffset, long pLenghInBytes)
  {
    if (mAddressInBytes + pOffset + pLenghInBytes > mAddressInBytes
                                                    + mLengthInBytes)
      throw new InvalidNativeMemoryAccessException(String.format("Cannot instanciate OffHeapMemory from FileMappedMemoryRegion on subregion staring at offset %d and length %d  ",
                                                                 pOffset,
                                                                 pLenghInBytes));
    final OffHeapMemory lOffHeapMemory =
                                       new OffHeapMemory(this,
                                                         mAddressInBytes
                                                               + pOffset,
                                                         pLenghInBytes);
    return lOffHeapMemory;
  }

  /* (non-Javadoc)
   * @see coremem.MemoryBase#getMemoryType()
   */
  @Override
  public MemoryType getMemoryType()
  {
    complainIfFreed();
    return MemoryType.FILERAM;
  }

  /* (non-Javadoc)
   * @see coremem.interfaces.Resizable#resize(long)
   */
  @Override
  public long resize(long pNewLength)
  {
    final String lErrorMessage =
                               String.format("Could not resize memory mapped file! ");
    throw new UnsupportedMemoryResizingException(lErrorMessage);
  }

  /* (non-Javadoc)
   * @see coremem.MemoryBase#free()
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
   * @see coremem.rgc.Cleanable#getCleaner()
   */
  @Override
  public Cleaner getCleaner()
  {
    // no need to return a cleaner since MemoryMappedFile already cleans behind
    // itself.
    return null;
  }

}
