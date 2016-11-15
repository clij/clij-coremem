package coremem.interfaces;

/**
 * Memory objects implementing this interface can be copied from and to other
 * memory objects.
 * 
 * @param <M>
 *          other memory object
 * @author royer
 */
public interface Copyable<M extends Copyable<M>>
{
  /**
   * Copies this memory object entirely into an other.
   * 
   * @param pTo
   */
  public void copyTo(M pTo);

  /**
   * Copies an other memory object entirely into this one.
   * 
   * @param pFrom
   */
  public void copyFrom(M pFrom);

}
