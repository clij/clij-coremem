package coremem.recycling;


public interface RecyclableFactory<O extends RecyclableInterface<O, P>, P extends RecyclerRequest>
{

	O create(P pParameters);

}
