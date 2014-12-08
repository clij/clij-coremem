package coremem.rgc;

public interface Freeable
{
	public void free();

	public boolean isFree();

	public void complainIfFreed() throws FreedException;

}
