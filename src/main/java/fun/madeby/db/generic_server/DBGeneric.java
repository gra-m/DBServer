package fun.madeby.db.generic_server;

import fun.madeby.transaction.ITransaction;
import fun.madeby.util.DebugInfo;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by Gra_m on 2022 06 27
 */

public interface DBGeneric extends Closeable {

	void add(Object obj);

	void update(Long rowNumber, final Object obj);

	void update(String name, final Object obj);

	void delete(Long rowNumber);

	Object read(Long rowNumber);

	void close() throws IOException;

	Object search (String name);

	void refreshGenericIndex();

	void defragmentDatabase() throws IOException;

	Collection<DebugInfo> getRowsWithDebugInfo();

	Collection<Object> searchWithLevenshtein(final String indexedFieldName, int tolerance);

	Collection<Object> searchWithRegex(final String regEx);

	ITransaction beginTransaction();

	void commit();

	void rollback();

	Long getTotalRecordAmount();
}
