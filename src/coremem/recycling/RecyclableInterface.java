package coremem.recycling;

import coremem.interfaces.SizedInBytes;
import coremem.rgc.Freeable;

/**
 *
 *
 * @param <O>
 * @param <P>
 * @author royer
 */
public interface RecyclableInterface<O extends RecyclableInterface<O, P>, P extends RecyclerRequestInterface>	extends
																																																				SizedInBytes,
																																																				Freeable
{

	/**
	 * @param pParameters
	 * @return
	 */
	boolean isCompatible(P pParameters);

	/**
	 * @param pParameters
	 */
	void recycle(P pParameters);

	/**
	 * @param pRecycler
	 */
	void setRecycler(RecyclerInterface<O, P> pRecycler);

	/**
	 * @param pIsReleased
	 */
	void setReleased(boolean pIsReleased);

	/**
	 * @return
	 */
	boolean isReleased();

	/**
	 * 
	 */
	void release();

}
