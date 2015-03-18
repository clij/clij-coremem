package coremem.offheap;

import coremem.rgc.Cleaner;

public class OffHeapMemoryCleaner implements Cleaner
{
	private final Long mAddressToClean;
	private final StackTraceElement[] mAllocationStackTrace;

	public OffHeapMemoryCleaner(Long pAddress,
															StackTraceElement[] pAllocationStackTrace)
	{
		mAddressToClean = pAddress;
		mAllocationStackTrace = pAllocationStackTrace;
	}

	@Override
	public void run()
	{
		if (mAddressToClean == null)
			return;
		if (OffHeapMemoryAccess.isAllocatedMemory(mAddressToClean))
		{
			// System.out.println("Cleaning: " + mAddressToClean);
			/*System.out.format("Cleaning address: %d from:\n %s \n",
												mAddressToClean,
												Arrays.toString(mAllocationStackTrace)
															.replaceAll(", ", "\n"));/**/
			try
			{
				OffHeapMemoryAccess.freeMemory(mAddressToClean);
			}
			catch (final Throwable e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.err.println("Trying to clean unallocated memory! address=" + mAddressToClean);
		}
	}

}
