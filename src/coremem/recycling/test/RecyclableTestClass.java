package coremem.recycling.test;

import java.util.concurrent.atomic.AtomicBoolean;

import coremem.recycling.RecyclableInterface;
import coremem.recycling.RecyclerInterface;
import coremem.rgc.FreeableBase;

public class RecyclableTestClass extends FreeableBase	implements
																											RecyclableInterface<RecyclableTestClass, LongRequest>
{
	// Proper class fields:
	AtomicBoolean mFree = new AtomicBoolean(false);
	double[] mArray;

	// Recycling related fields:
	private RecyclerInterface<RecyclableTestClass, LongRequest> mRecycler;
	AtomicBoolean mReleased = new AtomicBoolean(false);

	public RecyclableTestClass(LongRequest pParameters)
	{
		recycle(pParameters);
	}

	@Override
	public long getSizeInBytes()
	{
		return 10;
	}

	@Override
	public void free()
	{
		mFree.set(true);
	}

	@Override
	public boolean isFree()
	{
		return mFree.get();
	}

	@Override
	public boolean isCompatible(LongRequest pRequest)
	{
		return mArray.length == pRequest.value;
	}

	@Override
	public void recycle(LongRequest pRequest)
	{
		mArray = new double[(int) (pRequest.value)];
	}

	@Override
	public void setReleased(boolean pIsReleased)
	{
		mReleased.set(pIsReleased);
	}

	@Override
	public boolean isReleased()
	{
		return mReleased.get();
	}

	@Override
	public void release()
	{
		mRecycler.release(this);
	}

	@Override
	public void setRecycler(RecyclerInterface<RecyclableTestClass, LongRequest> pRecycler)
	{
		mRecycler = pRecycler;
	}




}
