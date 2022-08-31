package fun.madeby.db.generic_server;

import fun.madeby.db.Table;
import fun.madeby.exceptions.DBException;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Gra_m on 2022 06 27
 */

public interface DBGeneric extends Closeable {

	Table useTable(final String tableName, final String tableSchema, final Class aClass) throws DBException, FileNotFoundException;
	Boolean dropCurrentTable() throws IOException, DBException;
	void close() throws IOException;
	void closeCurrentTable() throws IOException, DBException;

	void suspendCurrentTable();
}
