package coremem.memmap;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import coremem.ContiguousMemoryInterface;
import coremem.MappedMemoryBase;
import coremem.exceptions.InvalidNativeMemoryAccessException;
import coremem.exceptions.MemoryMappedFileException;
import coremem.exceptions.UnsupportedMemoryResizingException;
import coremem.interfaces.MappableMemory;
import coremem.interfaces.MemoryType;
import coremem.interfaces.Resizable;
import coremem.interfaces.SizedInBytes;
import coremem.offheap.OffHeapMemory;
import coremem.rgc.Cleaner;
import coremem.rgc.Freeable;

public class FileMappedMemoryRegion extends
 MappedMemoryBase implements
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

	public FileMappedMemoryRegion openExistingFileMappedMemoryRegion(	File pFile,
																																			final long pLengthInBytes) throws IOException
	{
		return openExistingFileMappedMemoryRegion(pFile,
																							0,
																							pLengthInBytes);
	}

	public FileMappedMemoryRegion openExistingFileMappedMemoryRegion(	File pFile,
																																			final long pPositionInBytes,
																																			final long pLengthInBytes) throws IOException
	{
		return new FileMappedMemoryRegion(pFile,
																					pPositionInBytes,
																					pLengthInBytes,
																					StandardOpenOption.READ,
																					StandardOpenOption.WRITE);
	}

	public FileMappedMemoryRegion openReadOnlyExistingFileMappedMemoryRegion(	File pFile,
																																							final long pLengthInBytes) throws IOException
	{
		return openReadOnlyExistingFileMappedMemoryRegion(pFile,
																											0,
																											pLengthInBytes);
	}

	public FileMappedMemoryRegion openReadOnlyExistingFileMappedMemoryRegion(	File pFile,
																																							final long pPositionInBytes,
																																							final long pLengthInBytes) throws IOException
	{
		return new FileMappedMemoryRegion(pFile,
																					pPositionInBytes,
																					pLengthInBytes,
																					StandardOpenOption.READ);
	}

	public FileMappedMemoryRegion(File pFile,
																final long pLengthInBytes,
																StandardOpenOption... pStandardOpenOption) throws IOException
	{
		this(pFile, 0, pLengthInBytes, pStandardOpenOption);
	}

	public FileMappedMemoryRegion(File pFile,
																final long pPositionInBytes,
																final long pLengthInBytes,
																StandardOpenOption... pStandardOpenOption) throws IOException
	{
		this(	FileChannel.open(	pFile.toPath(),
														obtainStandardOptions(pFile,
																									pStandardOpenOption)),
					pPositionInBytes,
					pLengthInBytes,
					pStandardOpenOption);

	}

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

	static StandardOpenOption[] obtainStandardOptions(File pFile,
																										StandardOpenOption... pStandardOpenOption)
	{
		StandardOpenOption[] lStandardOpenOption = pStandardOpenOption;
		if (pStandardOpenOption == null || pStandardOpenOption.length == 0)
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

	@Override
	public long map()
	{
		if (isCurrentlyMapped() && mAddressInBytes != 0)
			return mAddressInBytes;
		try
		{
			mMemoryMappedFile = new MemoryMappedFile(	mFileChannel,
																								MemoryMappedFileUtils.bestMode(mStandardOpenOption),
																								mFilePositionInBytes,
																								mLengthInBytes,
																								mFileChannel.size() < mFilePositionInBytes + mLengthInBytes);
			mAddressInBytes = mMemoryMappedFile.getAddressAtFilePosition(mFilePositionInBytes);
			setCurrentlyMapped(true);
			return mAddressInBytes;
		}
		catch (MemoryMappedFileException | IOException e)
		{
			throw new MemoryMappedFileException("Could not map file channel " + mFileChannel,
																					e);
		}

	}

	@Override
	public void force()
	{
		try
		{
			mFileChannel.force(true);
		}
		catch (final IOException e)
		{
			final String lErrorMessage = String.format("Could not force memory mapping consistency! ");
			throw new MemoryMappedFileException(lErrorMessage, e);
		}
	}

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
			throw new RuntimeException(	"Exception while unmapping " + this.getClass()
																																			.getSimpleName(),
																	e);
		}
		finally
		{
			mMemoryMappedFile = null;
			mAddressInBytes = 0;
			mLengthInBytes = 0;
		}
	}

	@Override
	public OffHeapMemory subRegion(	long pOffset,
																					long pLenghInBytes)
	{
		if (mAddressInBytes + pOffset + pLenghInBytes > mAddressInBytes + mLengthInBytes)
			throw new InvalidNativeMemoryAccessException(String.format(	"Cannot instanciate OffHeapMemory from FileMappedMemoryRegion on subregion staring at offset %d and length %d  ",
																																	pOffset,
																																	pLenghInBytes));
		final OffHeapMemory lOffHeapMemory = new OffHeapMemory(	this,
																																							mAddressInBytes + pOffset,
																																							pLenghInBytes);
		return lOffHeapMemory;
	}

	@Override
	public MemoryType getMemoryType()
	{
		complainIfFreed();
		return MemoryType.FILERAM;
	}

	@Override
	public long resize(long pNewLength)
	{
		final String lErrorMessage = String.format("Could not resize memory mapped file! ");
		// error("KAM", lErrorMessage);
		throw new UnsupportedMemoryResizingException(lErrorMessage);
	}

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
			final String lErrorMessage = String.format("Could not unmap memory mapped file! ");
			throw new MemoryMappedFileException(lErrorMessage, e);
		}

	}

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

	@Override
	public Cleaner getCleaner()
	{
		// no need to return a cleaner since MemoryMappedFile already cleans behind
		// itself.
		return null;
	}

}
