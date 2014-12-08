package coremem.interfaces;

public interface MapAndReadWrite extends MappableMemory
{

	void mapAndReadFrom(PointerAccessible pPointerAccessible);

	void mapAndWriteTo(PointerAccessible pPointerAccessible);

}
