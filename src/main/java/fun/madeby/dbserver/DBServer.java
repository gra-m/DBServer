package fun.madeby.dbserver;

import fun.madeby.DBRecord;
import fun.madeby.FileHandler;
import fun.madeby.Index;
import fun.madeby.exceptions.NameDoesNotExistException;
import fun.madeby.util.DebugInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;

/**
 * Created by Gra_m on 2022 06 27
 */

public final class DBServer implements DB{
	private FileHandler fileHandler;

	public DBServer(final String dbFileName) throws FileNotFoundException {
		this.fileHandler = new FileHandler(dbFileName);
		this.initialise();
	}

	private void initialise() {
		this.fileHandler.populateIndex();
	}

	public void close() throws IOException {
		Index.getInstance().clear();
		this.fileHandler.close();
	}

	@Override
	public void defragmentDatabase() throws IOException {
		String prefix = "defrag";
		String suffix = "dat";

		File tmpFile = File.createTempFile(prefix, suffix);
		Index.getInstance().clear();

		// open temp file:
		FileHandler defragFH = new FileHandler(new RandomAccessFile(tmpFile, "rw"), tmpFile.getName());

		Collection<DebugInfo> currentFHData = this.fileHandler.getCurrentData();

		for(DebugInfo info: currentFHData) {
			if (info.isDeleted())
				continue;
			DBRecord dbRecord = info.getDbRecord();
			defragFH.add(dbRecord);
		}

		replaceOldFileWithNew(tmpFile);
		defragFH.close();
		Index.getInstance().clear();
		this.initialise();
	}

	private void replaceOldFileWithNew(File tmpFile) throws IOException {
		String oldDBName = this.fileHandler.getDbFileName();
		boolean oldFileDeleted = this.fileHandler.deleteFile();
		try {
			if (oldFileDeleted) {
				this.fileHandler.close();
				Files.copy(tmpFile.toPath(), FileSystems.getDefault().getPath("", oldDBName),
						StandardCopyOption.REPLACE_EXISTING);
				this.fileHandler = new FileHandler("oldDBName");
			} else {
				tmpFile.delete();
				this.initialise(); //
				throw new IOException("Old DB file could not be deleted, defrag failed, check logs");
			}
		}catch (IOException e) {
			e.printStackTrace();
		}


	}

	@Override
	public DBRecord search(String name) {
		try {
			return this.fileHandler.search(name);
		}catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void refreshIndex() {
		this.fileHandler.populateIndex();
	}



	/**
	 * @return
	 */
	@Override
	public Collection<DebugInfo> getData() {
		return this.fileHandler.getCurrentData();
	}

	/**
	 * @param name
	 * @param tolerance
	 * @return
	 */
	@Override
	public Collection<DBRecord> searchWithLevenshtein(String name, int tolerance) throws IOException {
		return this.fileHandler.searchWithLevenshtein(name, tolerance);
	}

	/**
	 * @param regEx
	 * @return
	 */
	@Override
	public Collection<DBRecord> searchWithRegex(String regEx) throws IOException {
		return this.fileHandler.searchWithRegex(regEx);
	}

	@Override
	public boolean add(DBRecord dbRecord) {
		try {
			return this.fileHandler.add(dbRecord);
		}catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @param rowNumber
	 * @throws IOException
	 */
	@Override
	public void update(Long rowNumber, final DBRecord newRecord) throws IOException {
		String name;
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
		} catch (NameDoesNotExistException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void update(String name, DBRecord newRecord) {
		try {
			if (Index.getInstance().hasNameInIndex(name)) {
				DBRecord existingRowNumberRecord = read(Index.getInstance().getRowNumberByName(name));
				this.fileHandler.updateByName(name, newRecord, existingRowNumberRecord);
			} else
				throw new NameDoesNotExistException(String.format("The name you are trying to update ('%s') does not exist", name));
		}catch (NameDoesNotExistException e) {
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
	public DBRecord read(Long rowNumber) {
		try {
			if (checkRowNumber(rowNumber))
				return this.fileHandler.readRow(rowNumber);
		}catch (IOException e) {
			e.printStackTrace();
		}
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
