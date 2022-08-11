package fun.madeby;

import fun.madeby.util.DebugInfo;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by Gra_m on 2022 06 30
 */

public interface DataHandlerGeneric {

	void populateIndex();
	void close() throws IOException;
	boolean isExistingData();
	byte[] readRawRecord(Long rowsBytePosition);
	Object readFromByteStream(final DataInputStream stream) throws IOException;
	Collection<DebugInfo> getCurrentDebugInfoRows() throws IOException;
	Boolean commit(Collection<Long> newRowsBytePosition, Collection<Long> deletedRowsBytePosition); // commit/rollback from DBSpecificServer
	Boolean rollback(Collection<Long> newRowsBytePosition, Collection<Long> deletedRowsBytePosition);

}
