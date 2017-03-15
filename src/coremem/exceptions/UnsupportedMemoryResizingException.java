package coremem.exceptions;

/**
 * Exception thrown when trying to resize a memory object that cannot be
 * resized.
 *
 * @author royer
 */
public class UnsupportedMemoryResizingException extends
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
