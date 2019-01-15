package net.haesleinhuepf.clij.coremem.recycling.test;

import java.util.concurrent.atomic.AtomicBoolean;

import net.haesleinhuepf.clij.coremem.ContiguousMemoryInterface;
import net.haesleinhuepf.clij.coremem.offheap.OffHeapMemory;
import net.haesleinhuepf.clij.coremem.recycling.RecyclableInterface;
import net.haesleinhuepf.clij.coremem.recycling.RecyclerInterface;
import net.haesleinhuepf.clij.coremem.rgc.FreeableBase;

/**
 * Example Recyclable used for testing.
 *
 * @author royer
 */
public class TestRecyclable extends FreeableBase implements
        RecyclableInterface<TestRecyclable, TestRequest>
{
  // Proper class fields:
  ContiguousMemoryInterface mBuffer;

  // Recycling related fields:
  private net.haesleinhuepf.clij.coremem.recycling.RecyclerInterface<TestRecyclable, TestRequest> mRecycler;
  AtomicBoolean mReleased = new AtomicBoolean(false);

  /**
   * Instanciates a recyclable object with a given request
   * 
   * @param pRequest
   *          request
   * 
   */
  public TestRecyclable(TestRequest pRequest)
  {
    recycle(pRequest);
  }

  @Override
  public long getSizeInBytes()
  {
    return mBuffer.getSizeInBytes();
  }

  @Override
  public void free()
  {
    mBuffer.free();
  }

  @Override
  public boolean isFree()
  {
    return mBuffer.isFree();
  }

  @Override
  public boolean isCompatible(TestRequest pRequest)
  {
    return mBuffer.getSizeInBytes() == pRequest.size;
  }

  @Override
  public void recycle(TestRequest pRequest)
  {
    mBuffer = OffHeapMemory.allocateBytes(pRequest.size);
  }

  @Override
  public void setReleased(boolean pIsReleased)
  {
    mReleased.set(pIsReleased);
  }

  @Override
  public boolean isReleased()
  {
    return mReleased.get();
  }

  @Override
  public void release()
  {
    mRecycler.release(this);
  }

  @Override
  public void setRecycler(RecyclerInterface<TestRecyclable, TestRequest> pRecycler)
  {
    mRecycler = pRecycler;
  }

  @Override
  public String toString()
  {
    return String.format("TestRecyclable [mBuffer=%s, mRecycler=%s, mReleased=%s]",
                         mBuffer,
                         mRecycler,
                         mReleased);
  }

}
