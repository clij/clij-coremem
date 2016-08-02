package coremem.recycling;

import java.util.concurrent.TimeUnit;

import coremem.rgc.Freeable;

public interface RecyclerInterface<R extends RecyclableInterface<R, P>, P extends RecyclerRequestInterface> extends
																																																						Freeable
{

	public abstract long ensurePreallocated(final long pNumberofPrealocatedRecyclablesNeeded,
																					final P pRecyclerRequest);

	public abstract R getOrFail(final P pRecyclerRequest);

	public abstract R getOrWait(final long pWaitTime,
															final TimeUnit pTimeUnit,
															final P pRecyclerRequest);

	public abstract R request(final boolean pWaitForLiveRecyclablesToComeBack,
														final long pWaitTime,
														final TimeUnit pTimeUnit,
														final P pRecyclerRequest);

	public abstract int getMaxNumberOfLiveObjects();

	public abstract int getNumberOfLiveObjects();

	public abstract int getMaxNumberOfAvailableObjects();

	public abstract int getNumberOfAvailableObjects();
	
	public abstract long getNumberOfFailedRequests();
	
	public abstract double computeLiveMemorySizeInBytes();
	
	public abstract double computeAvailableMemorySizeInBytes();

	public abstract void release(final R pObject);

	public abstract void clearReleased();

	public abstract void clearLive();

	public abstract void addListener(RecyclerListenerInterface pRecyclerListener);

	public abstract void removeListener(RecyclerListenerInterface pRecyclerListener);

	public abstract void printDebugInfo();





}
