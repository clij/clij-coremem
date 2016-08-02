package coremem.recycling;


public interface RecyclableFactoryInterface<R extends RecyclableInterface<R, P>, P extends RecyclerRequestInterface>
{
	R create(P pParameters);
}
