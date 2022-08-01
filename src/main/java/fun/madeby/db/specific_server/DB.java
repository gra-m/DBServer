package fun.madeby.db.specific_server;

import fun.madeby.DBRecord;
import fun.madeby.transaction.ITransaction;
import fun.madeby.util.DebugInfo;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by Gra_m on 2022 06 27
 */

public interface DB extends Closeable {

	void add(DBRecord object);

	void update(Long rowNumber, final DBRecord object);

	void update(String name, final DBRecord object);

	void delete(Long rowNumber);

	DBRecord read(Long rowNumber);

	void close() throws IOException;

	DBRecord search (String name);

	void refreshIndex();

	void defragmentDatabase() throws IOException;

	Collection<DebugInfo> getRowsWithDebugInfo();

	Collection<DBRecord> searchWithLevenshtein(final String name, int tolerance);

	Collection<DBRecord> searchWithRegex(final String regEx);

	ITransaction beginTransaction();

	void commit();

	void rollback();

	Long getTotalRecordAmount();
}
