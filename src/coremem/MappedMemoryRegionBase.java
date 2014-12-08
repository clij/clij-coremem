package coremem;

import coremem.interfaces.MappableMemory;

public abstract class MappedMemoryRegionBase<T> extends
																						MemoryRegionBase<T>	implements
																																			MappableMemory

{

	private volatile boolean mIsMapped;

	@Override
	public abstract long map();

	@Override
	public abstract void unmap();

	@Override
	public boolean isCurrentlyMapped()
	{
		return mIsMapped;
	}

	protected void setCurrentlyMapped(boolean pMapped)
	{
		mIsMapped = pMapped;
	}
}
