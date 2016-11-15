package coremem.recycling;


/**
 *
 *
 * @param <R>
 * @param <P>
 * @author royer
 */
public interface RecyclableFactoryInterface<R extends RecyclableInterface<R, P>, P extends RecyclerRequestInterface>
{
	/**
	 * @param pParameters
	 * @return
	 */
	R create(P pParameters);
}
