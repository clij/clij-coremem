package coremem.interfaces;

import java.nio.Buffer;

public interface CopyFromToNIOBuffers
{
	public void copyTo(Buffer pTo);

	public void copyFrom(Buffer pFrom);

}
