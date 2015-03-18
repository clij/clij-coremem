package coremem.exceptions;

public class CoreMemException extends RuntimeException
{

	private static final long serialVersionUID = 1L;

	public CoreMemException(String pString)
	{
		super(pString);
	}

	public CoreMemException(String pErrorMessage, Throwable pE)
	{
		super(pErrorMessage, pE);
	}

}
