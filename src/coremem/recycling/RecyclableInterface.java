package coremem.recycling;

import coremem.interfaces.SizedInBytes;
import coremem.rgc.Freeable;

public interface RecyclableInterface<O extends RecyclableInterface<O, P>, P extends RecyclerRequest<O>> extends
																																																				SizedInBytes,
																																																				Freeable
{

	@SuppressWarnings("unchecked")
	boolean isCompatible(P pParameters);

	@SuppressWarnings("unchecked")
	void initialize(P pParameters);

	void setRecycler(Recycler<O, P> pRecycler);

	void setReleased(boolean pIsReleased);

}
