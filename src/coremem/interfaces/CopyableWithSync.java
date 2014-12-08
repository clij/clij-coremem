package coremem.interfaces;

public interface CopyableWithSync<M>
{
	public void copyTo(M pTo, boolean pSync);

	public void copyFrom(M pFrom, boolean pSync);
}
