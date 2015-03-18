package coremem.interfaces;


public interface CopyFromToJavaArray
{
	public void copyTo(byte[] pTo);

	public void copyTo(short[] pTo);

	public void copyTo(char[] pTo);

	public void copyTo(int[] pTo);

	public void copyTo(long[] pTo);

	public void copyTo(float[] pTo);

	public void copyTo(double[] pTo);

	public void copyFrom(byte[] pFrom);

	public void copyFrom(short[] pFrom);

	public void copyFrom(char[] pFrom);

	public void copyFrom(int[] pFrom);

	public void copyFrom(long[] pFrom);

	public void copyFrom(float[] pFrom);

	public void copyFrom(double[] pFrom);

}
