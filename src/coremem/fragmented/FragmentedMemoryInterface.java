package coremem.fragmented;

import java.nio.Buffer;

import coremem.ContiguousMemoryInterface;
import coremem.interfaces.ReadWriteBytesFileChannel;
import coremem.interfaces.SizedInBytes;
import coremem.offheap.OffHeapMemory;
import coremem.rgc.Freeable;

public interface FragmentedMemoryInterface extends
																					Iterable<ContiguousMemoryInterface>,
																					ReadWriteBytesFileChannel,
																					SizedInBytes,
																					Freeable
{

	void add(ContiguousMemoryInterface pPlaneContiguousMemory);
	
	void remove(ContiguousMemoryInterface pContiguousMemoryInterface);

	OffHeapMemory add(Buffer pBuffer);
	
	int getNumberOfFragments();

	ContiguousMemoryInterface get(int pIndex);

	OffHeapMemory makeConsolidatedCopy();




}
