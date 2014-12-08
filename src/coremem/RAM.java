package coremem;

import coremem.interfaces.PointerAccessible;
import coremem.interfaces.ReadAt;
import coremem.interfaces.ReadAtAligned;
import coremem.interfaces.SizedInBytes;
import coremem.interfaces.WriteAt;
import coremem.interfaces.WriteAtAligned;
import coremem.offheap.RAMDirect;
import coremem.rgc.Freeable;

public interface RAM extends
										PointerAccessible,
										ReadAtAligned,
										WriteAtAligned,
										ReadAt,
										WriteAt,
										SizedInBytes,
										Freeable
{

	RAMDirect subRegion(long pOffset, long pLenghInBytes);

}
