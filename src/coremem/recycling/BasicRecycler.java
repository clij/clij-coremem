package coremem.recycling;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import coremem.exceptions.InvalidAllocationParameterException;
import coremem.rgc.Freeable;
import coremem.rgc.FreeableBase;

public class BasicRecycler<R extends RecyclableInterface<R, P>, P extends RecyclerRequestInterface> extends
																																																		FreeableBase implements
																																																								RecyclerInterface<R, P>,
																																																								Freeable
{
	private final RecyclableFactoryInterface<R, P> mRecyclableFactory;
	private final ArrayBlockingQueue<R> mAvailableObjectsQueue;
	private final ArrayBlockingQueue<R> mLiveObjectsQueue;

	private final AtomicBoolean mIsFreed = new AtomicBoolean(false);
	private final boolean mAutoFree;

	private int mMaxNumberOfLiveObjects, mMaxNumberOfAvailableObjects;

	private final AtomicLong mFailedRequests = new AtomicLong(0);
	private volatile long mAvailableQueueWaitTime = 1;
	private volatile TimeUnit mAvailableQueueTimeUnit = TimeUnit.MICROSECONDS;

	private CopyOnWriteArrayList<RecyclerListenerInterface> mRecyclerListeners = new CopyOnWriteArrayList<>();

	public BasicRecycler(	final RecyclableFactoryInterface<R, P> pRecyclableFactory,
												final int pMaximumNumberOfObjects)
	{
		this(	pRecyclableFactory,
					pMaximumNumberOfObjects / 2,
					pMaximumNumberOfObjects / 2,
					true);
	}

	public BasicRecycler(	final RecyclableFactoryInterface<R, P> pRecyclableFactory,
												final int pMaximumNumberOfLiveObjects,
												final int pMaximumNumberOfAvailableObjects,
												final boolean pAutoFree)
	{
		mMaxNumberOfLiveObjects = pMaximumNumberOfLiveObjects;
		mMaxNumberOfAvailableObjects = pMaximumNumberOfAvailableObjects;
		mAvailableObjectsQueue = new ArrayBlockingQueue<R>(pMaximumNumberOfAvailableObjects);
		mLiveObjectsQueue = new ArrayBlockingQueue<R>(pMaximumNumberOfLiveObjects);
		mRecyclableFactory = pRecyclableFactory;
		mAutoFree = pAutoFree;
	}

	@Override
	public void addListener(RecyclerListenerInterface pRecyclerListener)
	{
		mRecyclerListeners.add(pRecyclerListener);
	}

	@Override
	public void removeListener(RecyclerListenerInterface pRecyclerListener)
	{
		mRecyclerListeners.remove(pRecyclerListener);
	}

	@Override
	public long ensurePreallocated(	final long pNumberofPrealocatedRecyclablesNeeded,
																	final P pRecyclerRequest)
	{
		complainIfFreed();
		final long lNumberOfAvailableObjects = getNumberOfAvailableObjects();
		final long lNumberOfObjectsToAllocate = Math.max(	0,
																											pNumberofPrealocatedRecyclablesNeeded - lNumberOfAvailableObjects);
		long i = 1;
		try
		{
			for (; i <= lNumberOfObjectsToAllocate; i++)
			{

				final R lNewInstance = mRecyclableFactory.create(pRecyclerRequest);
				if (lNewInstance == null)
					return i - 1;

				lNewInstance.setRecycler(this);
				addToAvailableObjectsQueue(lNewInstance);

			}
			return lNumberOfObjectsToAllocate;
		}
		catch (final Throwable e)
		{
			final String lErrorString = "Error while creating new instance!";
			error("Recycling", lErrorString, e);
			return (i - 1);
		}

	}

	@Override
	public R getOrWait(	final long pWaitTime,
											final TimeUnit pTimeUnit,
											final P pRecyclerRequest)
	{
		return request(true, pWaitTime, pTimeUnit, pRecyclerRequest);
	}

	@Override
	public R getOrFail(final P pRecyclerRequest)
	{
		return request(false, 0, TimeUnit.MICROSECONDS, pRecyclerRequest);
	}

	@Override
	public R request(	final boolean pWaitForLiveObjectToComeBack,
										final long pWaitTime,
										final TimeUnit pTimeUnit,
										final P pRecyclerRequest)
	{
		complainIfFreed();

		R lRecyclable;

		try
		{
			lRecyclable = retrieveFromAvailableObjectsQueue();
		}
		catch (final InterruptedException e)
		{
			return request(	pWaitForLiveObjectToComeBack,
											pWaitTime,
											pTimeUnit,
											pRecyclerRequest);
		}

		if (lRecyclable != null)
		{
			// Is this a compatible recyclable?
			if (lRecyclable.isCompatible(pRecyclerRequest))
			{
				// Recycle existing recyclable if compatible:
				lRecyclable.recycle(pRecyclerRequest);
				lRecyclable.setReleased(false);
				return addToLiveObjectQueue(pWaitForLiveObjectToComeBack,
																		pWaitTime,
																		pTimeUnit,
																		lRecyclable);
			}
			else
			{
				// Got unlucky, first we trash this recyclable:
				if (mAutoFree)
					lRecyclable.free();
				// Got unlucky, we try again (eventualy we might find a compatible one
				// or just allocate a new one)
				try
				{
					return request(	pWaitForLiveObjectToComeBack,
													pWaitTime,
													pTimeUnit,
													pRecyclerRequest);
				}
				catch (InvalidAllocationParameterException e)
				{
					// This is to debug a rare but troublesome exception caused by an
					// InvalidAllocationParameterException
					System.err.println(pRecyclerRequest);
					e.printStackTrace();
					return request(	pWaitForLiveObjectToComeBack,
													pWaitTime,
													pTimeUnit,
													pRecyclerRequest);
				}
			}

		}
		else
		{
			// Create new recyclable if there are not too many live objects
			try
			{
				// If we are not allowed, then give up immediately if we don't have
				// space
				// in the live object queue.
				if (!pWaitForLiveObjectToComeBack && mLiveObjectsQueue.remainingCapacity() == 0)
				{
					notifyFailedRequest();
					return null;
				}

				// if we can wait then we wait...
				if (pWaitForLiveObjectToComeBack)
					waitForFreeSpaceInLiveQueue(pWaitTime, pTimeUnit);

				// There is ~maybe~ enough free capacity in the live object queue, we
				// proceed to allocate a new object.
				lRecyclable = mRecyclableFactory.create(pRecyclerRequest);
				lRecyclable.setRecycler(this);
				lRecyclable.setReleased(false);
				return addToLiveObjectQueue(pWaitForLiveObjectToComeBack,
																		pWaitTime,
																		pTimeUnit,
																		lRecyclable);
			}
			catch (final Throwable e)
			{
				final String lErrorString = "Error while creating new instance!";
				error("Recycling", lErrorString, e);
				e.printStackTrace();
				notifyFailedRequest();
				return null;
			}
		}

	}

	private void waitForFreeSpaceInLiveQueue(	long pWaitTime,
																						TimeUnit pTimeUnit)
	{
		final long lStartNanos = System.nanoTime();
		final long lDeadlineNanos = lStartNanos + pTimeUnit.toNanos(pWaitTime);

		while (mLiveObjectsQueue.remainingCapacity() == 0 && System.nanoTime() < lDeadlineNanos)
		{
			try
			{
				Thread.sleep(1);
			}
			catch (final InterruptedException e)
			{
			}
		}
	}

	private void removeFromLiveObjectQueue(final R pRecyclable)
	{
		// System.out.println("removeFromLiveObjectQueue:");
		// System.out.println(mLiveObjectsQueue);

		mLiveObjectsQueue.remove(pRecyclable);
		notifyListeners();
	}

	private R addToLiveObjectQueue(	final boolean pWait,
																	final long pWaitTime,
																	final TimeUnit pTimeUnit,
																	R lRecyclable)
	{
		// System.out.println("addToLiveObjectQueue:");
		// System.out.println(mLiveObjectsQueue);

		if (pWait)
		{
			try
			{
				if (mLiveObjectsQueue.offer(lRecyclable, pWaitTime, pTimeUnit))
				{
					notifyListeners();
					return lRecyclable;
				}
				else
				{
					lRecyclable.setReleased(true);
					addToAvailableObjectsQueue(lRecyclable);
					notifyFailedRequest();
					return null;
				}
			}
			catch (final InterruptedException e)
			{
				return addToLiveObjectQueue(pWait,
																		pWaitTime,
																		pTimeUnit,
																		lRecyclable);
			}
		}
		else
		{
			if (mLiveObjectsQueue.offer(lRecyclable))
			{
				notifyListeners();
				return lRecyclable;
			}
			else
			{
				lRecyclable.setReleased(true);
				addToAvailableObjectsQueue(lRecyclable);
				notifyFailedRequest();
				return null;
			}
		}
	}

	private R retrieveFromAvailableObjectsQueue() throws InterruptedException
	{
		// System.out.println("retrieveFromAvailableObjectsQueue:");
		// System.out.println(mAvailableObjectsQueue);

		R lObject = mAvailableObjectsQueue.poll(mAvailableQueueWaitTime,
																						mAvailableQueueTimeUnit);
		notifyListeners();
		return lObject;
	}

	private void addToAvailableObjectsQueue(R lRecyclable)
	{
		// System.out.println("addToAvailableObjectsQueue:");
		// System.out.println(mAvailableObjectsQueue);

		if (!mAvailableObjectsQueue.offer(lRecyclable))
			if (mAutoFree)
				lRecyclable.free();

		notifyListeners();
	}

	@Override
	public void release(final R pRecyclable)
	{
		complainIfFreed();
		removeFromLiveObjectQueue(pRecyclable);
		addToAvailableObjectsQueue(pRecyclable);
	}

	@Override
	public int getMaxNumberOfLiveObjects()
	{
		return mMaxNumberOfLiveObjects;
	}

	@Override
	public int getMaxNumberOfAvailableObjects()
	{
		return mMaxNumberOfAvailableObjects;
	}

	@Override
	public int getNumberOfLiveObjects()
	{
		return mLiveObjectsQueue.size();
	}

	@Override
	public int getNumberOfAvailableObjects()
	{
		return mAvailableObjectsQueue.size();
	}

	@Override
	public long getNumberOfFailedRequests()
	{
		return mFailedRequests.longValue();
	}

	@Override
	public double computeLiveMemorySizeInBytes()
	{
		long lMemorySizeInBytes = 0;
		for (R lRecyclable : mLiveObjectsQueue)
			lMemorySizeInBytes += lRecyclable.getSizeInBytes();
		return lMemorySizeInBytes;
	}

	@Override
	public double computeAvailableMemorySizeInBytes()
	{
		long lMemorySizeInBytes = 0;
		for (R lRecyclable : mAvailableObjectsQueue)
			lMemorySizeInBytes += lRecyclable.getSizeInBytes();
		return lMemorySizeInBytes;
	}

	@Override
	public void clearReleased()
	{
		R lRecyclable;
		while ((lRecyclable = mAvailableObjectsQueue.poll()) != null)
			if (mAutoFree)
				lRecyclable.free();
		notifyListeners();
	}

	@Override
	public void clearLive()
	{
		R lRecyclable;
		while ((lRecyclable = mLiveObjectsQueue.poll()) != null)
			if (mAutoFree)
				lRecyclable.free();
		notifyListeners();
	}

	@Override
	public void free()
	{
		mIsFreed.set(true);
		clearReleased();
	}

	@Override
	public boolean isFree()
	{
		return mIsFreed.get();
	}

	private void error(String string, String lErrorString)
	{

	}

	private void error(String string, String lErrorString, Throwable e)
	{

	}

	public long getAvailableQueueWaitTime()
	{
		return mAvailableQueueWaitTime;
	}

	public void setAvailableQueueWaitTime(long pAvailableQueueWaitTime)
	{
		mAvailableQueueWaitTime = pAvailableQueueWaitTime;
	}

	public TimeUnit getAvailableQueueTimeUnit()
	{
		return mAvailableQueueTimeUnit;
	}

	public void setAvailableQueueTimeUnit(TimeUnit pAvailableQueueTimeUnit)
	{
		mAvailableQueueTimeUnit = pAvailableQueueTimeUnit;
	}

	private void notifyFailedRequest()
	{
		mFailedRequests.incrementAndGet();
		notifyListeners();
	}

	private void notifyListeners()
	{
		if (mRecyclerListeners.isEmpty())
			return;

		final int lNumberOfLiveObjects = getNumberOfLiveObjects();
		final int lNumberOfAvailableObjects = getNumberOfAvailableObjects();
		final long lNumberOfFailedRequests = mFailedRequests.longValue();

		for (RecyclerListenerInterface lRecyclerListener : mRecyclerListeners)
			lRecyclerListener.update(	lNumberOfLiveObjects,
																lNumberOfAvailableObjects,
																lNumberOfFailedRequests);
	}

	@Override
	public void printDebugInfo()
	{
		System.out.println("getNumberOfAvailableObjects()=" + getNumberOfAvailableObjects());
		System.out.println("getNumberOfLiveObjects()=" + getNumberOfLiveObjects());
	}

}
