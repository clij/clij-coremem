package coremem.offheap;

import coremem.MemoryRegionBase;
import coremem.MemoryRegionInterface;
import coremem.exceptions.InvalidNativeMemoryAccessException;
import coremem.exceptions.UnsupportedMemoryResizingException;
import coremem.interfaces.MemoryType;
import coremem.interfaces.PointerAccessible;
import coremem.interfaces.Resizable;
import coremem.interfaces.SizedInBytes;
import coremem.rgc.Cleaner;
import coremem.rgc.Freeable;
import coremem.rgc.RessourceGarbageCollector;

public class OffHeapMemoryRegion<T> extends MemoryRegionBase<T>	implements
																																PointerAccessible,
																																Resizable,
																																SizedInBytes,
																																MemoryRegionInterface<T>,
																																Freeable

{
	protected Object mParent = null;

	public static final <T> OffHeapMemoryRegion<T> wrapPointer(	final Object pParent,
																											final long pAddress,
																											final long pLengthInBytes)
	{
		return new OffHeapMemoryRegion<T>(pParent,
																			pAddress,
																			pLengthInBytes);
	};

	public static <T> OffHeapMemoryRegion<T> allocate(long pLengthInBytes)
	{
		return new OffHeapMemoryRegion<>(pLengthInBytes);
	}

	public OffHeapMemoryRegion(	final Object pParent,
															final long pAddress,
															final long pLengthInBytes)
	{
		super();
		mParent = pParent;
		mAddressInBytes = pAddress;
		mLengthInBytes = pLengthInBytes;
		if (pParent != null)
			RessourceGarbageCollector.register(this);
	}

	public OffHeapMemoryRegion(final long pLengthInBytes)
	{
		this(	null,
					NativeMemoryAccess.allocateMemory(pLengthInBytes),
					pLengthInBytes);
	}

	@Override
	public OffHeapMemoryRegion<T> subRegion(final long pOffset,
																				final long pLenghInBytes)
	{
		if (mAddressInBytes + pOffset + pLenghInBytes > mAddressInBytes + mLengthInBytes)
			throw new InvalidNativeMemoryAccessException(String.format(	"Cannot instanciate OffHeapMemoryRegion on subregion staring at offset %d and length %d  ",
																																	pOffset,
																																	pLenghInBytes));
		OffHeapMemoryRegion<T> lOffHeapMemoryRegion = new OffHeapMemoryRegion<T>(	this,
																																				mAddressInBytes + pOffset,
																																				pLenghInBytes);
		return lOffHeapMemoryRegion;
	}

	@Override
	public MemoryType getMemoryType()
	{
		complainIfFreed();
		return MemoryType.CPURAMDIRECT;
	}

	@Override
	public long resize(long pNewLength)
	{
		complainIfFreed();
		if (mParent != null)
			throw new UnsupportedMemoryResizingException("Cannot resize externally allocated memory region!");
		try
		{
			mAddressInBytes = NativeMemoryAccess.reallocateMemory(mAddressInBytes,
																														pNewLength);
			mLengthInBytes = pNewLength;
		}
		catch (Throwable e)
		{
			final String lErrorMessage = String.format(	"Could not resize memory region from %d to %d ",
																									mLengthInBytes,
																									pNewLength);
			// error("KAM", lErrorMessage);
			throw new UnsupportedMemoryResizingException(lErrorMessage, e);
		}
		return mLengthInBytes;
	}

	@Override
	public void free()
	{
		if (mParent == null && mAddressInBytes != 0)
		{
			NativeMemoryAccess.freeMemory(mAddressInBytes);
		}
		mAddressInBytes = 0;
		mParent = null;
		super.free();
	}

	@Override
	public Cleaner getCleaner()
	{
		if (mParent != null)
			return new NativeMemoryCleaner(null);
		return new NativeMemoryCleaner(mAddressInBytes);
	}



	@Override
	public String toString()
	{
		return "OffHeapMemoryRegion [mParent=" + mParent
						+ ", mAddressInBytes="
						+ mAddressInBytes
						+ ", mLengthInBytes="
						+ mLengthInBytes
						+ ", mIsFree="
						+ mIsFree
						+ ", getMemoryType()="
						+ getMemoryType()
						+ "]";
	}

}
