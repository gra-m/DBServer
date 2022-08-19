package fun.madeby.db.specific_server;

import fun.madeby.DBRecord;
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

public interface DB extends Closeable {

	void add(DBRecord object) throws DuplicateNameException;

	void update(Long rowNumber, final DBRecord object) throws DuplicateNameException, DBException;

	void update(String name, final DBRecord object) throws DuplicateNameException;

	void delete(Long rowNumber);

	DBRecord read(Long rowNumber) throws DBException;

	void close() throws IOException;

	DBRecord search (String name) throws DBException;

	void refreshIndex() throws IOException, DBException;

	void defragmentDatabase() throws IOException, DuplicateNameException, DBException;

	Collection<DebugInfo> getRowsWithDebugInfo() throws DBException;

	Collection<DBRecord> searchWithLevenshtein(final String name, int tolerance) throws DBException;

	Collection<DBRecord> searchWithRegex(final String regEx) throws DBException;

	ITransaction beginTransaction();

	void commit() throws DBException;

	void rollback() throws DBException;

	Long getTotalRecordAmount();
}
