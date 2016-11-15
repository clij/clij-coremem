package coremem.recycling;

import java.util.concurrent.TimeUnit;

import coremem.rgc.Freeable;

/**
 *
 *
 * @param <R>
 * @param <P>
 * @author royer
 */
public interface RecyclerInterface<R extends RecyclableInterface<R, P>, P extends RecyclerRequestInterface> extends
																																																						Freeable
{

	/**
	 * @param pNumberofPrealocatedRecyclablesNeeded
	 * @param pRecyclerRequest
	 * @return
	 */
	public abstract long ensurePreallocated(final long pNumberofPrealocatedRecyclablesNeeded,
																					final P pRecyclerRequest);

	/**
	 * @param pRecyclerRequest
	 * @return
	 */
	public abstract R getOrFail(final P pRecyclerRequest);

	/**
	 * @param pWaitTime
	 * @param pTimeUnit
	 * @param pRecyclerRequest
	 * @return
	 */
	public abstract R getOrWait(final long pWaitTime,
															final TimeUnit pTimeUnit,
															final P pRecyclerRequest);

	/**
	 * @param pWaitForLiveRecyclablesToComeBack
	 * @param pWaitTime
	 * @param pTimeUnit
	 * @param pRecyclerRequest
	 * @return
	 */
	public abstract R request(final boolean pWaitForLiveRecyclablesToComeBack,
														final long pWaitTime,
														final TimeUnit pTimeUnit,
														final P pRecyclerRequest);

	/**
	 * @return
	 */
	public abstract int getMaxNumberOfLiveObjects();

	/**
	 * @return
	 */
	public abstract int getNumberOfLiveObjects();

	/**
	 * @return
	 */
	public abstract int getMaxNumberOfAvailableObjects();

	/**
	 * @return
	 */
	public abstract int getNumberOfAvailableObjects();
	
	/**
	 * @return
	 */
	public abstract long getNumberOfFailedRequests();
	
	/**
	 * @return
	 */
	public abstract double computeLiveMemorySizeInBytes();
	
	/**
	 * @return
	 */
	public abstract double computeAvailableMemorySizeInBytes();

	/**
	 * @param pObject
	 */
	public abstract void release(final R pObject);

	/**
	 * 
	 */
	public abstract void clearReleased();

	/**
	 * 
	 */
	public abstract void clearLive();

	/**
	 * @param pRecyclerListener
	 */
	public abstract void addListener(RecyclerListenerInterface pRecyclerListener);

	/**
	 * @param pRecyclerListener
	 */
	public abstract void removeListener(RecyclerListenerInterface pRecyclerListener);

	/**
	 * 
	 */
	public abstract void printDebugInfo();





}
