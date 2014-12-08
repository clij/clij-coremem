package coremem.interfaces;

public interface MappableMemory
{
	public long map();

	public void force();

	public void unmap();

	public boolean isCurrentlyMapped();
}
