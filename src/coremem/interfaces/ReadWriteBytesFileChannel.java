package coremem.interfaces;

import java.io.IOException;
import java.nio.channels.FileChannel;

public interface ReadWriteBytesFileChannel
{


	public long writeBytesToFileChannel(final FileChannel pFileChannel,
																			final long pFilePositionInBytes) throws IOException;

	public long writeBytesToFileChannel(final long pBufferPositionInBytes,
																			final FileChannel pFileChannel,
																			final long pFilePositionInBytes,
																			final long pLengthInBytes) throws IOException;

	public long readBytesFromFileChannel(	final FileChannel pFileChannel,
																				final long pFilePositionInBytes,
																				final long pLengthInBytes) throws IOException;

	public long readBytesFromFileChannel(	final long pBufferPositionInBytes,
																				final FileChannel pFileChannel,
																				final long pFilePositionInBytes,
																				final long pLengthInBytes) throws IOException;

}
