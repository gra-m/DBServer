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
		Index.getInstance().clear();
		this.fileHandler.close();
	}

	@Override
	public boolean add(DBRecord dbRecord) throws IOException {
		return this.fileHandler.add(dbRecord);
	}

	@Override
	public void delete(Long rowNumber) throws IOException {
		checkRowNumber(rowNumber);
		this.fileHandler.deleteRow(rowNumber);

	}



	@Override
	public DBRecord read(Long rowNumber) throws IOException {
		checkRowNumber(rowNumber);
		return this.fileHandler.readRow(rowNumber);
	}

	private void checkRowNumber(Long rowNumber) {
		try {
			if (rowNumber < 0)
				throw new IOException("Row number is less than 0");
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
