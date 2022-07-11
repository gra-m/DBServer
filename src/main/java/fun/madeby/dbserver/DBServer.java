package fun.madeby.dbserver;

import fun.madeby.DBRecord;
import fun.madeby.FileHandler;
import fun.madeby.Index;
import fun.madeby.exceptions.NameDoesNotExistException;
import fun.madeby.transaction.ITransaction;
import fun.madeby.transaction.Transaction;
import fun.madeby.util.DebugInfo;
import fun.madeby.util.LoggerSetUp;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Gra_m on 2022 06 27
 */

public final class DBServer implements DB{
	private FileHandler fileHandler;
	private  Logger LOGGER;
	private Map<Long, ITransaction> transactions;

	{
		try {
			LOGGER = LoggerSetUp.setUpLogger("DbServer");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public DBServer(final String dbFileName) throws FileNotFoundException {
		this.fileHandler = new FileHandler(dbFileName);
		this.transactions = new LinkedHashMap<>();
		this.initialise();
	}

	private void initialise() {
		this.fileHandler.populateIndex();
	}

	@Override
	public ITransaction beginTransaction() {
		long threadId = Thread.currentThread().getId(); // If we know the thread id we can store the transaction
		ITransaction transaction = new Transaction();
		this.transactions.put(threadId, transaction); // now DB knows which transactions belong to this thread.. but if multiple wont work.
		return new Transaction();
	}

	@Override
	public void commit() {

	}


	@Override
	public void rollback() {

	}

	public void close() throws IOException {
		Index.getInstance().clear();
		this.fileHandler.close();
	}

	@Override
	public void defragmentDatabase() throws IOException {
		LOGGER.info("@defragmentDatabase()");
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

	private void replaceOldFileWithNew(File tmpFile) {
		String oldDBName = this.fileHandler.getDbFileName();
		boolean oldFileDeleted = this.fileHandler.deleteFile();
		try {
			if (oldFileDeleted) {
				this.fileHandler.close();
				Files.copy(tmpFile.toPath(), FileSystems.getDefault().getPath("", oldDBName),
						StandardCopyOption.REPLACE_EXISTING);
				this.fileHandler = new FileHandler(oldDBName);
			} else {
				boolean tmpFileDeleted = tmpFile.delete();
				LOGGER.severe("Database file could not be deleted during defragmentation ||" +
						" \nOutcome for tmpFile.delete() (necessary on fail) == " + tmpFileDeleted);
				this.initialise();
				throw new IOException("Old DB file could not be deleted, defrag failed, check logs");
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public DBRecord search(String name) {
		LOGGER.info("@search(String name)" + name);
		return this.fileHandler.search(name);
	}

	@Override
	public void refreshIndex() {
		LOGGER.info("@refreshIndex()");
		this.fileHandler.populateIndex();
	}

	@Override
	public Collection<DebugInfo> getData() {
		LOGGER.info("@getData()");
		return this.fileHandler.getCurrentData();
	}

	@Override
	public Collection<DBRecord> searchWithLevenshtein(String name, int tolerance) throws IOException {
		LOGGER.info("@searchWithLevenshtein() " + name + " " + tolerance);
		return this.fileHandler.searchWithLevenshtein(name, tolerance);
	}

	@Override
	public Collection<DBRecord> searchWithRegex(String regEx) throws IOException {
		LOGGER.info("@searchWithRegex() " + regEx);
		return this.fileHandler.searchWithRegex(regEx);
	}


	@Override
	public boolean add(DBRecord dbRecord) {
		LOGGER.info("@add(DBRecord), dbRecord: " + dbRecord);
		return this.fileHandler.add(dbRecord);
	}

	@Override
	public void update(Long rowNumber, final DBRecord newRecord) {
		LOGGER.info("@update(rowNumberOldRecord, newRecord) " + rowNumber + " "  + newRecord);
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
		LOGGER.info("@update(name, newRecord) " + name + " "  + newRecord.getName());
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
	public void delete(Long rowNumber) {
		LOGGER.info("@delete(rowNumber) " + rowNumber);
		if (checkRowNumber(rowNumber)) {
			// DBRecord necessary to delete name index entry
			DBRecord existingRowNumberRecord = read(rowNumber);
			this.fileHandler.deleteRow(rowNumber, existingRowNumberRecord);
		}

	}

	@Override
	public DBRecord read(Long rowNumber) {
		LOGGER.info("@read(rowNumber) " + rowNumber);
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
