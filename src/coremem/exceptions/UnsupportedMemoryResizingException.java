package coremem.exceptions;

public class UnsupportedMemoryResizingException	extends
																								CoreMemException
{

	private static final long serialVersionUID = 1L;

	public UnsupportedMemoryResizingException(String pString)
	{
		super(pString);
	}

	public UnsupportedMemoryResizingException(String pErrorMessage,
																						Throwable pE)
	{
		super(pErrorMessage, pE);
	}

}
