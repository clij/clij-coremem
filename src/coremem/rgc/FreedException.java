package coremem.rgc;

/**
 *
 *
 * @author royer
 */
public class FreedException extends RuntimeException
{

	private static final long serialVersionUID = 1L;

	/**
	 * @param pErrorMessage
	 */
	public FreedException(String pErrorMessage)
	{
		super(pErrorMessage);
	}

}
