package coremem.interfaces;

import org.bridj.Pointer;

public interface BridJPointerWrappable
{
	public <T> Pointer<T> getBridJPointer(Class<T> pTargetClass);
}
