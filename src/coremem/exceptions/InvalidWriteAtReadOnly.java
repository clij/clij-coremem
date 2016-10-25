package coremem.exceptions;

/**
 * Exception thrown when trying to write to a read-only memory object.
 *
 * @author royer
 */
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
