package coremem.exceptions;

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
