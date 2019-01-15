package net.haesleinhuepf.clij.coremem.rgc;

import net.haesleinhuepf.clij.coremem.rgc.Cleaner;

/**
 * A cleanable object is an object that can return a runnable which task is to
 * clean the ressources associated to the cleanable object.
 * 
 * 
 *
 * @author royer
 */
public interface Cleanable
{
  /**
   * Returns the cleaner for this object.
   * 
   * @return cleaner runnable
   */
  public Cleaner getCleaner();

}
