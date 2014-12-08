package coremem.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import coremem.MemoryRegionInterface;
import coremem.interfaces.Copyable;
import coremem.interfaces.MappableMemory;
import coremem.interfaces.MemoryType;
import coremem.interfaces.RangeCopyable;
import coremem.interfaces.Resizable;
import coremem.offheap.NativeMemoryAccess;

public class RAMTests
{

	public static void testBasics(MemoryRegionInterface pMemoryRegionInterface,
																MemoryType pMemoryType,
																final boolean pResize)
	{
		long lLength = pMemoryRegionInterface.getSizeInBytes();

		if (pMemoryRegionInterface instanceof MappableMemory)
			((MappableMemory) pMemoryRegionInterface).map();

		assertTrue(pMemoryRegionInterface.getAddress() != 0L);

		assertTrue(pMemoryRegionInterface.getMemoryType() == pMemoryType);

		assertFalse(pMemoryRegionInterface.isFree());

		if (pResize)
		{
			if (pMemoryRegionInterface instanceof Resizable)
				((Resizable) pMemoryRegionInterface).resize(lLength / 2);

			assertTrue(pMemoryRegionInterface.getSizeInBytes() == lLength / 2);

			if (pMemoryRegionInterface instanceof Resizable)
				((Resizable) pMemoryRegionInterface).resize(lLength);

			assertTrue(pMemoryRegionInterface.getSizeInBytes() == lLength);
		}

		if (pMemoryRegionInterface instanceof MappableMemory)
			((MappableMemory) pMemoryRegionInterface).unmap();

		pMemoryRegionInterface.free();

	}

	@SuppressWarnings("unchecked")
	public static void testCopySameSize(MemoryRegionInterface pMemoryRegionInterface1,
																			MemoryRegionInterface pMemoryRegionInterface2)
	{
		if (pMemoryRegionInterface1 instanceof Copyable<?>)
		{
			if (pMemoryRegionInterface1 instanceof MappableMemory)
				((MappableMemory) pMemoryRegionInterface1).map();
			if (pMemoryRegionInterface2 instanceof MappableMemory)
				((MappableMemory) pMemoryRegionInterface2).map();

			// OffHeapMemoryRegion lRAMDirect1 = new OffHeapMemoryRegion(1L * Integer.MAX_VALUE);
			// OffHeapMemoryRegion lRAMDirect2 = new OffHeapMemoryRegion(1L * Integer.MAX_VALUE);
			// System.out.println(lRAMDirect1.getLength() / (1024 * 1024));

			NativeMemoryAccess.setByte(	pMemoryRegionInterface1.getAddress(),
																	(byte) 123);

			NativeMemoryAccess.setByte(	pMemoryRegionInterface1.getAddress() + pMemoryRegionInterface1.getSizeInBytes()
																			/ 2,
																	(byte) 456);

			((Copyable<MemoryRegionInterface>) pMemoryRegionInterface1).copyTo(pMemoryRegionInterface2);

			assertEquals(	(byte) 123,
										NativeMemoryAccess.getByte(pMemoryRegionInterface2.getAddress()));
			assertEquals(	(byte) 456,
										NativeMemoryAccess.getByte(pMemoryRegionInterface2.getAddress() + pMemoryRegionInterface1.getSizeInBytes()
																								/ 2));

			pMemoryRegionInterface1.free();
			pMemoryRegionInterface2.free();

			try
			{
				((Copyable<MemoryRegionInterface>) pMemoryRegionInterface1).copyTo(pMemoryRegionInterface2);
				fail();
			}
			catch (Throwable e)
			{
			}

			if (pMemoryRegionInterface1 instanceof MappableMemory)
				((MappableMemory) pMemoryRegionInterface1).unmap();
			if (pMemoryRegionInterface2 instanceof MappableMemory)
				((MappableMemory) pMemoryRegionInterface2).unmap();
		}
	}

	@SuppressWarnings("unchecked")
	public static void testCopyDifferentSize(	MemoryRegionInterface pMemoryRegionInterface1,
																						MemoryRegionInterface pMemoryRegionInterface2)
	{
		// OffHeapMemoryRegion lRAMDirect1 = new OffHeapMemoryRegion(4);
		// OffHeapMemoryRegion lRAMDirect2 = new OffHeapMemoryRegion(8);
		// System.out.println(lRAMDirect1.getLength() / (1024 * 1024));

		if (pMemoryRegionInterface1 instanceof MappableMemory)
			((MappableMemory) pMemoryRegionInterface1).map();
		if (pMemoryRegionInterface2 instanceof MappableMemory)
			((MappableMemory) pMemoryRegionInterface2).map();

		NativeMemoryAccess.setByte(	pMemoryRegionInterface1.getAddress() + 2,
																(byte) 123);

		NativeMemoryAccess.setByte(	pMemoryRegionInterface1.getAddress() + 3,
																(byte) 111);

		if (pMemoryRegionInterface1 instanceof Copyable<?>)
		{
			((RangeCopyable<MemoryRegionInterface>) pMemoryRegionInterface1).copyRangeTo(	2,
																																										pMemoryRegionInterface2,
																																										4,
																																										2);

			assertEquals(	(byte) 123,
										NativeMemoryAccess.getByte(pMemoryRegionInterface2.getAddress() + 4));
			assertEquals(	(byte) 111,
										NativeMemoryAccess.getByte(pMemoryRegionInterface2.getAddress() + 5));

			pMemoryRegionInterface1.free();
			pMemoryRegionInterface2.free();

			try
			{
				((RangeCopyable<MemoryRegionInterface>) pMemoryRegionInterface1).copyRangeTo(	2,
																																											pMemoryRegionInterface2,
																																											4,
																																											2);
				fail();
			}
			catch (Throwable e)
			{
			}
		}

		if (pMemoryRegionInterface1 instanceof MappableMemory)
			((MappableMemory) pMemoryRegionInterface1).unmap();
		if (pMemoryRegionInterface2 instanceof MappableMemory)
			((MappableMemory) pMemoryRegionInterface2).unmap();
	}

	@SuppressWarnings("unchecked")
	public static void testCopyChecks(MemoryRegionInterface pMemoryRegionInterface1,
																		MemoryRegionInterface pMemoryRegionInterface2)
	{
		// OffHeapMemoryRegion lRAMDirect1 = new OffHeapMemoryRegion(4);
		// OffHeapMemoryRegion lRAMDirect2 = new OffHeapMemoryRegion(8);
		if (pMemoryRegionInterface1 instanceof Copyable<?>)
		{
			try
			{
				((RangeCopyable<MemoryRegionInterface>) pMemoryRegionInterface1).copyRangeTo(	2,
																																											pMemoryRegionInterface2,
																																											4,
																																											3);
				fail();
			}
			catch (Throwable e1)
			{
			}

			try
			{
				((RangeCopyable<MemoryRegionInterface>) pMemoryRegionInterface1).copyRangeTo(	2,
																																											pMemoryRegionInterface2,
																																											4,
																																											-2);
				fail();
			}
			catch (Throwable e1)
			{
			}

			try
			{
				((RangeCopyable<MemoryRegionInterface>) pMemoryRegionInterface1).copyRangeTo(	2,
																																											pMemoryRegionInterface1,
																																											4,
																																											9);
				fail();
			}
			catch (Throwable e1)
			{
			}

			pMemoryRegionInterface1.free();
			pMemoryRegionInterface2.free();

			try
			{
				((RangeCopyable<MemoryRegionInterface>) pMemoryRegionInterface1).copyRangeTo(	2,
																																											pMemoryRegionInterface2,
																																											4,
																																											2);
				fail();
			}
			catch (Throwable e)
			{
			}
		}
	}

	public static void testWriteRead(MemoryRegionInterface pMemoryRegionInterface)
	{
		// OffHeapMemoryRegion lRAMDirect = new OffHeapMemoryRegion(4);

		if (pMemoryRegionInterface instanceof MappableMemory)
			((MappableMemory) pMemoryRegionInterface).map();

		pMemoryRegionInterface.setByteAligned(0, (byte) 255);
		pMemoryRegionInterface.setByteAligned(1, (byte) 255);
		pMemoryRegionInterface.setByteAligned(2, (byte) 255);
		pMemoryRegionInterface.setByteAligned(3, (byte) 255);

		assertEquals(	Character.MAX_VALUE,
									pMemoryRegionInterface.getCharAligned(0));
		assertEquals(-1, pMemoryRegionInterface.getShortAligned(0));
		assertEquals(-1, pMemoryRegionInterface.getIntAligned(0));

		assertEquals(	Double.NaN,
									pMemoryRegionInterface.getFloatAligned(0),
									0);

		if (pMemoryRegionInterface instanceof MappableMemory)
			((MappableMemory) pMemoryRegionInterface).unmap();
	}

}
