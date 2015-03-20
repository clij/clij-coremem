package coremem.recycling.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import coremem.recycling.BasicRecycler;
import coremem.recycling.RecyclableFactory;
import coremem.recycling.RecyclerInterface;

public class RecyclerTests
{

	@Test
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

		final RecyclerInterface<RecyclableTestClass, LongRequest> lRecycler = new BasicRecycler<RecyclableTestClass, LongRequest>(lRecyclableFactory,
																																																															200,
																																																															200,
																																																															true);

		assertEquals(	100,
									lRecycler.ensurePreallocated(	100,
																								new LongRequest(1L)));

		assertEquals(100, lRecycler.getNumberOfAvailableObjects());
		assertEquals(0, lRecycler.getNumberOfLiveObjects());

		lRecycler.clearReleased();

		assertEquals(0, lRecycler.getNumberOfAvailableObjects());
		assertEquals(0, lRecycler.getNumberOfLiveObjects());

		assertEquals(	100,
									lRecycler.ensurePreallocated(	100,
																								new LongRequest(1L)));

		assertEquals(100, lRecycler.getNumberOfAvailableObjects());
		assertEquals(0, lRecycler.getNumberOfLiveObjects());

		final HashSet<RecyclableTestClass> lRecyclableObjectSet = new HashSet<RecyclableTestClass>();
		for (int i = 0; i < 200; i++)
		{
			final RecyclableTestClass lRecyclableObject = lRecycler.getOrFail(new LongRequest(1L));
			assertTrue(lRecyclableObject != null);
			lRecyclableObjectSet.add(lRecyclableObject);
		}

		for (int i = 0; i < 10; i++)
		{
			final RecyclableTestClass lFailOrRequestRecyclableObject = lRecycler.getOrFail(new LongRequest(1L));
			// System.out.println(lFailOrRequestRecyclableObject);
			assertTrue(lFailOrRequestRecyclableObject == null);
		}

		assertEquals(0, lRecycler.getNumberOfAvailableObjects());
		assertEquals(200, lRecycler.getNumberOfLiveObjects());

		for (final RecyclableTestClass lRecyclableTestClass : lRecyclableObjectSet)
		{
			lRecycler.release(lRecyclableTestClass);
		}

		assertEquals(200, lRecycler.getNumberOfAvailableObjects());
		assertEquals(0, lRecycler.getNumberOfLiveObjects());

		lRecycler.clearReleased();

		assertEquals(0, lRecycler.getNumberOfAvailableObjects());
		assertEquals(0, lRecycler.getNumberOfLiveObjects());

		for (int i = 0; i < 100; i++)
		{
			// System.out.println(i);
			final RecyclableTestClass lFailOrRequestRecyclableObject = lRecycler.getOrWait(	1,
																																											TimeUnit.MICROSECONDS,
																																											new LongRequest(1L));
			assertTrue(lFailOrRequestRecyclableObject != null);
			lRecycler.release(lFailOrRequestRecyclableObject);
		}

		assertEquals(1, lRecycler.getNumberOfAvailableObjects());
		assertEquals(0, lRecycler.getNumberOfLiveObjects());

		for (int i = 0; i < 200; i++)
		{
			// System.out.println(i);
			final RecyclableTestClass lRecyclableObject = lRecycler.getOrWait(1,
																																				TimeUnit.MICROSECONDS,
																																				new LongRequest(1L));
			assertTrue(lRecyclableObject != null);
			lRecyclableObjectSet.add(lRecyclableObject);
		}

		assertEquals(0, lRecycler.getNumberOfAvailableObjects());
		assertEquals(200, lRecycler.getNumberOfLiveObjects());

		for (int i = 0; i < 10; i++)
		{
			final RecyclableTestClass lFailOrRequestRecyclableObject = lRecycler.getOrFail(new LongRequest(1L));
			// System.out.println(lFailOrRequestRecyclableObject);
			assertTrue(lFailOrRequestRecyclableObject == null);
		}

		assertEquals(0, lRecycler.getNumberOfAvailableObjects());
		assertEquals(200, lRecycler.getNumberOfLiveObjects());

		lRecycler.free();

		assertEquals(0, lRecycler.getNumberOfAvailableObjects());
		assertEquals(200, lRecycler.getNumberOfLiveObjects());
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

		final BasicRecycler<RecyclableTestClass, LongRequest> lRecycler = new BasicRecycler<RecyclableTestClass, LongRequest>(lRecyclableFactory,
																																																													1000);

		for (int i = 0; i < 100000; i++)
		{
			final RecyclableTestClass lRecyclableObject = lRecycler.getOrWait(1,
																																				TimeUnit.SECONDS,
																																				new LongRequest(1L));
			assertTrue(lRecyclableObject != null);

			// if (i % 2 == 0)
			lRecycler.release(lRecyclableObject);
		}

	}

}
