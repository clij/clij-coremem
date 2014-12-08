package coremem.memmap.test;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import coremem.MemoryRegionInterface;
import coremem.interfaces.MemoryType;
import coremem.memmap.FileMappedMemoryRegion;
import coremem.test.RAMTests;

public class RAMFileTests
{

	private static final long cMemoryRegionSize = 1024;

	@Test
	public void testLargeMemory() throws IOException
	{
		MemoryRegionInterface lMemoryRegionInterface = new FileMappedMemoryRegion(	createTempFile(),
																																1000,
																																(long) (1.1 * Integer.MAX_VALUE));
		RAMTests.testBasics(lMemoryRegionInterface,
												MemoryType.FILERAM,
												false);

	}

	@Test
	public void testBasics() throws IOException
	{
		MemoryRegionInterface lMemoryRegionInterface = new FileMappedMemoryRegion(	createTempFile(),
																																1000,
																																cMemoryRegionSize);

		RAMTests.testBasics(lMemoryRegionInterface,
												MemoryType.FILERAM,
												false);
	}

	private File createTempFile() throws IOException
	{
		File lCreateTempFile = File.createTempFile(	RAMFileTests.class.toString(),
																								"" + Math.random());
		lCreateTempFile.deleteOnExit();
		return lCreateTempFile;
	}

	@Test
	public void testCopySameSize() throws IOException
	{
		MemoryRegionInterface lMemoryRegionInterface1 = new FileMappedMemoryRegion(createTempFile(),
																																1000,
																																cMemoryRegionSize);
		MemoryRegionInterface lMemoryRegionInterface2 = new FileMappedMemoryRegion(createTempFile(),
																																1000,
																																cMemoryRegionSize);

		RAMTests.testCopySameSize(lMemoryRegionInterface1,
															lMemoryRegionInterface2);

	}

	@Test
	public void testCopyDifferentSize() throws IOException
	{
		MemoryRegionInterface lMemoryRegionInterface1 = new FileMappedMemoryRegion(createTempFile(),
																																1,
																																4);
		MemoryRegionInterface lMemoryRegionInterface2 = new FileMappedMemoryRegion(createTempFile(),
																																1,
																																8);

		RAMTests.testCopyDifferentSize(	lMemoryRegionInterface1,
																		lMemoryRegionInterface2);
	}

	@Test
	public void testCopyChecks() throws IOException
	{
		MemoryRegionInterface lMemoryRegionInterface1 = new FileMappedMemoryRegion(createTempFile(),
																																1,
																																4);
		MemoryRegionInterface lMemoryRegionInterface2 = new FileMappedMemoryRegion(createTempFile(),
																																1,
																																8);

		RAMTests.testCopyChecks(lMemoryRegionInterface1,
														lMemoryRegionInterface2);
	}

	@Test
	public void testWriteRead() throws IOException
	{
		MemoryRegionInterface lMemoryRegionInterface = new FileMappedMemoryRegion(	createTempFile(),
																																1,
																																4);

		RAMTests.testWriteRead(lMemoryRegionInterface);
	}
}
