package fun.madeby.dbserver;

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

	boolean add(DBRecord dbRecord);

	void update(Long rowNumber, final DBRecord dbRecord);

	void update(String name, final DBRecord dbRecord);

	void delete(Long rowNumber);

	DBRecord read(Long rowNumber);

	void close() throws IOException;

	DBRecord search (String name);

	void refreshIndex();

	void defragmentDatabase() throws IOException;

	Collection<DebugInfo> getData();

	Collection<DBRecord> searchWithLevenshtein(final String name, int tolerance) throws IOException;

	Collection<DBRecord> searchWithRegex(final String regEx) throws IOException;

	ITransaction beginTransaction();

	void commit();

	void rollback();
}
