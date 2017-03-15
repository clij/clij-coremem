package coremem.exceptions;

/**
 * Exception thrown during memory mapping
 *
 * @author royer
 */
public class MemoryMapException extends CoreMemException
{

  private static final long serialVersionUID = 1L;

  public MemoryMapException(String pString)
  {
    super(pString);
  }

  public MemoryMapException(String pErrorMessage, Throwable pE)
  {
    super(pErrorMessage, pE);
  }

}
