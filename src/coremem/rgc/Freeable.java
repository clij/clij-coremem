package coremem.rgc;

/**
 *
 *
 * @author royer
 */
public interface Freeable
{
  /**
   * 
   */
  public void free();

  /**
   * @return
   */
  public boolean isFree();

  /**
   * @throws FreedException
   */
  public void complainIfFreed() throws FreedException;

}
