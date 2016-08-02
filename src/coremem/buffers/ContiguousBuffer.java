package coremem.buffers;

import java.util.ArrayDeque;

import coremem.ContiguousMemoryInterface;
import coremem.interfaces.SizedInBytes;
import coremem.offheap.OffHeapMemory;
import coremem.offheap.OffHeapMemoryAccess;
import coremem.types.NativeTypeEnum;

/**
 * ContiguousBuffer is a more handy way to read and write to and from instances
 * of ContiguousMemoryInterface. It holds a 'position' that is automatically
 * incremented for read and writes of different primitive types. This position
 * can be moved, pushed and popped.
 * 
 * @author royer
 */
public class ContiguousBuffer implements SizedInBytes
{
	/**
	 * Wrapped ContiguousMemoryInterface instance.
	 */
	private final ContiguousMemoryInterface mContiguousMemoryInterface;

	/**
	 * This caches the first valid and invalid positions in the contiguous buffer:
	 */
	private final long mFirstValidPosition;
	private final long mFirstInvalidPosition;

	/**
	 * Current position
	 */
	private volatile long mPosition;

	/**
	 * Queue for pushing and popping positions.
	 */
	private final ArrayDeque<Long> mStack = new ArrayDeque<Long>();

	/**
	 * Allocates a ContiguousBuffer (using OffHeapMemory) of given length.
	 * 
	 * @param pLengthInBytes
	 * @return
	 */
	public static ContiguousBuffer allocate(long pLengthInBytes)
	{
		final OffHeapMemory lAllocatedBytes = OffHeapMemory.allocateBytes(pLengthInBytes);
		final ContiguousBuffer lContiguousBuffer = new ContiguousBuffer(lAllocatedBytes);
		return lContiguousBuffer;
	}

	/**
	 * Wraps a ContiguousMemoryInterface with a ContiguousBuffer.
	 * 
	 * @param pContiguousMemoryInterface
	 * @return
	 */
	public static ContiguousBuffer wrap(ContiguousMemoryInterface pContiguousMemoryInterface)
	{
		return new ContiguousBuffer(pContiguousMemoryInterface);
	}

	/**
	 * Constructs a ContiguousBuffer by wrapping a ContiguousMemoryInterface.
	 * 
	 * @param pContiguousMemoryInterface
	 */
	public ContiguousBuffer(ContiguousMemoryInterface pContiguousMemoryInterface)
	{
		super();
		mContiguousMemoryInterface = pContiguousMemoryInterface;
		mFirstValidPosition = pContiguousMemoryInterface.getAddress();
		mPosition = mFirstValidPosition;
		mFirstInvalidPosition = mFirstValidPosition + pContiguousMemoryInterface.getSizeInBytes();
	}

	/**
	 * Returns the underlying ContiguousMemoryInterface.
	 * 
	 * @return
	 */
	public ContiguousMemoryInterface getContiguousMemory()
	{
		return mContiguousMemoryInterface;
	}

	public long getSizeInBytes()
	{
		return mContiguousMemoryInterface.getSizeInBytes();
	}

	/**
	 * Sets the current position to a new value.
	 * 
	 * @param pNewPosition
	 *          new position value.
	 */
	public void setPosition(long pNewPosition)
	{
		mPosition = mFirstValidPosition + pNewPosition;
	}

	/**
	 * Rewinds the position to the first valid position in the buffer.
	 */
	public void rewind()
	{
		mPosition = mFirstValidPosition;
	}

	/**
	 * Clears the position stack.
	 */
	public void clearStack()
	{
		mStack.clear();
	}

	/**
	 * Pushes current position to stack.
	 */
	public void pushPosition()
	{
		mStack.push(mPosition);
	}

	/**
	 * Pops position at the top of the stack and sets it as the new current
	 * position.
	 */
	public void popPosition()
	{
		mPosition = mStack.pop();
	}

	/**
	 * Checks whether the current position is valid.
	 * 
	 * @return true if valid, false otherwise.
	 */
	public boolean isPositionValid()
	{
		final long lAddress = mContiguousMemoryInterface.getAddress();
		final long lSizeInBytes = mContiguousMemoryInterface.getSizeInBytes();
		return lAddress <= mPosition && mPosition < lAddress + lSizeInBytes;
	}

	public boolean hasRemainingByte()
	{
		return mPosition <= mFirstInvalidPosition - Byte.BYTES;
	}

	public boolean hasRemainingChar()
	{
		return mPosition <= mFirstInvalidPosition - Character.BYTES;
	}

	public boolean hasRemainingShort()
	{
		return mPosition <= mFirstInvalidPosition - Short.BYTES;
	}

	public boolean hasRemainingInt()
	{
		return mPosition <= mFirstInvalidPosition - Integer.BYTES;
	}

	public boolean hasRemainingLong()
	{
		return mPosition <= mFirstInvalidPosition - Long.BYTES;
	}

	public boolean hasRemainingFloat()
	{
		return mPosition <= mFirstInvalidPosition - Float.BYTES;
	}

	public boolean hasRemainingDouble()
	{
		return mPosition <= mFirstInvalidPosition - Double.BYTES;
	}

	/**
	 * Writes the entire contents of a ContigContiguousMemoryInterface into this
	 * buffer. The position is incremented accordingly.
	 * 
	 * @param pContiguousMemoryInterface
	 */
	public void writeContiguousMemory(ContiguousMemoryInterface pContiguousMemoryInterface)
	{
		OffHeapMemoryAccess.copyMemory(	pContiguousMemoryInterface.getAddress(),
																		mPosition,
																		pContiguousMemoryInterface.getSizeInBytes());
		mPosition += pContiguousMemoryInterface.getSizeInBytes();
	}

	/**
	 * Write a sequence of identical bytes into this buffer. The position is
	 * incremented accordingly.
	 * 
	 * @param pNumberOfBytes
	 *          number of bytes to write
	 * @param pByte
	 *          byte to write repeatedly.
	 */
	public void writeBytes(long pNumberOfBytes, byte pByte)
	{
		OffHeapMemoryAccess.fillBytes(mPosition, pNumberOfBytes, pByte);
		mPosition += pNumberOfBytes;
	}

	/**
	 * Write a single byte. The position is incremented accordingly.
	 * @param pByte
	 */
	public void writeByte(byte pByte)
	{
		OffHeapMemoryAccess.setByte(mPosition, pByte);
		mPosition += 1;
	}

	/**
	 * Write a single short. The position is incremented accordingly.
	 * @param pShort
	 */
	public void writeShort(short pShort)
	{
		OffHeapMemoryAccess.setShort(mPosition, pShort);
		mPosition += 2;
	}

	/**
	 * Write a single char. The position is incremented accordingly.
	 * @param pChar
	 */
	public void writeChar(char pChar)
	{
		OffHeapMemoryAccess.setChar(mPosition, pChar);
		mPosition += 2;
	}

	/**
	 * Write a single int. The position is incremented accordingly.
	 * @param pInt
	 */
	public void writeInt(int pInt)
	{
		OffHeapMemoryAccess.setInt(mPosition, pInt);
		mPosition += 4;
	}

	/**
	 * Write a single long. The position is incremented accordingly.
	 * @param pLong
	 */
	public void writeLong(long pLong)
	{
		OffHeapMemoryAccess.setLong(mPosition, pLong);
		mPosition += 8;
	}

	/**
	 * Write a single float. The position is incremented accordingly.
	 * @param pFloat
	 */
	public void writeFloat(float pFloat)
	{
		OffHeapMemoryAccess.setFloat(mPosition, pFloat);
		mPosition += 4;
	}

	/**
	 * Write a single double. The position is incremented accordingly.
	 * @param pDouble
	 */
	public void writeDouble(char pDouble)
	{
		OffHeapMemoryAccess.setDouble(mPosition, pDouble);
		mPosition += 8;
	}

	/**
	 * Reads a single byte. The position is incremented accordingly.
	 * @return
	 */
	public byte readByte()
	{
		final byte lByte = OffHeapMemoryAccess.getByte(mPosition);
		mPosition += 1;
		return lByte;
	}

	/**
	 * Reads a single short. The position is incremented accordingly.
	 * @return
	 */
	public short readShort()
	{
		final short lShort = OffHeapMemoryAccess.getShort(mPosition);
		mPosition += 2;
		return lShort;
	}

	/**
	 * Reads a single char. The position is incremented accordingly.
	 * @return
	 */
	public char readChar()
	{
		final char lChar = OffHeapMemoryAccess.getChar(mPosition);
		mPosition += 2;
		return lChar;
	}

	/**
	 * Reads a single int. The position is incremented accordingly.
	 * @return
	 */
	public int readInt()
	{
		final int lInt = OffHeapMemoryAccess.getInt(mPosition);
		mPosition += 4;
		return lInt;
	}

	/**
	 * Reads a single long. The position is incremented accordingly.
	 * @return
	 */
	public long readLong()
	{
		final long lLong = OffHeapMemoryAccess.getLong(mPosition);
		mPosition += 8;
		return lLong;
	}

	/**
	 * Reads a single float. The position is incremented accordingly.
	 * @return
	 */
	public float readFloat()
	{
		final float lFloat = OffHeapMemoryAccess.getFloat(mPosition);
		mPosition += 4;
		return lFloat;
	}

	/**
	 * Reads a single double. The position is incremented accordingly.
	 * @return
	 */
	public double readDouble()
	{
		final double lDouble = OffHeapMemoryAccess.getDouble(mPosition);
		mPosition += 8;
		return lDouble;
	}

	/**
	 * Skips multiple bytes. The position is incremented accordingly.
	 * @param pNumberToSkip
	 */
	public void skipBytes(long pNumberToSkip)
	{
		mPosition += 1 * pNumberToSkip;
	}

	/**
	 * Skips multiple shorts. The position is incremented accordingly.
	 * @param pNumberToSkip
	 */
	public void skipShorts(long pNumberToSkip)
	{
		mPosition += 2 * pNumberToSkip;
	}

	/**
	 * Skips multiple chars. The position is incremented accordingly.
	 * @param pNumberToSkip
	 */
	public void skipChars(long pNumberToSkip)
	{
		mPosition += 2 * pNumberToSkip;
	}

	/**
	 * Skips multiple ints. The position is incremented accordingly.
	 * @param pNumberToSkip
	 */
	public void skipInts(long pNumberToSkip)
	{
		mPosition += 4 * pNumberToSkip;
	}

	/**
	 * Skips multiple longs. The position is incremented accordingly.
	 * @param pNumberToSkip
	 */
	public void skipLongs(long pNumberToSkip)
	{
		mPosition += 8 * pNumberToSkip;
	}

	/**
	 * Skips multiple floats. The position is incremented accordingly.
	 * @param pNumberToSkip
	 */
	public void skipFloats(long pNumberToSkip)
	{
		mPosition += 4 * pNumberToSkip;
	}

	/**
	 * Skips multiple doubles. The position is incremented accordingly.
	 * @param pNumberToSkip
	 */
	public void skipDoubles(long pNumberToSkip)
	{
		mPosition += 8 * pNumberToSkip;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return String.format(	"ContiguousBuffer [mContiguousMemoryInterface=%s, mFirstValidPosition=%s, mFirstInvalidPosition=%s, mPosition=%s, mStack=%s]",
													mContiguousMemoryInterface,
													mFirstValidPosition,
													mFirstInvalidPosition,
													mPosition,
													mStack);
	}

}
