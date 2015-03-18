package coremem.memmap.test;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import coremem.ContiguousMemoryInterface;
import coremem.interfaces.MemoryType;
import coremem.memmap.FileMappedMemoryRegion;
import coremem.test.ContiguousMemoryTestsHelper;

public class RAMFileTests
{

	private static final long cMemoryRegionSize = 1024;

	@Test
	public void testLargeMemory() throws IOException
	{
		ContiguousMemoryInterface lContiguousMemoryInterface = new FileMappedMemoryRegion(	createTempFile(),
																																1000,
																																(long) (1.1 * Integer.MAX_VALUE));
		ContiguousMemoryTestsHelper.testBasics(lContiguousMemoryInterface,
												MemoryType.FILERAM,
												false);

	}

	@Test
	public void testBasics() throws IOException
	{
		ContiguousMemoryInterface lContiguousMemoryInterface = new FileMappedMemoryRegion(	createTempFile(),
																																1000,
																																cMemoryRegionSize);

		ContiguousMemoryTestsHelper.testBasics(lContiguousMemoryInterface,
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
		ContiguousMemoryInterface lContiguousMemoryInterface1 = new FileMappedMemoryRegion(createTempFile(),
																																1000,
																																cMemoryRegionSize);
		ContiguousMemoryInterface lContiguousMemoryInterface2 = new FileMappedMemoryRegion(createTempFile(),
																																1000,
																																cMemoryRegionSize);

		ContiguousMemoryTestsHelper.testCopySameSize(lContiguousMemoryInterface1,
															lContiguousMemoryInterface2);

	}

	@Test
	public void testCopyDifferentSize() throws IOException
	{
		ContiguousMemoryInterface lContiguousMemoryInterface1 = new FileMappedMemoryRegion(createTempFile(),
																																1,
																																4);
		ContiguousMemoryInterface lContiguousMemoryInterface2 = new FileMappedMemoryRegion(createTempFile(),
																																1,
																																8);

		ContiguousMemoryTestsHelper.testCopyDifferentSize(	lContiguousMemoryInterface1,
																		lContiguousMemoryInterface2);
	}

	@Test
	public void testCopyChecks() throws IOException
	{
		ContiguousMemoryInterface lContiguousMemoryInterface1 = new FileMappedMemoryRegion(createTempFile(),
																																1,
																																4);
		ContiguousMemoryInterface lContiguousMemoryInterface2 = new FileMappedMemoryRegion(createTempFile(),
																																1,
																																8);

		ContiguousMemoryTestsHelper.testCopyChecks(lContiguousMemoryInterface1,
														lContiguousMemoryInterface2);
	}

	@Test
	public void testWriteRead() throws IOException
	{
		ContiguousMemoryInterface lContiguousMemoryInterface = new FileMappedMemoryRegion(	createTempFile(),
																																1,
																																4);

		ContiguousMemoryTestsHelper.testWriteRead(lContiguousMemoryInterface);
	}
}
