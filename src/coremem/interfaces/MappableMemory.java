package coremem.interfaces;

/**
 * Memory objects implementing this interface are 'mapped' and require calls to
 * map/unmap methods for access.
 *
 * @author royer
 */
public interface MappableMemory
{
  /**
   * Maps this memory object.
   * 
   * @return
   */
  public long map();

  public void force();

  /**
   * Unmaps this memory object.
   */
  public void unmap();

  /**
   * Returns true if this object is mapped, false otherwise.
   * 
   * @return true if mapped, false otherwise.
   */
  public boolean isCurrentlyMapped();
}
