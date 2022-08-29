package fun.madeby.db;

import fun.madeby.exceptions.DBException;
import fun.madeby.exceptions.DuplicateNameException;
import fun.madeby.generic.GenericIndexPool;
import fun.madeby.transaction.ITransaction;
import fun.madeby.util.DebugInfo;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by Gra_m on 2022 08 29
 */

public interface Table {

	String getTableName();

	void add(Object obj) throws DuplicateNameException, DBException;

	void update(Long rowNumber, final Object obj) throws DuplicateNameException, DBException;

	void update(String name, final Object obj) throws DuplicateNameException, DBException;

	void delete(Long rowNumber);

	Object read(Long rowNumber) throws DBException;

	void close() throws DBException, IOException;

	Object search (String name) throws DBException;

	void refreshTableIndex_WasGenericIndex() throws DBException;

	void defragmentTable_Tables_Question() throws IOException, DuplicateNameException, DBException;

	Collection<DebugInfo> getRowsWithDebugInfo() throws DBException;

	Collection<Object> searchWithLevenshtein(final String indexedFieldName, int tolerance) throws DBException;

	Collection<Object> searchWithRegex(final String regEx) throws DBException;

	ITransaction beginTransaction();

	void commit() throws DBException;

	void rollback() throws DBException;

	Long getTotalRecordAmount();

}
