package coremem.recycling;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import coremem.rgc.Freeable;
import coremem.rgc.FreeableBase;

public class BasicRecycler<R extends RecyclableInterface<R, P>, P extends RecyclerRequest>	extends
																																														FreeableBase implements
																																																				RecyclerInterface<R, P>,
																																																				Freeable
{
	private final RecyclableFactory<R, P> mRecyclableFactory;
	private final ArrayBlockingQueue<R> mAvailableObjectsQueue;
	private final ArrayBlockingQueue<R> mLiveObjectsQueue;

	private final AtomicBoolean mIsFreed = new AtomicBoolean(false);
	private final boolean mAutoFree;

	private volatile long mAvailableQueueWaitTime = 1;
	private volatile TimeUnit mAvailableQueueTimeUnit = TimeUnit.MICROSECONDS;

	public BasicRecycler(	final RecyclableFactory<R, P> pRecyclableFactory,
												final int pMaximumNumberOfObjects)
	{
		this(	pRecyclableFactory,
					pMaximumNumberOfObjects / 2,
					pMaximumNumberOfObjects / 2,
					true);
	}

	public BasicRecycler(	final RecyclableFactory<R, P> pRecyclableFactory,
												final int pMaximumNumberOfAvailableObjects,
												final int pMaximumNumberOfLiveObjects,
												final boolean pAutoFree)
	{
		mAvailableObjectsQueue = new ArrayBlockingQueue<R>(pMaximumNumberOfAvailableObjects);
		mLiveObjectsQueue = new ArrayBlockingQueue<R>(pMaximumNumberOfLiveObjects);
		mRecyclableFactory = pRecyclableFactory;
		mAutoFree = pAutoFree;
	}

	@Override
	public long ensurePreallocated(	final long pNumberofPrealocatedRecyclablesNeeded,
																	final P pRecyclerRequest)
	{
		complainIfFreed();
		final long lNumberOfAvailableObjects = mAvailableObjectsQueue.size();
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
				mAvailableObjectsQueue.add(lNewInstance);

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
			lRecyclable = mAvailableObjectsQueue.poll(mAvailableQueueWaitTime,
																								mAvailableQueueTimeUnit);
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
			// Recycle existing recyclable:
			lRecyclable.recycle(pRecyclerRequest);
			lRecyclable.setReleased(false);
			return addToLiveObjectQueue(pWaitForLiveObjectToComeBack,
																	pWaitTime,
																	pTimeUnit,
																	lRecyclable);

		}
		else
		{
			// Create new recyclable if there are not too many live objects
			try
			{
				// If we are not allowed then give up immediately if we don't have space
				// in the live object queue.
				if (!pWaitForLiveObjectToComeBack && mLiveObjectsQueue.remainingCapacity() == 0)
					return null;

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

	private R addToLiveObjectQueue(	final boolean pWait,
																	final long pWaitTime,
																	final TimeUnit pTimeUnit,
																	R lRecyclable)
	{
		if (pWait)
		{
			try
			{
				if (mLiveObjectsQueue.offer(lRecyclable, pWaitTime, pTimeUnit))
				{
					return lRecyclable;
				}
				else
				{
					lRecyclable.setReleased(true);
					addToAvailableRecyclableQueue(lRecyclable);
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
				return lRecyclable;
			}
			else
			{
				lRecyclable.setReleased(true);
				addToAvailableRecyclableQueue(lRecyclable);
				return null;
			}
		}
	}

	private void addToAvailableRecyclableQueue(R lRecyclable)
	{
		if (!mAvailableObjectsQueue.offer(lRecyclable))
			if (mAutoFree)
				lRecyclable.free();
	}

	@Override
	public void release(final R pRecyclable)
	{
		complainIfFreed();
		mLiveObjectsQueue.remove(pRecyclable);
		addToAvailableRecyclableQueue(pRecyclable);
	}

	@Override
	public long getNumberOfAvailableObjects()
	{
		return mAvailableObjectsQueue.size();
	}

	@Override
	public long getNumberOfLiveObjects()
	{
		return mLiveObjectsQueue.size();
	}

	@Override
	public void clearReleased()
	{
		R lRecyclable;
		while ((lRecyclable = mAvailableObjectsQueue.poll()) != null)
			if (mAutoFree)
				lRecyclable.free();
	}

	@Override
	public void clearLive()
	{
		R lRecyclable;
		while ((lRecyclable = mLiveObjectsQueue.poll()) != null)
			if (mAutoFree)
				lRecyclable.free();
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

}
