package net.haesleinhuepf.clij.coremem.rgc;

import net.haesleinhuepf.clij.coremem.rgc.Cleanable;
import net.haesleinhuepf.clij.coremem.rgc.Cleaner;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

/**
 * Phantom reference augmented with a cleaner runnable that knows how to 'clean'
 * the ressources of the referent.
 *
 * @author royer
 */
class CleaningPhantomReference extends PhantomReference<net.haesleinhuepf.clij.coremem.rgc.Cleanable>
{

  private final net.haesleinhuepf.clij.coremem.rgc.Cleaner mCleaner;

  /**
   * Instanciates a cleaning phantom reference given a referent, a cleaner
   * runnable that knows how to 'clean' the resources of te referent, and a
   * reference queue.
   * 
   * @param pReferent
   *          referent
   * @param pCleaner
   *          cleaner
   * @param pReferencenQueue
   *          reference queue
   */
  public CleaningPhantomReference(net.haesleinhuepf.clij.coremem.rgc.Cleanable pReferent,
                                  net.haesleinhuepf.clij.coremem.rgc.Cleaner pCleaner,
                                  ReferenceQueue<Cleanable> pReferencenQueue)
  {
    super(pReferent, pReferencenQueue);
    mCleaner = pCleaner;
  }

  /**
   * Returns the cleaner Runnable responsible for releasing the ressources of
   * the referent.
   * 
   * @return cleaner
   */
  public Cleaner getCleaner()
  {
    return mCleaner;
  }

}
