package coremem.offheap.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import org.junit.Test;

import coremem.interfaces.MemoryType;
import coremem.offheap.OffHeapMemoryRegion;
import coremem.test.RAMTests;

public class RAMDirectTests
{

	@Test
	public void testBasics()
	{
		OffHeapMemoryRegion lOffHeapMemoryRegion = new OffHeapMemoryRegion(2L * Integer.MAX_VALUE);

		RAMTests.testBasics(lOffHeapMemoryRegion, MemoryType.CPURAMDIRECT, true);

	}

	@Test
	public void testCopySameSize()
	{
		OffHeapMemoryRegion lOffHeapMemoryRegion1 = new OffHeapMemoryRegion(1L * Integer.MAX_VALUE);
		OffHeapMemoryRegion lOffHeapMemoryRegion2 = new OffHeapMemoryRegion(1L * Integer.MAX_VALUE);

		RAMTests.testCopySameSize(lOffHeapMemoryRegion1, lOffHeapMemoryRegion2);

	}

	@Test
	public void testCopyDifferentSize()
	{
		OffHeapMemoryRegion lOffHeapMemoryRegion1 = new OffHeapMemoryRegion(4);
		OffHeapMemoryRegion lOffHeapMemoryRegion2 = new OffHeapMemoryRegion(8);

		RAMTests.testCopyDifferentSize(lOffHeapMemoryRegion1, lOffHeapMemoryRegion2);
	}

	@Test
	public void testCopyChecks()
	{
		OffHeapMemoryRegion lOffHeapMemoryRegion1 = new OffHeapMemoryRegion(4);
		OffHeapMemoryRegion lOffHeapMemoryRegion2 = new OffHeapMemoryRegion(8);

		RAMTests.testCopyChecks(lOffHeapMemoryRegion1, lOffHeapMemoryRegion2);
	}

	@Test
	public void testWriteRead()
	{
		OffHeapMemoryRegion lOffHeapMemoryRegion = new OffHeapMemoryRegion(4);

		RAMTests.testWriteRead(lOffHeapMemoryRegion);
	}

	@Test
	public void testWriteToReadFromFileChannel() throws IOException
	{
		File lTempFile = File.createTempFile(	this.getClass()
																							.getSimpleName(),
																					"testWriteToReadFromFileChannel");
		lTempFile.delete();
		lTempFile.deleteOnExit();

		FileChannel lFileChannel1 = FileChannel.open(	lTempFile.toPath(),
																									StandardOpenOption.CREATE,
																									StandardOpenOption.WRITE,
																									StandardOpenOption.READ);

		OffHeapMemoryRegion lOffHeapMemoryRegion1 = new OffHeapMemoryRegion(1023);

		for (int i = 0; i < lOffHeapMemoryRegion1.getSizeInBytes(); i++)
			lOffHeapMemoryRegion1.setByte(i, (byte) i);

		lOffHeapMemoryRegion1.writeBytesToFileChannel(lFileChannel1, 512);
		lOffHeapMemoryRegion1.free();
		lFileChannel1.close();

		assertTrue(lTempFile.exists());
		assertEquals(512 + 1023, lTempFile.length());

		FileChannel lFileChannel2 = FileChannel.open(	lTempFile.toPath(),
																									StandardOpenOption.CREATE,
																									StandardOpenOption.READ,
																									StandardOpenOption.WRITE);

		assertEquals(512 + 1023, lFileChannel2.size());

		OffHeapMemoryRegion lOffHeapMemoryRegion2 = new OffHeapMemoryRegion(1023);
		lOffHeapMemoryRegion2.readBytesFromFileChannel(	lFileChannel2,
																					512,
																					lOffHeapMemoryRegion2.getSizeInBytes());

		for (int i = 0; i < lOffHeapMemoryRegion2.getSizeInBytes(); i++)
			assertEquals((byte) i, lOffHeapMemoryRegion2.getByte(i));

		lOffHeapMemoryRegion2.free();
		lFileChannel2.close();
	}
}
