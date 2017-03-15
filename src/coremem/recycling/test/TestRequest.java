package coremem.recycling.test;

import coremem.recycling.RecyclerRequestInterface;

class TestRequest implements RecyclerRequestInterface
{
  public TestRequest(long pL)
  {
    size = pL;
  }

  public long size;
}