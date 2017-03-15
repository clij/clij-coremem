package coremem.rgc;

/**
 *
 *
 * @author royer
 */
public interface Cleaner extends Runnable
{
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run();
}
