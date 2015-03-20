package coremem.recycling;

import java.util.concurrent.TimeUnit;

import coremem.rgc.Freeable;

public interface RecyclerInterface<R extends RecyclableInterface<R, P>, P extends RecyclerRequest>	extends
																																																		Freeable
{
	public abstract long ensurePreallocated(final long pNumberofPrealocatedRecyclablesNeeded,
																					final P pRecyclerRequest);

	public abstract R getOrFail(final P pRecyclerRequest);

	public abstract R getOrWait(final long pWaitTime,
															final TimeUnit pTimeUnit,
															final P pRecyclerRequest);

	public abstract R request(final boolean pWait,
														final long pWaitTime,
														final TimeUnit pTimeUnit,
														final P pRecyclerRequest);




	public abstract long getNumberOfAvailableObjects();

	public abstract long getNumberOfLiveObjects();

	public abstract void release(final R pObject);

	public abstract void clearReleased();

	public abstract void clearLive();

}
