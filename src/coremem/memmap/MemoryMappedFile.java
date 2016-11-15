package coremem.memmap;

import java.io.IOException;
import java.nio.channels.FileChannel;

import coremem.offheap.OffHeapMemoryAccess;
import coremem.rgc.Cleanable;
import coremem.rgc.Cleaner;

/**
 *
 *
 * @author royer
 */
public class MemoryMappedFile implements AutoCloseable, Cleanable
{

	private final FileChannel mFileChannel;
	private final MemoryMappedFileAccessMode mAccessMode;
	private final boolean mExtendIfNeeded;

	private final long mRequestedFilePosition;
	private final long mRequestedMappedRegionLength;

	private final long mActualMappingFilePosition;
	private final long mActualMappingRegionLength;

	private final long mMappingPointerAddress;

	private final Long mSignature;

	/**
	 * @param pFileChannel
	 * @param pAccessMode
	 * @param pFilePosition
	 * @param pMappedRegionLength
	 * @param pExtendIfNeeded
	 */
	public MemoryMappedFile(FileChannel pFileChannel,
													MemoryMappedFileAccessMode pAccessMode,
													final long pFilePosition,
													final long pMappedRegionLength,
													final boolean pExtendIfNeeded)
	{
		super();
		mFileChannel = pFileChannel;
		mAccessMode = pAccessMode;
		mRequestedFilePosition = pFilePosition;
		mRequestedMappedRegionLength = pMappedRegionLength;
		mExtendIfNeeded = pExtendIfNeeded;

		mActualMappingFilePosition = mRequestedFilePosition - (mRequestedFilePosition % MemoryMappedFileUtils.cAllocationGranularity);
		mActualMappingRegionLength = (mRequestedFilePosition % MemoryMappedFileUtils.cAllocationGranularity) + mRequestedMappedRegionLength;

		mMappingPointerAddress = MemoryMappedFileUtils.map(	mFileChannel,
																												mAccessMode,
																												mActualMappingFilePosition,
																												mActualMappingRegionLength,
																												mExtendIfNeeded);

		mSignature = OffHeapMemoryAccess.getSignature(mMappingPointerAddress);

	}

	/**
	 * @param pFilePosition
	 * @return
	 */
	public long getAddressAtFilePosition(long pFilePosition)
	{
		if (pFilePosition < mRequestedFilePosition)
			throw new IndexOutOfBoundsException("File position index invalid: accessing before the mapped file region");

		if (mRequestedFilePosition + mRequestedMappedRegionLength <= pFilePosition)
			throw new IndexOutOfBoundsException("File position index invalid: accessing after the mapped file region");

		return mMappingPointerAddress + (pFilePosition - mActualMappingFilePosition);
	}

	/* (non-Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() throws IOException
	{
		MemoryMappedFileUtils.unmap(mFileChannel,
																mMappingPointerAddress,
																mActualMappingRegionLength);

	}


	/**
	 *
	 *
	 * @author royer
	 */
	static class MemoryMappedFileCleaner implements Cleaner
	{
		private final long mAddressToClean;
		private final FileChannel mFileChannelToClean;
		private final long mMappedRegionLength;
		private final Long mCleanerSignature;

		public MemoryMappedFileCleaner(	FileChannel pFileChannel,
																		final long pMemoryMapAddress,
																		final long pMappedRegionLength,
																		final Long pSignature)
		{
			mFileChannelToClean = pFileChannel;
			mAddressToClean = pMemoryMapAddress;
			mMappedRegionLength = pMappedRegionLength;
			mCleanerSignature = pSignature;
		}

		@Override
		public void run()
		{
			if (OffHeapMemoryAccess.isAllocatedMemory(mAddressToClean,
																								mCleanerSignature))
			{
				MemoryMappedFileUtils.unmap(mFileChannelToClean,
																		mAddressToClean,
																		mMappedRegionLength);
				format(	"Successfully unmaped memory! channel=%s, address=%s, signature=%d \n",
								mFileChannelToClean,
								mAddressToClean,
								mCleanerSignature);/**/
			}
			else
			{
				format(	"Attempted to unmap already unmapped memory, or memorywith wrong signature! channel=%s, address=%s, signature=%d \n",
								mFileChannelToClean,
								mAddressToClean,
								mCleanerSignature);/**/
			}
		}

		public void format(String format, Object... args)
		{
			System.out.format(format, args);
		}

	}

	/* (non-Javadoc)
	 * @see coremem.rgc.Cleanable#getCleaner()
	 */
	@Override
	public Cleaner getCleaner()
	{
		return new MemoryMappedFileCleaner(	mFileChannel,
																				mMappingPointerAddress,
																				mActualMappingRegionLength,
																				mSignature);
	}

}
