package fun.madeby.dbserver;

import fun.madeby.DBRecord;
import fun.madeby.exceptions.NameDoesNotExistException;
import fun.madeby.util.DebugInfo;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by Gra_m on 2022 06 27
 */

public interface DB extends Closeable {
	boolean add(DBRecord dbRecord) throws IOException;
	void update(Long rowNumber, final DBRecord dbRecord) throws IOException;
	void update(String name, final DBRecord dbRecord) throws NameDoesNotExistException, IOException;
	void delete(Long rowNumber) throws IOException;
	DBRecord read(Long rowNumber) throws IOException;
	void close() throws IOException;
	DBRecord search (String name);
	void refreshIndex();
	Collection<DebugInfo> getData();
}
