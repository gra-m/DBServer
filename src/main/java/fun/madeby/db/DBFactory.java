package fun.madeby.db;

import fun.madeby.db.generic_server.DBGenericServer;
import fun.madeby.db.specific_server.DBSpecificServer;
import fun.madeby.exceptions.DBException;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Gra_m on 2022 07 30
 */

public final class DBFactory {
	public static final String DEFAULT_ENCODING = "UTF-8"; // NOTE, changing this to UTF-16 will break all boolean reads as minimum byte size in utf16 == 2.

	public static DBSpecificServer getSpecificDB(final String dbFileName) throws IOException, DBException
		{
		return new DBSpecificServer(dbFileName);
	}

	public static DBGenericServer getGenericDB() throws  FileNotFoundException {
		return new DBGenericServer();
	}
}
