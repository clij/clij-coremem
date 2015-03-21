package coremem.buffers;

import java.util.ArrayDeque;

import coremem.ContiguousMemoryInterface;
import coremem.offheap.OffHeapMemoryAccess;

public class ContiguousBuffer
{
	private final ContiguousMemoryInterface mContiguousMemoryInterface;
	private long mPosition;

	ArrayDeque<Long> mStack = new ArrayDeque<Long>();

	public ContiguousBuffer(ContiguousMemoryInterface pContiguousMemoryInterface)
	{
		super();
		mContiguousMemoryInterface = pContiguousMemoryInterface;
		mPosition = pContiguousMemoryInterface.getAddress();
	}

	public long getCapacity()
	{
		return mContiguousMemoryInterface.getSizeInBytes();
	}

	public void setPosition(long pOffset)
	{
		mPosition = mContiguousMemoryInterface.getAddress() + pOffset;
	}

	public void rewind()
	{
		mPosition = mContiguousMemoryInterface.getAddress();
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

	public void writeInt(char pInt)
	{
		OffHeapMemoryAccess.setInt(mPosition, pInt);
		mPosition += 4;
	}

	public void writeLong(char pLong)
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

}
