package fun.madeby;

import fun.madeby.util.DebugInfo;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by Gra_m on 2022 06 30
 */

public interface DataHandler {

	void populateIndex();
	void close() throws IOException;
	boolean checkForExistingData();
	byte[] readRawRecord(Long rowsBytePosition);
	DBRecord readFromByteStream(final DataInputStream stream) throws IOException;
	Collection<DebugInfo> getCurrentData();


}
