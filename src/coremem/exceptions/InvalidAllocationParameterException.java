package coremem.exceptions;

/**
 * Exception thrown when invalid parameters are passed during memory allocation.
 *
 * @author royer
 */
public class InvalidAllocationParameterException extends
																								CoreMemException
{

	private static final long serialVersionUID = 1L;

	public InvalidAllocationParameterException(String pString)
	{
		super(pString);
	}

	public InvalidAllocationParameterException(	String pErrorMessage,
																							Throwable pE)
	{
		super(pErrorMessage, pE);
	}

}
