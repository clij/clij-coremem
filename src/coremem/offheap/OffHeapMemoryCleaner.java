package coremem.offheap;

import java.util.Arrays;

import coremem.rgc.Cleaner;

public class OffHeapMemoryCleaner implements Cleaner
{
	private final Long mAddressToClean;
	private final String mName;
	private final StackTraceElement[] mAllocationStackTrace;
	private final Long mSignature;

	public OffHeapMemoryCleaner(Long pAddress,
															Long pSignature,
															String pName,
															StackTraceElement[] pAllocationStackTrace)
	{
		mAddressToClean = pAddress;
		mSignature = pSignature;
		mName = pName;
		mAllocationStackTrace = pAllocationStackTrace;
	}

	@Override
	public void run()
	{
		if (mAddressToClean == null)
			return;
		if (OffHeapMemoryAccess.isAllocatedMemory(mAddressToClean,
																							mSignature))
		{
			format(	"Cleaning memory: name=%s, address=%s, signature=%d \n",
							mName,
							mAddressToClean,
							mSignature);/**/
			try
			{
				OffHeapMemoryAccess.freeMemory(mAddressToClean);
				format(	"Cleaned successfully memory! name=%s, address=%s, signature=%d \n",
								mName,
								mAddressToClean,
								mSignature);/**/
			}
			catch (final Throwable e)
			{
				e.printStackTrace();
			}
		}
		else if (OffHeapMemoryAccess.getSignature(mAddressToClean) != mSignature)
		{
			format(	"INFO: Attempted to clean memory with wrong signature! name=%s, address=%d, signature=%d \n",
							mName,
							mAddressToClean,
							mSignature);
			format("Stack:\n %s \n", Arrays.toString(mAllocationStackTrace)
																			.replaceAll(", ", "\n"));/**/

		}
		else if (OffHeapMemoryAccess.getSignature(mAddressToClean) == mSignature)
		{
			format(	"INFO: Attempted to clean already freed memory! name=%s, address=%d, signature=%d \n",
							mName,
							mAddressToClean,
							mSignature);
			format("Stack:\n %s \n", Arrays.toString(mAllocationStackTrace)
																			.replaceAll(", ", "\n"));/**/

		}
	}

	public void format(String format, Object... args)
	{
		// System.out.format(format, args);
	}

}
