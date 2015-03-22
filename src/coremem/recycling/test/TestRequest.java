package coremem.recycling.test;

import coremem.recycling.RecyclerRequest;

class TestRequest implements RecyclerRequest
{
	public TestRequest(long pL)
	{
		size = pL;
	}

	public long size;
}