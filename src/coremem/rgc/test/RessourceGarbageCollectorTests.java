package coremem.rgc.test;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import coremem.rgc.Cleanable;
import coremem.rgc.Cleaner;
import coremem.rgc.Freeable;
import coremem.rgc.FreeableBase;
import coremem.rgc.RessourceGarbageCollector;

public class RessourceGarbageCollectorTests
{
	static AtomicInteger sCounter = new AtomicInteger(0);

	public static final void freeRessource(long pResourceId)
	{
		sCounter.incrementAndGet();
		// System.out.println("freeing: " + pResourceId);
	}

	private static class ClassWithRessource extends FreeableBase implements
																															Freeable,
																															Cleanable
	{
		long mSomeRessource = (long) (1000 * Math.random());
		AtomicBoolean mFree = new AtomicBoolean(false);

		{
			ClassWithRessource lClassWithRessource = this;
			RessourceGarbageCollector.register(lClassWithRessource);

			// double[] lGarbage = new double[10000000];
			// lGarbage[12345] = 1;
		}

		static class MyCleaner implements Cleaner
		{
			private long mSomeRessource2;

			public MyCleaner(long pSomeRessource)
			{
				mSomeRessource2 = pSomeRessource;
			}

			@Override
			public void run()
			{
				freeRessource(mSomeRessource2);
			}

		}

		@Override
		public Cleaner getCleaner()
		{
			return new MyCleaner(mSomeRessource);
		}

		@Override
		public void free()
		{
			mFree.set(false);
			freeRessource(mSomeRessource);
		}

		@Override
		public boolean isFree()
		{
			return mFree.get();
		}

	}

	@Test
	public void testRessourceCollection() throws InterruptedException
	{

		for (int i = 0; i < 100; i++)
		{
			ClassWithRessource a = new ClassWithRessource();
			RessourceGarbageCollector.collectNow();
			sleep(1);
		}

		for (int i = 0; i < 1000; i++)
		{
			sleep(1);
			System.gc();
			RessourceGarbageCollector.collectNow();
			// System.out.println(i);
		}

		int lNumberOfRegisteredObjects = RessourceGarbageCollector.getNumberOfRegisteredObjects();
		assertEquals(0, lNumberOfRegisteredObjects);
		// System.out.println("lNumberOfRegisteredObjects=" +
		// lNumberOfRegisteredObjects);

		// System.out.println(sCounter.get());
		assertEquals(100, sCounter.get());
	}

	private void sleep(int pMilliseconds)
	{
		try
		{
			Thread.sleep(pMilliseconds);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

	}
}
