package coremem.exceptions;

public class UnsupportedWrappingException extends CoreMemException
{

	private static final long serialVersionUID = 1L;

	public UnsupportedWrappingException(String pString)
	{
		super(pString);
	}

	public UnsupportedWrappingException(String pErrorMessage,
																			Throwable pE)
	{
		super(pErrorMessage, pE);
	}

}
