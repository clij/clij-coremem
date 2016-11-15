package coremem.memmap;

/**
 *
 *
 * @author royer
 */
public enum MemoryMappedFileAccessMode
{
	ReadOnly(0), ReadWrite(1), Private(2);

	private final int mValue;

	/**
	 * @param pValue
	 */
	private MemoryMappedFileAccessMode(final int pValue)
	{
		mValue = pValue;
	}

	/**
	 * @return
	 */
	public int getValue()
	{
		return mValue;
	}
}
