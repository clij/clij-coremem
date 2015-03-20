package coremem.recycling;


public interface RecyclableFactory<R extends RecyclableInterface<R, P>, P extends RecyclerRequest>
{

	R create(P pParameters);

}
