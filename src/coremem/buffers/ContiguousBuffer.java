package coremem.buffers;

import java.util.ArrayDeque;

import coremem.ContiguousMemoryInterface;
import coremem.offheap.OffHeapMemory;
import coremem.offheap.OffHeapMemoryAccess;

public class ContiguousBuffer
{
	private final ContiguousMemoryInterface mContiguousMemoryInterface;
	private final long mFirstValidPosition;
	private final long mLastValidPosition;
	private long mPosition;
	private final ArrayDeque<Long> mStack = new ArrayDeque<Long>();

	public static ContiguousBuffer allocate(long pLengthInBytes)
	{
		final OffHeapMemory lAllocatedBytes = OffHeapMemory.allocateBytes(pLengthInBytes);
		final ContiguousBuffer lContiguousBuffer = new ContiguousBuffer(lAllocatedBytes);
		return lContiguousBuffer;
	}

	public static ContiguousBuffer wrap(ContiguousMemoryInterface pContiguousMemoryInterface)
	{
		return new ContiguousBuffer(pContiguousMemoryInterface);
	}

	public ContiguousBuffer(ContiguousMemoryInterface pContiguousMemoryInterface)
	{
		super();
		mContiguousMemoryInterface = pContiguousMemoryInterface;
		mFirstValidPosition = pContiguousMemoryInterface.getAddress();
		mPosition = mFirstValidPosition;
		mLastValidPosition = mFirstValidPosition + pContiguousMemoryInterface.getSizeInBytes();
	}

	public ContiguousMemoryInterface getContiguousMemory()
	{
		return mContiguousMemoryInterface;
	}

	public long getSizeInBytes()
	{
		return mContiguousMemoryInterface.getSizeInBytes();
	}

	public void setPosition(long pOffset)
	{
		mPosition = mFirstValidPosition + pOffset;
	}

	public void rewind()
	{
		mPosition = mFirstValidPosition;
	}

	public void clearStack()
	{
		mStack.clear();
	}

	public void pushPosition()
	{
		mStack.push(mPosition);
	}

	public void popPosition()
	{
		mPosition = mStack.pop();
	}

	public boolean isPositionValid()
	{
		final long lAddress = mContiguousMemoryInterface.getAddress();
		final long lSizeInBytes = mContiguousMemoryInterface.getSizeInBytes();
		return lAddress <= mPosition && mPosition < lAddress + lSizeInBytes;
	}

	public boolean hasRemaining()
	{
		return mPosition <= mLastValidPosition;
	}

	public void writeBytes(long pNumberOfBytes, byte pByte)
	{
		OffHeapMemoryAccess.fillBytes(mPosition, pNumberOfBytes, pByte);
		mPosition += pNumberOfBytes;
	}

	public void writeByte(byte pByte)
	{
		OffHeapMemoryAccess.setByte(mPosition, pByte);
		mPosition += 1;
	}

	public void writeShort(short pShort)
	{
		OffHeapMemoryAccess.setShort(mPosition, pShort);
		mPosition += 2;
	}

	public void writeChar(char pChar)
	{
		OffHeapMemoryAccess.setChar(mPosition, pChar);
		mPosition += 2;
	}

	public void writeInt(int pInt)
	{
		OffHeapMemoryAccess.setInt(mPosition, pInt);
		mPosition += 4;
	}

	public void writeLong(long pLong)
	{
		OffHeapMemoryAccess.setLong(mPosition, pLong);
		mPosition += 8;
	}

	public void writeFloat(float pFloat)
	{
		OffHeapMemoryAccess.setFloat(mPosition, pFloat);
		mPosition += 4;
	}

	public void writeDouble(char pDouble)
	{
		OffHeapMemoryAccess.setDouble(mPosition, pDouble);
		mPosition += 8;
	}

	public byte readByte()
	{
		final byte lByte = OffHeapMemoryAccess.getByte(mPosition);
		mPosition += 1;
		return lByte;
	}

	public short readShort()
	{
		final short lShort = OffHeapMemoryAccess.getShort(mPosition);
		mPosition += 2;
		return lShort;
	}

	public char readChar()
	{
		final char lChar = OffHeapMemoryAccess.getChar(mPosition);
		mPosition += 2;
		return lChar;
	}

	public int readInt()
	{
		final int lInt = OffHeapMemoryAccess.getInt(mPosition);
		mPosition += 4;
		return lInt;
	}

	public long readLong()
	{
		final long lLong = OffHeapMemoryAccess.getLong(mPosition);
		mPosition += 8;
		return lLong;
	}

	public float readFloat()
	{
		final float lFloat = OffHeapMemoryAccess.getFloat(mPosition);
		mPosition += 4;
		return lFloat;
	}

	public double readDouble()
	{
		final double lDouble = OffHeapMemoryAccess.getDouble(mPosition);
		mPosition += 8;
		return lDouble;
	}

	public void skipBytes(long pNumberToSkip)
	{
		mPosition += 1 * pNumberToSkip;
	}

	public void skipShorts(long pNumberToSkip)
	{
		mPosition += 2 * pNumberToSkip;
	}

	public void skipChars(long pNumberToSkip)
	{
		mPosition += 2 * pNumberToSkip;
	}

	public void skipInts(long pNumberToSkip)
	{
		mPosition += 4 * pNumberToSkip;
	}

	public void skipLongs(long pNumberToSkip)
	{
		mPosition += 8 * pNumberToSkip;
	}

	public void skipFloats(long pNumberToSkip)
	{
		mPosition += 4 * pNumberToSkip;
	}

	public void skipDoubles(long pNumberToSkip)
	{
		mPosition += 8 * pNumberToSkip;
	}

	@Override
	public String toString()
	{
		return String.format(	"ContiguousBuffer [mContiguousMemoryInterface=%s, mFirstValidPosition=%s, mLastValidPosition=%s, mPosition=%s, mStack=%s]",
													mContiguousMemoryInterface,
													mFirstValidPosition,
													mLastValidPosition,
													mPosition,
													mStack);
	}



}
