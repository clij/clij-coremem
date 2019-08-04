package net.haesleinhuepf.clij.coremem.rgc;

import net.haesleinhuepf.clij.coremem.rgc.Cleanable;
import net.haesleinhuepf.clij.coremem.rgc.Cleaner;
import net.haesleinhuepf.clij.coremem.rgc.CleaningPhantomReference;

import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The ressource cleaner handles the release of ressources after the garbage
 * collector has maked objects unreachable.
 *
 * 
 * @author royer
 */
public class RessourceCleaner
{
  private static int sMaxItemsToCleanPerRound = 100000;
  private static int sCleanupPeriodInMs = 100;

  private static final Executor sExecutor =
                                          Executors.newSingleThreadExecutor();


  private static RessourceCleaner sRessourceCleaner;

  static
  {
    sRessourceCleaner = new RessourceCleaner();
    sRessourceCleaner.cleanAtFixedRate(sCleanupPeriodInMs,
                                       TimeUnit.MILLISECONDS);
  }

  private static ConcurrentLinkedDeque<net.haesleinhuepf.clij.coremem.rgc.CleaningPhantomReference> sCleaningPhantomReferenceList =
                                                                                               new ConcurrentLinkedDeque<>();

  /**
   * Registers a cleanable object. When this object is eventually garbage
   * collected, the ressources associated to this object will be released by
   * executing the cleaner runnable.
   * 
   * @param pCleanable
   *          cleanable object for which ressources have to be released.
   */
  public static final void register(net.haesleinhuepf.clij.coremem.rgc.Cleanable pCleanable)
  {
    final net.haesleinhuepf.clij.coremem.rgc.CleaningPhantomReference lCleaningPhantomReference =
                                                             new net.haesleinhuepf.clij.coremem.rgc.CleaningPhantomReference(pCleanable,
                                                                                          pCleanable.getCleaner(),
                                                                                          sRessourceCleaner.getReferenceQueue());

    sCleaningPhantomReferenceList.add(lCleaningPhantomReference);
  }

  private final ReferenceQueue<net.haesleinhuepf.clij.coremem.rgc.Cleanable> mReferenceQueue =
                                                          new ReferenceQueue<>();

  private ReferenceQueue<Cleanable> getReferenceQueue()
  {
    return mReferenceQueue;
  }

  private final AtomicBoolean mActive = new AtomicBoolean(true);

  /**
   * Does the cleaning
   */
  private void clean()
  {
    if (mActive.get())
    {
      for (int i = 0; i < sMaxItemsToCleanPerRound; i++)
      {
        final net.haesleinhuepf.clij.coremem.rgc.CleaningPhantomReference lReference =
                                                  (net.haesleinhuepf.clij.coremem.rgc.CleaningPhantomReference) mReferenceQueue.poll();

        // if the queue is empty we get null...
        if (lReference == null)
          return;
        final Cleaner lCleaner = lReference.getCleaner();
        if (lCleaner != null)
          sExecutor.execute(lCleaner);
        sCleaningPhantomReferenceList.remove(lReference);
      }

    }
  }

  /**
   * Schedules the cleaning at a fixed rate.
   * 
   * @param pPeriod
   *          period
   * @param pUnit
   *          unit for period
   */
  private void cleanAtFixedRate(long pPeriod, TimeUnit pUnit)
  {
    final Runnable lCollector = new Runnable()
    {
      @Override
      public void run()
      {
        final long lPeriodInMillis = pUnit.toMillis(pPeriod);
        while(true)
        {
          clean();
          try
          {
            Thread.sleep(lPeriodInMillis);
          }
          catch (InterruptedException pE)
          {
          }
        }
      }
    };

    Thread lThread = new Thread(lCollector, "RGC_Thread");
    lThread.setDaemon(true);
    lThread.setPriority(Thread.MIN_PRIORITY);
    lThread.start();
  }

  /**
   * Forces the immediate cleaning of ressources after thei corresponding
   * objects are marked for garbage collection.
   */
  public static void cleanNow()
  {
    sRessourceCleaner.clean();
  }

  /**
   * Prevents the cleaning of ressources for the duration of the execution of
   * the provided runnable (executed synchronously by same thread as caller)
   * 
   * @param pRunnable
   *          runnable
   */
  public static void preventCleaning(Runnable pRunnable)
  {
    final boolean lActive = sRessourceCleaner.mActive.get();
    sRessourceCleaner.mActive.set(false);
    pRunnable.run();
    sRessourceCleaner.mActive.compareAndSet(false, lActive);
  }

  /**
   * Returns the number of registered objects
   * 
   * @return number of registered objects
   */
  public static int getNumberOfRegisteredObjects()
  {
    return sCleaningPhantomReferenceList.size();
  }

}
