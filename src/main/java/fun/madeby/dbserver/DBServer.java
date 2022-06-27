package fun.madeby.dbserver;

import fun.madeby.DBRecord;
import fun.madeby.FileHandler;
import fun.madeby.Index;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Gra_m on 2022 06 27
 */

public final class DBServer implements DB{
	private FileHandler fileHandler;

	public DBServer(final String dbFileName) throws FileNotFoundException {
		this.fileHandler = new FileHandler(dbFileName);
		this.fileHandler.populateIndex();
	}

	public void close() throws IOException {
		//Index.getInstance().clear(); unnecessary
		this.fileHandler.close();
	}

	@Override
	public boolean add(DBRecord dbRecord) throws IOException {
		return this.fileHandler.add(dbRecord);
	}

	@Override
	public DBRecord delete(Long rowNumber) throws IOException {
		return null;
	}

	@Override
	public DBRecord read(Long rowNumber) throws IOException {
		return this.fileHandler.readRow(rowNumber);
	}
}
