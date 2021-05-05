package net.haesleinhuepf.clij.coremem;

import net.haesleinhuepf.clij.coremem.interfaces.MappableMemory;

/**
 * This base class offers basic mapping/unmapping machinery for descendents.
 * 
 * @author royer
 */
public abstract class MappedMemoryBase extends MemoryBase
                                       implements MappableMemory

{

  /**
   * This flag is true when this memory region is mapped, false otherwise.
   */
  private volatile boolean mIsMapped;

  /* (non-Javadoc)
   * @see MappableMemory#map()
   */
  @Override
  public abstract long map();

  /* (non-Javadoc)
   * @see MappableMemory#unmap()
   */
  @Override
  public abstract void unmap();

  /* (non-Javadoc)
   * @see MappableMemory#isCurrentlyMapped()
   */
  @Override
  public boolean isCurrentlyMapped()
  {
    return mIsMapped;
  }

  /**
   * Sets the mapped state of this MappableMemory region.
   * 
   * @param pMapped ?
   */
  protected void setCurrentlyMapped(boolean pMapped)
  {
    mIsMapped = pMapped;
  }

}
