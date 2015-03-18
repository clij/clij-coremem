package coremem.recycling.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import coremem.recycling.RecyclableFactory;
import coremem.recycling.Recycler;

public class RecyclerTests
{

	// TODO: this test is failing!!! @Test
	public void testBasics()
	{

		final RecyclableFactory<RecyclableTestClass, LongRequest> lRecyclableFactory = new RecyclableFactory<RecyclableTestClass, LongRequest>()
		{
			@Override
			public RecyclableTestClass create(LongRequest pParameters)
			{
				return new RecyclableTestClass(pParameters);
			}
		};

		final Recycler<RecyclableTestClass, LongRequest> lRecycler = new Recycler<RecyclableTestClass, LongRequest>(lRecyclableFactory,
																																																								1000);

		assertEquals(	100,
									lRecycler.ensurePreallocated(	100,
																								new LongRequest(1L)));

		assertEquals(1000, lRecycler.getLiveMemoryInBytes());
		assertEquals(100, lRecycler.getLiveObjectCount());

		lRecycler.freeReleasedObjects(true);

		assertEquals(0, lRecycler.getLiveMemoryInBytes());
		assertEquals(0, lRecycler.getLiveObjectCount());

		assertEquals(	100,
									lRecycler.ensurePreallocated(	110,
																								new LongRequest(1L)));

		assertEquals(1000, lRecycler.getLiveMemoryInBytes());
		assertEquals(100, lRecycler.getLiveObjectCount());

		final HashSet<RecyclableTestClass> lRecyclableObjectSet = new HashSet<RecyclableTestClass>();
		for (int i = 0; i < 100; i++)
		{
			final RecyclableTestClass lRecyclableObject = lRecycler.failOrRequestRecyclableObject(new LongRequest(1L));
			assertTrue(lRecyclableObject != null);
			lRecyclableObjectSet.add(lRecyclableObject);
		}

		try
		{
			for (int i = 0; i < 10; i++)
			{
				final RecyclableTestClass lFailOrRequestRecyclableObject = lRecycler.failOrRequestRecyclableObject(new LongRequest(1L));
				assertTrue(lFailOrRequestRecyclableObject == null);
			}
			fail();
		}
		catch (final OutOfMemoryError e)
		{
			assertTrue(true);
		}

		for (final RecyclableTestClass lRecyclableTestClass : lRecyclableObjectSet)
		{
			lRecycler.release(lRecyclableTestClass);
		}

		lRecycler.freeReleasedObjects(true);

		assertEquals(0, lRecycler.getLiveMemoryInBytes());
		assertEquals(0, lRecycler.getLiveObjectCount());

		for (int i = 0; i < 99; i++)
		{
			// System.out.println(i);
			final RecyclableTestClass lFailOrRequestRecyclableObject = lRecycler.failOrRequestRecyclableObject(new LongRequest(1L));
			assertTrue(lFailOrRequestRecyclableObject != null);
			lRecycler.release(lFailOrRequestRecyclableObject);
		}

		assertEquals(10, lRecycler.getLiveMemoryInBytes());
		assertEquals(1, lRecycler.getLiveObjectCount());

		boolean lOutOfMemoryHappened = false;

		for (int i = 1; i <= 101; i++)
		{
			// System.out.println(lRecycler.getLiveObjectCount());
			// System.out.println(i);
			final long lStart = System.nanoTime();
			RecyclableTestClass lFailOrRequestRecyclableObject = null;
			try
			{
				lFailOrRequestRecyclableObject = lRecycler.waitOrRequestRecyclableObject(	1000,
																																									TimeUnit.MILLISECONDS,
																																									new LongRequest(1L));
			}
			catch (final OutOfMemoryError e)
			{
				lOutOfMemoryHappened = true;
			}

			final long lStop = System.nanoTime();
			final long lMilliSecondsElapsed = (long) (0.000001) * (lStop - lStart);
			if (i <= 100)
			{
				assertTrue(lMilliSecondsElapsed < 1000);
				assertTrue(lFailOrRequestRecyclableObject != null);
			}
			else
			{
				assertTrue(lFailOrRequestRecyclableObject == null);
				if (lMilliSecondsElapsed < 1000)
					System.out.println("lMilliSecondsElapsed=" + lMilliSecondsElapsed);
				assertTrue(lMilliSecondsElapsed >= 999);
			}

		}

		assertTrue(lOutOfMemoryHappened);

		lRecycler.free();

	}

	@Test
	public void testTightRecycling()
	{
		final RecyclableFactory<RecyclableTestClass, LongRequest> lRecyclableFactory = new RecyclableFactory<RecyclableTestClass, LongRequest>()
		{
			@Override
			public RecyclableTestClass create(LongRequest pParameters)
			{
				return new RecyclableTestClass(pParameters);
			}
		};

		final Recycler<RecyclableTestClass, LongRequest> lRecycler = new Recycler<RecyclableTestClass, LongRequest>(lRecyclableFactory,
																																																								1000);

		for (int i = 0; i < 100; i++)
		{
			final RecyclableTestClass lRecyclableObject = lRecycler.waitOrRequestRecyclableObject(1,
																																														TimeUnit.SECONDS,
																																														new LongRequest(1L));
			assertTrue(lRecyclableObject != null);

			// if (i % 2 == 0)
			lRecycler.release(lRecyclableObject);
		}

	}

}
