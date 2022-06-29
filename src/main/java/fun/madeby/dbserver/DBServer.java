package fun.madeby.dbserver;

import fun.madeby.DBRecord;
import fun.madeby.FileHandler;
import fun.madeby.Index;
import fun.madeby.dbserver.exceptions.NameDoesNotExistException;

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

	/**
	 *
	 */
	@Override
	public void refreshIndex() {
		this.fileHandler.populateIndex();
	}

	@Override
	public boolean add(DBRecord dbRecord) throws IOException {
		return this.fileHandler.add(dbRecord);
	}

	/**
	 * @param rowNumber
	 * @throws IOException
	 */
	@Override
	public void update(Long rowNumber, final DBRecord newRecord) throws IOException {
		String name = null;
		try {
			if (checkRowNumber(rowNumber)) {
				DBRecord existingRowNumberRecord = read(rowNumber);
				assert existingRowNumberRecord != null;
				name = existingRowNumberRecord.getName();
				if (Index.getInstance().hasNameInIndex(name)) {
					this.fileHandler.updateByRow(rowNumber, newRecord, existingRowNumberRecord);
				} else
					throw new NameDoesNotExistException(String.format("The row you are trying to update with name ('%s') does not exist in the name index", name));
			}
		} catch (NameDoesNotExistException | IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void update(String name, final DBRecord newRecord) {
		try {
			if (Index.getInstance().hasNameInIndex(name)) {
				DBRecord existingRowNumberRecord = read(Index.getInstance().getRowNumberByName(name));
				this.fileHandler.updateByName(name, newRecord, existingRowNumberRecord);
			} else
				throw new NameDoesNotExistException(String.format("The name you are trying to update ('%s') does not exist", name));
		}catch (NameDoesNotExistException | IOException e) {
			e.printStackTrace();
		}


	}

	@Override
	public void delete(Long rowNumber) throws IOException {
		if (checkRowNumber(rowNumber)) {
			// DBRecord necessary to delete name index entry
			DBRecord existingRowNumberRecord = read(rowNumber);
			this.fileHandler.deleteRow(rowNumber, existingRowNumberRecord);
		}

	}



	@Override
	public DBRecord read(Long rowNumber) throws IOException {
		if (checkRowNumber(rowNumber))
			return this.fileHandler.readRow(rowNumber);
		return null;
	}

	private boolean checkRowNumber(Long rowNumber) {
		try {
			if (rowNumber < 0) {
				throw new IOException("Row number is less than 0");
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
		return true;
	}
}
