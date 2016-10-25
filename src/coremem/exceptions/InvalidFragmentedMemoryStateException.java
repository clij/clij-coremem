package coremem.exceptions;

/**
 * Exception passed when a fragmented emory is in an invalid state.
 *
 * @author royer
 */
public class InvalidFragmentedMemoryStateException extends
																									CoreMemException
{

	private static final long serialVersionUID = 1L;

	public InvalidFragmentedMemoryStateException(String pString)
	{
		super(pString);
	}

	public InvalidFragmentedMemoryStateException(	String pErrorMessage,
																								Throwable pE)
	{
		super(pErrorMessage, pE);
	}

}
