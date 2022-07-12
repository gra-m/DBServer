package fun.madeby;

import fun.madeby.util.DebugInfo;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Created by Gra_m on 2022 06 30
 */

public interface DataHandler {

	void populateIndex();
	void close() throws IOException;
	boolean isExistingData();
	byte[] readRawRecord(Long rowsBytePosition);
	DBRecord readFromByteStream(final DataInputStream stream) throws IOException;
	Collection<DebugInfo> getCurrentData();
	void commit(Collection<Long> newRowsBytePosition, Collection<Long> deletedRowsBytePosition); // commit/rollback from DBServer
	void rollback(Collection<Long> newRowsBytePosition, Collection<Long> deletedRowsBytePosition);

}
