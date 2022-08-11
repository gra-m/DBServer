package fun.madeby.db;

import fun.madeby.db.generic_server.DBGenericServer;
import fun.madeby.db.specific_server.DBServer;
import fun.madeby.exceptions.DBException;

import java.io.FileNotFoundException;

/**
 * Created by Gra_m on 2022 07 30
 */

public final class DBFactory {

	public static DBServer getSpecificDB(final String dbFileName) throws FileNotFoundException {
		return new DBServer(dbFileName);
	}

	public static DBGenericServer getGenericDB(final String dbFileName, final String schema, final Class classTemplate) throws DBException, FileNotFoundException {
		return new DBGenericServer(dbFileName, schema,  classTemplate);
	}
}
