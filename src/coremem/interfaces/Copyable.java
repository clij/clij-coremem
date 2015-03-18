package coremem.interfaces;


public interface Copyable<M extends Copyable<M>>
{
	public void copyTo(M pTo);

	public void copyFrom(M pFrom);

}
