package coremem.memmap;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import coremem.MappedMemoryRegionBase;
import coremem.MemoryRegionInterface;
import coremem.exceptions.InvalidNativeMemoryAccessException;
import coremem.exceptions.MemoryMappedFileException;
import coremem.exceptions.UnsupportedMemoryResizingException;
import coremem.interfaces.MappableMemory;
import coremem.interfaces.MemoryType;
import coremem.interfaces.Resizable;
import coremem.interfaces.SizedInBytes;
import coremem.offheap.OffHeapMemoryRegion;
import coremem.rgc.Cleaner;
import coremem.rgc.Freeable;

public class FileMappedMemoryRegion<T>	extends
																				MappedMemoryRegionBase<T>	implements
																																	MappableMemory,
																																	Resizable,
																																	SizedInBytes,
																																	MemoryRegionInterface<T>,
																																	Freeable

{

	private FileChannel mFileChannel;
	private StandardOpenOption[] mStandardOpenOption;
	private long mFilePositionInBytes;
	private MemoryMappedFile mMemoryMappedFile;

	public FileMappedMemoryRegion<T> createNewFileMappedMemoryRegion(	File pFile,
																																		final long pLengthInBytes) throws IOException
	{
		return new FileMappedMemoryRegion<T>(	pFile,
																					0,
																					pLengthInBytes,
																					StandardOpenOption.CREATE_NEW,
																					StandardOpenOption.READ,
																					StandardOpenOption.WRITE);
	}

	public FileMappedMemoryRegion<T> createNewSparseFileMappedMemoryRegion(	File pFile,
																																					final long pLengthInBytes) throws IOException
	{
		return new FileMappedMemoryRegion<T>(	pFile,
																					0,
																					pLengthInBytes,
																					StandardOpenOption.CREATE_NEW,
																					StandardOpenOption.READ,
																					StandardOpenOption.WRITE,
																					StandardOpenOption.SPARSE);
	}

	public FileMappedMemoryRegion<T> openExistingFileMappedMemoryRegion(File pFile,
																																			final long pLengthInBytes) throws IOException
	{
		return openExistingFileMappedMemoryRegion(pFile,
																							0,
																							pLengthInBytes);
	}

	public FileMappedMemoryRegion<T> openExistingFileMappedMemoryRegion(File pFile,
																																			final long pPositionInBytes,
																																			final long pLengthInBytes) throws IOException
	{
		return new FileMappedMemoryRegion<T>(	pFile,
																					pPositionInBytes,
																					pLengthInBytes,
																					StandardOpenOption.READ,
																					StandardOpenOption.WRITE);
	}

	public FileMappedMemoryRegion<T> openReadOnlyExistingFileMappedMemoryRegion(File pFile,
																																							final long pLengthInBytes) throws IOException
	{
		return openReadOnlyExistingFileMappedMemoryRegion(pFile,
																											0,
																											pLengthInBytes);
	}

	public FileMappedMemoryRegion<T> openReadOnlyExistingFileMappedMemoryRegion(File pFile,
																																							final long pPositionInBytes,
																																							final long pLengthInBytes) throws IOException
	{
		return new FileMappedMemoryRegion<T>(	pFile,
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
			throw new MemoryMappedFileException(e);
		}

	}

	@Override
	public void force()
	{
		try
		{
			mFileChannel.force(true);
		}
		catch (IOException e)
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
		catch (IOException e)
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
	public OffHeapMemoryRegion<T> subRegion(long pOffset,
																					long pLenghInBytes)
	{
		if (mAddressInBytes + pOffset + pLenghInBytes > mAddressInBytes + mLengthInBytes)
			throw new InvalidNativeMemoryAccessException(String.format(	"Cannot instanciate OffHeapMemoryRegion from FileMappedMemoryRegion on subregion staring at offset %d and length %d  ",
																																	pOffset,
																																	pLenghInBytes));
		OffHeapMemoryRegion<T> lOffHeapMemoryRegion = new OffHeapMemoryRegion<T>(	this,
																																							mAddressInBytes + pOffset,
																																							pLenghInBytes);
		return lOffHeapMemoryRegion;
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
		catch (Throwable e)
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
