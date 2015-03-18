package coremem.fragmented;

import coremem.ContiguousMemoryInterface;
import coremem.interfaces.ReadWriteBytesFileChannel;
import coremem.interfaces.SizedInBytes;
import coremem.rgc.Freeable;

public interface FragmentedMemoryInterface extends
																					Iterable<ContiguousMemoryInterface>,
																					ReadWriteBytesFileChannel,
																					SizedInBytes,
																					Freeable
{

	void add(ContiguousMemoryInterface pPlaneContiguousMemory);

	void remove(ContiguousMemoryInterface pContiguousMemoryInterface);

	int getNumberOfFragments();

	ContiguousMemoryInterface get(int pIndex);


}
