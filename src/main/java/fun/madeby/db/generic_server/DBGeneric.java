package fun.madeby.db.generic_server;

import fun.madeby.exceptions.DBException;
import fun.madeby.exceptions.DuplicateNameException;
import fun.madeby.transaction.ITransaction;
import fun.madeby.util.DebugInfo;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by Gra_m on 2022 06 27
 */

public interface DBGeneric extends Closeable {

	void add(Object obj) throws DuplicateNameException, DBException;

	void update(Long rowNumber, final Object obj) throws DuplicateNameException, DBException;

	void update(String name, final Object obj) throws DuplicateNameException, DBException;

	void delete(Long rowNumber);

	Object read(Long rowNumber);

	void close() throws IOException;

	Object search (String name);

	void refreshGenericIndex();

	void defragmentDatabase() throws IOException, DuplicateNameException, DBException;

	Collection<DebugInfo> getRowsWithDebugInfo();

	Collection<Object> searchWithLevenshtein(final String indexedFieldName, int tolerance);

	Collection<Object> searchWithRegex(final String regEx);

	ITransaction beginTransaction();

	void commit();

	void rollback();

	Long getTotalRecordAmount();
}
