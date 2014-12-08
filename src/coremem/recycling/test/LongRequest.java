package coremem.recycling.test;

import coremem.recycling.RecyclerRequest;

class LongRequest implements RecyclerRequest<RecyclableTestClass>
{
	public LongRequest(long pL)
	{
		value = pL;
	}

	public long value;
}