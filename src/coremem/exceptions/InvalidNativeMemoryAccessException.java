package coremem.exceptions;

/**
 * Exception thrown when an invalid native memory access occurs.
 *
 * @author royer
 */
public class InvalidNativeMemoryAccessException	extends
																								CoreMemException
{

	private static final long serialVersionUID = 1L;

	public InvalidNativeMemoryAccessException(String pString)
	{
		super(pString);
	}

}
