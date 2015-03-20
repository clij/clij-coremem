package coremem.fragmented;

import static java.lang.Math.min;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;

import coremem.ContiguousMemoryInterface;
import coremem.exceptions.InvalidFragmentedMemoryStateException;
import coremem.offheap.OffHeapMemory;
import coremem.rgc.FreeableBase;

public class FragmentedMemory extends FreeableBase implements
																									FragmentedMemoryInterface

{

	private final ArrayList<ContiguousMemoryInterface> mMemoryRegionList = new ArrayList<ContiguousMemoryInterface>();
	private long mTotalSizeInBytes;

	public static FragmentedMemory split(	ContiguousMemoryInterface pContiguousMemoryInterface,
																				int pNumberOfFragments)
	{
		long lAddress = pContiguousMemoryInterface.getAddress();
		final long lSizeInBytes = pContiguousMemoryInterface.getSizeInBytes();
		final long lFragmentSizeInBytes = (lSizeInBytes / pNumberOfFragments);

		final FragmentedMemory lFragmentedMemory = new FragmentedMemory();
		long lLeftToBeAssignedSizeInBytes = lSizeInBytes;
		OffHeapMemory lOffHeapMemory;
		for (int i = 0; i < pNumberOfFragments; i++)
		{
			long lEffectiveFragmentSizeInBytes;
			if (i == pNumberOfFragments - 1)
				lEffectiveFragmentSizeInBytes = lLeftToBeAssignedSizeInBytes;
			else
				lEffectiveFragmentSizeInBytes = lFragmentSizeInBytes;

			lOffHeapMemory = OffHeapMemory.wrapPointer(	"FragmentOf" + pContiguousMemoryInterface,
																									pContiguousMemoryInterface,
																									lAddress,
																									lEffectiveFragmentSizeInBytes);
			lAddress += lFragmentSizeInBytes;
			lLeftToBeAssignedSizeInBytes -= lFragmentSizeInBytes;
			lFragmentedMemory.add(lOffHeapMemory);
		}

		return lFragmentedMemory;
	}

	public static FragmentedMemoryInterface wrap(ContiguousMemoryInterface... pContiguousMemoryInterfaces)
	{
		final FragmentedMemory lFragmentedMemory = new FragmentedMemory();
		for (final ContiguousMemoryInterface lContiguousMemoryInterface : pContiguousMemoryInterfaces)
			lFragmentedMemory.add(lContiguousMemoryInterface);
		return lFragmentedMemory;
	}

	public FragmentedMemory()
	{
		super();
	}

	@Override
	public ContiguousMemoryInterface get(int pIndex)
	{
		return mMemoryRegionList.get(pIndex);
	}

	@Override
	public int getNumberOfFragments()
	{
		return mMemoryRegionList.size();
	}

	@Override
	public void add(ContiguousMemoryInterface pContiguousMemoryInterface)
	{
		mMemoryRegionList.add(pContiguousMemoryInterface);
		mTotalSizeInBytes += pContiguousMemoryInterface.getSizeInBytes();
	}

	@Override
	public void remove(ContiguousMemoryInterface pContiguousMemoryInterface)
	{
		mMemoryRegionList.remove(pContiguousMemoryInterface);
		mTotalSizeInBytes -= pContiguousMemoryInterface.getSizeInBytes();
	}

	@Override
	public long writeBytesToFileChannel(FileChannel pFileChannel,
																			long pFilePositionInBytes) throws IOException
	{
		return writeBytesToFileChannel(	0,
																		pFileChannel,
																		pFilePositionInBytes,
																		getSizeInBytes());
	}

	@Override
	public long writeBytesToFileChannel(long pBufferPositionInBytes,
																			FileChannel pFileChannel,
																			long pFilePositionInBytes,
																			long pLengthInBytes) throws IOException
	{
		complainIfFreed();
		long lBytesWritten = 0;
		long lCurrentFilePosition = pFilePositionInBytes;
		for (final ContiguousMemoryInterface lContiguousMemoryInterface : mMemoryRegionList)
		{
			final long lBytesLeftToBeWritten = pLengthInBytes - lBytesWritten;
			if (lBytesLeftToBeWritten <= 0)
				break;
			final long lBytesToBeWritten = min(	lContiguousMemoryInterface.getSizeInBytes(),
																					lBytesLeftToBeWritten);
			lCurrentFilePosition = lContiguousMemoryInterface.writeBytesToFileChannel(0,
																																								pFileChannel,
																																								lCurrentFilePosition,
																																								lBytesToBeWritten);
			lBytesWritten += lBytesToBeWritten;
		}
		return lCurrentFilePosition;
	}

	@Override
	public long readBytesFromFileChannel(	FileChannel pFileChannel,
																				long pFilePositionInBytes,
																				long pLengthInBytes) throws IOException
	{
		return readBytesFromFileChannel(0,
																		pFileChannel,
																		pFilePositionInBytes,
																		getSizeInBytes());
	}

	@Override
	public long readBytesFromFileChannel(	long pBufferPositionInBytes,
																				FileChannel pFileChannel,
																				long pFilePositionInBytes,
																				long pLengthInBytes) throws IOException
	{
		complainIfFreed();
		long lBytesRead = 0;
		long lCurrentFilePosition = pFilePositionInBytes;
		for (final ContiguousMemoryInterface lContiguousMemoryInterface : mMemoryRegionList)
		{
			final long lBytesLeftToBeRead = pLengthInBytes - lBytesRead;
			if (lBytesLeftToBeRead <= 0)
				break;
			final long lBytesToReadNow = min(	lContiguousMemoryInterface.getSizeInBytes(),
																				lBytesLeftToBeRead);
			lCurrentFilePosition = lContiguousMemoryInterface.readBytesFromFileChannel(	0,
																																									pFileChannel,
																																									lCurrentFilePosition,
																																									lBytesToReadNow);
			lBytesRead += lBytesToReadNow;
		}
		return lCurrentFilePosition;

	}

	@Override
	public long getSizeInBytes()
	{
		complainIfFreed();
		return mTotalSizeInBytes;
	}

	@Override
	public void free()
	{
		for (final ContiguousMemoryInterface lContiguousMemoryInterface : mMemoryRegionList)
			lContiguousMemoryInterface.free();
	}

	@SuppressWarnings("null")
	@Override
	public boolean isFree()
	{
		if (mMemoryRegionList.isEmpty())
			return false;

		Boolean lIsFree = null;
		for (final ContiguousMemoryInterface lContiguousMemoryInterface : mMemoryRegionList)
		{
			final boolean lLocalIsFree = lContiguousMemoryInterface.isFree();
			if (lIsFree == null)
				lIsFree = lLocalIsFree;
			else if (lIsFree != lLocalIsFree)
				throw new InvalidFragmentedMemoryStateException("Some contiguous memory blocks are freed and others not!");
		}

		return lIsFree;
	}

	@Override
	public Iterator<ContiguousMemoryInterface> iterator()
	{
		return mMemoryRegionList.iterator();
	}

}
