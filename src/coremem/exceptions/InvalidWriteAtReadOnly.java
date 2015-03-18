package coremem.exceptions;

public class InvalidWriteAtReadOnly extends CoreMemException
{

	private static final long serialVersionUID = 1L;

	public InvalidWriteAtReadOnly(String pString)
	{
		super(pString);
	}

	public InvalidWriteAtReadOnly(String pErrorMessage, Throwable pE)
	{
		super(pErrorMessage, pE);
	}

}
