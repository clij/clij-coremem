package coremem.exceptions;

/**
 * Exception thrown when memory mapping a file
 *
 * @author royer
 */
public class MemoryMapFileException extends MemoryMapException
{

  private static final long serialVersionUID = 1L;

  public MemoryMapFileException(String lErrorMessage, Throwable pE)
  {
    super(lErrorMessage, pE);
  }

}
