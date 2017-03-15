package coremem.rgc;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

/**
 *
 *
 * @author royer
 */
class CleaningPhantomReference extends PhantomReference<Cleanable>
{

  private final Cleaner mCleaner;

  /**
   * @param pReferent
   * @param pCleaner
   * @param pReferencenQueue
   */
  public CleaningPhantomReference(Cleanable pReferent,
                                  Cleaner pCleaner,
                                  ReferenceQueue<Cleanable> pReferencenQueue)
  {
    super(pReferent, pReferencenQueue);
    mCleaner = pCleaner;
  }

  /**
   * @return
   */
  public Cleaner getCleaner()
  {
    return mCleaner;
  }

}
