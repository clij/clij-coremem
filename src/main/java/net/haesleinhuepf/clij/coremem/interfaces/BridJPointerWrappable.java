package net.haesleinhuepf.clij.coremem.interfaces;

import org.bridj.Pointer;

/**
 * Memory objects implementing this interface can be wrapped into a bridj
 * pointer.
 *
 * @author royer
 */
public interface BridJPointerWrappable
{
  /**
   * Returns a BridJ pointer for this memory.
   * 
   * @param pTargetClass
   *          target class
   * @param <T> ?
   * @return BridJ
   */
  public <T> Pointer<T> getBridJPointer(Class<T> pTargetClass);
}
