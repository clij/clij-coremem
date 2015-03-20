package coremem.offheap;

import java.nio.Buffer;

import coremem.ContiguousMemoryInterface;
import coremem.MemoryBase;
import coremem.exceptions.InvalidNativeMemoryAccessException;
import coremem.exceptions.UnsupportedMemoryResizingException;
import coremem.interfaces.MemoryType;
import coremem.interfaces.Resizable;
import coremem.interop.NIOBuffersInterop;
import coremem.rgc.Cleaner;
import coremem.rgc.RessourceGarbageCollector;
import coremem.util.Size;

public class OffHeapMemory extends MemoryBase	implements
																							Resizable,
																							ContiguousMemoryInterface

{
	protected StackTraceElement[] mAllocationStackTrace = new StackTraceElement[]
	{ new StackTraceElement("NULL", "NULL", "NULL", -1) };
	protected String mName = "NOTDEFINED";
	protected Long mSignature;
	protected Object mParent = null;

	public static final OffHeapMemory wrapPointer(final Object pParent,
																								final long pAddress,
																								final long pLengthInBytes)
	{
		return wrapPointer("WRAPNULL", pParent, pAddress, pLengthInBytes);
	};

	public static final OffHeapMemory wrapPointer(final String pName,
																								final Object pParent,
																								final long pAddress,
																								final long pLengthInBytes)
	{
		return new OffHeapMemory(pName, pParent, pAddress, pLengthInBytes);
	};

	public static final OffHeapMemory wrapBuffer(final Buffer pBuffer)
	{
		return NIOBuffersInterop.getContiguousMemoryFrom(pBuffer);
	};

	public static OffHeapMemory allocateBytes(long pNumberOfBytes)
	{
		return new OffHeapMemory(pNumberOfBytes * Size.BYTE);
	}

	public static OffHeapMemory allocateChars(long pNumberOfChars)
	{
		return new OffHeapMemory(pNumberOfChars * Size.CHAR);
	}

	public static OffHeapMemory allocateShorts(long pNumberOfShorts)
	{
		return new OffHeapMemory(pNumberOfShorts * Size.SHORT);
	}

	public static OffHeapMemory allocateInts(long pNumberOfInts)
	{
		return new OffHeapMemory(pNumberOfInts * Size.INT);
	}

	public static OffHeapMemory allocateLongs(long pNumberOfLongs)
	{
		return new OffHeapMemory(pNumberOfLongs * Size.LONG);
	}

	public static OffHeapMemory allocateFloats(long pNumberOfFloats)
	{
		return new OffHeapMemory(pNumberOfFloats * Size.FLOAT);
	}

	public static OffHeapMemory allocateDoubles(long pNumberOfDoubles)
	{
		return new OffHeapMemory(pNumberOfDoubles * Size.DOUBLE);
	}

	public static OffHeapMemory allocateBytes(String pName,
																						long pNumberOfBytes)
	{
		return new OffHeapMemory(pName, pNumberOfBytes * Size.BYTE);
	}

	public static OffHeapMemory allocateChars(String pName,
																						long pNumberOfChars)
	{
		return new OffHeapMemory(pName, pNumberOfChars * Size.CHAR);
	}

	public static OffHeapMemory allocateShorts(	String pName,
																							long pNumberOfShorts)
	{
		return new OffHeapMemory(pName, pNumberOfShorts * Size.SHORT);
	}

	public static OffHeapMemory allocateInts(	String pName,
																						long pNumberOfInts)
	{
		return new OffHeapMemory(pName, pNumberOfInts * Size.INT);
	}

	public static OffHeapMemory allocateLongs(String pName,
																						long pNumberOfLongs)
	{
		return new OffHeapMemory(pName, pNumberOfLongs * Size.LONG);
	}

	public static OffHeapMemory allocateFloats(	String pName,
																							long pNumberOfFloats)
	{
		return new OffHeapMemory(pName, pNumberOfFloats * Size.FLOAT);
	}

	public static OffHeapMemory allocateDoubles(String pName,
																							long pNumberOfDoubles)
	{
		return new OffHeapMemory(pName, pNumberOfDoubles * Size.DOUBLE);
	}

	public OffHeapMemory(final long pLengthInBytes)
	{
		this(null, pLengthInBytes);
	}

	public OffHeapMemory(final String pName, final long pLengthInBytes)
	{
		this(	null,
					OffHeapMemoryAccess.allocateMemory(pLengthInBytes),
					pLengthInBytes);
	}

	public OffHeapMemory(	final Object pParent,
												final long pAddress,
												final long pLengthInBytes)
	{
		this("NULL", pParent, pAddress, pLengthInBytes);
	}

	public OffHeapMemory(	final String pName,
												final Object pParent,
												final long pAddress,
												final long pLengthInBytes)
	{
		super(pAddress, pLengthInBytes);
		mName = pName.intern();
		mParent = pParent;
		mAllocationStackTrace = Thread.currentThread().getStackTrace();
		mSignature = OffHeapMemoryAccess.getSignature(getAddress());
		RessourceGarbageCollector.register(this);
	}

	@Override
	public OffHeapMemory subRegion(	final long pOffset,
																	final long pLenghInBytes)
	{
		if (mAddressInBytes + pOffset + pLenghInBytes > mAddressInBytes + mLengthInBytes)
			throw new InvalidNativeMemoryAccessException(String.format(	"Cannot instanciate OffHeapMemory on subregion staring at offset %d and length %d  ",
																																	pOffset,
																																	pLenghInBytes));
		final OffHeapMemory lOffHeapMemory = new OffHeapMemory(	this,
																														mAddressInBytes + pOffset,
																														pLenghInBytes);
		return lOffHeapMemory;
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
			mAddressInBytes = OffHeapMemoryAccess.reallocateMemory(	mAddressInBytes,
																															pNewLength);
			mLengthInBytes = pNewLength;
		}
		catch (final Throwable e)
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
			OffHeapMemoryAccess.freeMemory(mAddressInBytes);
		}
		mAddressInBytes = 0;
		mParent = null;
		super.free();
	}

	@Override
	public Cleaner getCleaner()
	{
		if (mParent != null)
			return new OffHeapMemoryCleaner(null,
																			mSignature,
																			mName,
																			mAllocationStackTrace);
		return new OffHeapMemoryCleaner(mAddressInBytes,
																		mSignature,
																		mName,
																		mAllocationStackTrace);
	}

	@Override
	public String toString()
	{
		return "OffHeapMemory [mParent=" + mParent
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
