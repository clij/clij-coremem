package coremem.rgc;

/**
 *
 *
 * @author royer
 */
public abstract class FreeableBase implements Freeable
{

	/* (non-Javadoc)
	 * @see coremem.rgc.Freeable#complainIfFreed()
	 */
	@Override
  public void complainIfFreed() throws FreedException
	{
		if (isFree())
		{
			final String lErrorMessage = "Underlying ressource has been freed!";
			throw new FreedException(lErrorMessage);
		}
	}

}
