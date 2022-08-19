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

	Object read(Long rowNumber) throws DBException;

	void close() throws IOException;

	Object search (String name) throws DBException;

	void refreshGenericIndex() throws DBException;

	void defragmentDatabase() throws IOException, DuplicateNameException, DBException;

	Collection<DebugInfo> getRowsWithDebugInfo() throws DBException;

	Collection<Object> searchWithLevenshtein(final String indexedFieldName, int tolerance) throws DBException;

	Collection<Object> searchWithRegex(final String regEx) throws DBException;

	ITransaction beginTransaction();

	void commit() throws DBException;

	void rollback() throws DBException;

	Long getTotalRecordAmount();
}
