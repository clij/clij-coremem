package coremem.interop;

import com.sun.jna.Pointer;

/**
 * BridJ buffers interoperability
 *
 * @author royer
 */
public class JNAInterop
{
  /**
   * Gets JNA pointer given a native address. Important: there is no way to keep
   * a reference of the parent object in a JNA Pointer. Holding the 'long' value
   * of the pointer is no garantee that the corresponding buffer will not be
   * deleted (opposite of malloc) if the holding object is garbage collected...
   *
   * @param pAddress
   *          address
   * @return BridJ pointer
   */
  public static Pointer getBridJPointer(long pAddress)
  {
    return new Pointer(pAddress);
  }
}
