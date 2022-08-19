package fun.madeby;

import fun.madeby.exceptions.DBException;
import fun.madeby.util.DebugInfo;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by Gra_m on 2022 06 30
 */

public interface DataHandler {

	void populateIndex() throws IOException, DBException;
	void close() throws IOException;
	boolean isExistingData() throws IOException;
	byte[] readRawRecord(Long rowsBytePosition) throws DBException;
	DBRecord readFromByteStream(final DataInputStream stream) throws IOException, DBException;
	Collection<DebugInfo> getCurrentDebugInfoRows() throws DBException;
	Boolean commit(Collection<Long> newRowsBytePosition, Collection<Long> deletedRowsBytePosition) throws DBException; // commit/rollback from DBSpecificServer
	Boolean rollback(Collection<Long> newRowsBytePosition, Collection<Long> deletedRowsBytePosition) throws DBException;

}
