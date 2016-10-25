package coremem.exceptions;

/**
 * Exception thrown when Size.of function canot determine the size in bytes of
 * Java object.
 *
 * @author royer
 */
public class UnknownSizeOfException extends CoreMemException
{

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs with object for which the size cannot be determined.
	 * 
	 * @param pObject
	 */
	public UnknownSizeOfException(Object pObject)
	{
		super("Unknown size-of for object:  " + pObject
					+ " of class: "
					+ pObject.getClass().toString());
	}

}
