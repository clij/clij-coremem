package coremem.offheap.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import coremem.exceptions.InvalidNativeMemoryAccessException;
import coremem.offheap.OffHeapMemoryAccess;

public class OffHeapMemoryAccessTests
{
	final private static long cBufferSize = 2 * (long) Integer.MAX_VALUE;

	@Test
	public void testMaxAllocation()
	{
		int i = 0;
		try
		{
			OffHeapMemoryAccess.setMaximumAllocatableMemory(1000L * 1000L);
			for (; i < 2000; i++)
			{
				OffHeapMemoryAccess.allocateMemory(1000);
			}
			fail();
		}
		catch (OutOfMemoryError e)
		{
			assertTrue(i >= 1000);
		}
		catch (Throwable lE)
		{
			fail();
		}

		OffHeapMemoryAccess.freeAll();
		assertEquals(0, OffHeapMemoryAccess.getTotalAllocatedMemory());

		OffHeapMemoryAccess.setMaximumAllocatableMemory(Long.MAX_VALUE);
	}

	@Test
	public void testAllocateReallocateFree()
	{
		try
		{
			// System.out.println(cBufferSize);
			long lAddress = OffHeapMemoryAccess.allocateMemory(cBufferSize);

			OffHeapMemoryAccess.setByte(lAddress, (byte) 123);
			assertEquals(OffHeapMemoryAccess.getByte(lAddress), (byte) 123);

			long lAddressReallocated = OffHeapMemoryAccess.reallocateMemory(	lAddress,
																																			10);

			OffHeapMemoryAccess.setByte(lAddressReallocated + 9, (byte) 123);
			assertEquals(	OffHeapMemoryAccess.getByte(lAddressReallocated + 9),
										(byte) 123);

			OffHeapMemoryAccess.freeMemory(lAddressReallocated);
		}
		catch (InvalidNativeMemoryAccessException e)
		{
			e.printStackTrace();
			fail();
		}

	}

	@Test
	public void testSuperBig()
	{
		// System.out.println("begin");

		try
		{
			final long lLength = 16L * 1000L * 1000L * 1000L;

			System.out.println("allocateMemory");
			long lAddress = OffHeapMemoryAccess.allocateMemory(lLength);
			System.out.println("lAddress=" + lAddress);
			assertFalse(lAddress == 0);

			/*System.out.println("setMemory");
			OffHeapMemoryAccess.setMemory(lAddress, lLength, (byte) 0);/**/

			System.out.println("setByte(s)");
			for (long i = 0; i < lLength; i += 1000L * 1000L)
			{
				OffHeapMemoryAccess.setByte(lAddress + i, (byte) i);
			}

			System.out.println("getByte(s)");
			for (long i = 0; i < lLength; i += 1000L * 1000L)
			{
				byte lValue = OffHeapMemoryAccess.getByte(lAddress + i);
				assertEquals((byte) i, lValue);
			}

			// Thread.sleep(10000);

			System.out.println("freeMemory");
			OffHeapMemoryAccess.freeMemory(lAddress);

			// System.out.println("end");
		}
		catch (InvalidNativeMemoryAccessException e)
		{
			e.printStackTrace();
			fail();
		}
	}

}
