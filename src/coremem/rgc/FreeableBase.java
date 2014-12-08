package coremem.rgc;

public abstract class FreeableBase implements Freeable
{

	public void complainIfFreed() throws FreedException
	{
		if (isFree())
		{
			final String lErrorMessage = "Underlying ressource has been freed!";
			throw new FreedException(lErrorMessage);
		}
	}

}
