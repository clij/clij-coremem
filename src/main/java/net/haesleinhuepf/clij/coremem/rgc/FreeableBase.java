package net.haesleinhuepf.clij.coremem.rgc;

import net.haesleinhuepf.clij.coremem.exceptions.FreedException;
import net.haesleinhuepf.clij.coremem.rgc.Freeable;

/**
 * Base class for all freeable objects
 *
 * @author royer
 */
public abstract class FreeableBase implements Freeable
{

  @Override
  public void complainIfFreed() throws FreedException
  {
    if (isFree())
    {
      final String lErrorMessage =
                                 "Underlying ressource has been freed!";
      throw new FreedException(lErrorMessage);
    }
  }

}
