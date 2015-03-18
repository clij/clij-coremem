package coremem.interop;

import org.bridj.Pointer;
import org.bridj.Pointer.Releaser;
import org.bridj.PointerIO;

public class BridJInterop
{
	public static <T> Pointer<T> wrapWithBridJPointer(	Class<T> pTargetClass,
																				long pAddress,
																				long pSizeInBytes,
																				Releaser pReleaser)
	{

		PointerIO<T> lPointerIO = PointerIO.getInstance(pTargetClass);

		Pointer<T> lPointerToAddress = Pointer.pointerToAddress(pAddress,
																														pSizeInBytes,
																														lPointerIO,
																														pReleaser);

		return lPointerToAddress;

	}
}
