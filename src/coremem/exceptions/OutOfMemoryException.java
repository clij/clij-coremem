package coremem.exceptions;

/**
 * Out-of-memory exception.
 *
 * @author royer
 */
public class OutOfMemoryException extends CoreMemException
{

	private static final long serialVersionUID = 1L;

	public OutOfMemoryException(String pString)
	{
		super(pString);
	}

	public OutOfMemoryException(String pErrorMessage, Throwable pE)
	{
		super(pErrorMessage, pE);
	}

}
