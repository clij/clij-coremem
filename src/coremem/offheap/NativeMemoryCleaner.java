package coremem.offheap;

import coremem.rgc.Cleaner;

public class NativeMemoryCleaner implements Cleaner
{
	private final Long mAddressToClean;

	public NativeMemoryCleaner(Long pAddress)
	{
		mAddressToClean = pAddress;
	}

	@Override
	public void run()
	{
		if (mAddressToClean == null)
			return;
		if (NativeMemoryAccess.isAllocatedMemory(mAddressToClean))
		{
			System.out.format("cleaning address: %d \n", mAddressToClean);
			NativeMemoryAccess.freeMemory(mAddressToClean);
		}
	}

}
