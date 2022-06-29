package fun.madeby.dbserver;

import fun.madeby.DBRecord;
import fun.madeby.dbserver.exceptions.NameDoesNotExistException;

import java.io.IOException;

/**
 * Created by Gra_m on 2022 06 27
 */

public interface DB {
	boolean add(DBRecord dbRecord) throws IOException;
	void update(Long rowNumber, final DBRecord dbRecord) throws IOException;
	void update(String name, final DBRecord dbRecord) throws NameDoesNotExistException, IOException;
	void delete(Long rowNumber) throws IOException;
	DBRecord read(Long rowNumber) throws IOException;
	void close() throws IOException;
	void refreshIndex();
}
