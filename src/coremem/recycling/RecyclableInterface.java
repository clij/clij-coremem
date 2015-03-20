package coremem.recycling;

import coremem.interfaces.SizedInBytes;
import coremem.rgc.Freeable;

public interface RecyclableInterface<O extends RecyclableInterface<O, P>, P extends RecyclerRequest>	extends
																																																				SizedInBytes,
																																																				Freeable
{

	boolean isCompatible(P pParameters);

	void recycle(P pParameters);

	void setRecycler(RecyclerInterface<O, P> pRecycler);

	void setReleased(boolean pIsReleased);

	boolean isReleased();

	void release();

}
