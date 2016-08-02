package coremem.recycling;

public interface RecyclerListenerInterface
{
	void update(int pNumberOfLiveObjects,
							int pNumberOfAvailableObjects,
							long pNumberOfFailedRequest);

}
