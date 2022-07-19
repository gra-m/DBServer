package fun.madeby.dbserver;

import fun.madeby.DBRecord;
import fun.madeby.FileHandler;
import fun.madeby.Index;
import fun.madeby.exceptions.NameDoesNotExistException;
import fun.madeby.transaction.ITransaction;
import fun.madeby.transaction.Transaction;
import fun.madeby.util.DebugInfo;
import fun.madeby.util.LoggerSetUp;
import fun.madeby.util.OperationUnit;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by Gra_m on 2022 06 27
 */

public final class DBServer implements DB{
	private FileHandler fileHandler;
	private  Logger LOGGER;
	public Map<Long, ITransaction> transactions;

	{
		try {
			LOGGER = LoggerSetUp.setUpLogger("DbServer");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public DBServer(final String dbFileName) throws FileNotFoundException {
		LOGGER.finest("@DBServer(String dbFileName) = " + dbFileName);
		this.fileHandler = new FileHandler(dbFileName);
		this.transactions = new LinkedHashMap<>();
		this.initialise();
	}

	private void initialise() {
		LOGGER.finest("@DBServer intialise()");
		this.fileHandler.writeVersionInfoIfNewFile();
		this.fileHandler.populateIndex();
	}

	@Override
	public ITransaction beginTransaction() {
		LOGGER.finest("@DBServer beginTransaction()");
		long threadId = Thread.currentThread().getId();
		boolean threadAlreadyHasTransaction = transactions.containsKey(threadId);

		if(threadAlreadyHasTransaction)
			return transactions.get(threadId);

		ITransaction transaction = new Transaction();
		this.transactions.put(threadId, transaction);
		return new Transaction();
	}

	@Override
	public void commit() {
		LOGGER.finest("@DBServer commit() entered");
		ITransaction transaction = getTransaction();

		if (transaction != null) {
		Boolean successfullyCommitted = fileHandler.
				commit(transaction.getNewRowsBytePosition(), transaction.getDeletedRowsBytePosition());
		if(successfullyCommitted) {
			transactions.remove(Thread.currentThread().getId()); // cannot be retrieved..
			transaction.clear();// clearing data in object that can no longer be retrieved...
			LOGGER.info("@DBServer commit() completed");
		}
		} else
			LOGGER.info("@DBServer commit() transaction could not be found");
	}

	@Override
	public void rollback() {
		LOGGER.finest("@DBServer rollback() entered");
		ITransaction transaction = getTransaction();
		if (transaction == null)
			return;

		Boolean successfullyRolledBack = fileHandler.rollback(transaction.getNewRowsBytePosition(), transaction.getDeletedRowsBytePosition());

		if(successfullyRolledBack){
			transactions.remove(Thread.currentThread().getId()); // cannot be retrieved..
			transaction.clear();// clearing data in object that can no longer be retrieved...
		}
		LOGGER.info("@DBServer rollback() completed");
	}


	private ITransaction getTransaction() {
		LOGGER.finest("@DBServer getTransaction()");
		long threadId = Thread.currentThread().getId();
		return transactions.getOrDefault(threadId, null);
	}

	public void close() throws IOException {
		LOGGER.info("@DBServer close()");
		Index.getInstance().clear();
		this.fileHandler.close();
	}

	@Override
	public void defragmentDatabase() throws IOException {
		LOGGER.finest("@DBServer defragmentDatabase()");
		String prefix = "defrag";
		String suffix = "dat";

		File tmpFile = File.createTempFile(prefix, suffix);
		Index.getInstance().clear();

		// open temp file:
		FileHandler defragFH = new FileHandler(new RandomAccessFile(tmpFile, "rw"), tmpFile.getName());

		Collection<DebugInfo> currentDebugInfoRows = this.fileHandler.getCurrentDebugInfoRows();

		for(DebugInfo info: currentDebugInfoRows) {
			if (info.isDeleted() || info.isTemporary())
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
		LOGGER.finest("@DBServer replaceOldFileWithNew(File tmpFile) = " + tmpFile.getName());
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
				LOGGER.warning("@DBServer @replaceOldFileWithNew(File tmpFile)\n->Database file could not be deleted during defragmentation ||" +
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
		LOGGER.finest("@DBServer @search(String name) = " + name);
		DBRecord dbRecord = this.fileHandler.search(name);
		LOGGER.info("@DBServer search(String name) return. name = " + name + " " + dbRecord);
		return dbRecord;
	}

	@Override
	public void refreshIndex() {
		LOGGER.finest("@DBServer @refreshIndex()");
		this.fileHandler.populateIndex();
	}

	@Override
	public Collection<DebugInfo> getRowsWithDebugInfo() {
		LOGGER.finest("@DBServer @getData()");
		return this.fileHandler.getCurrentDebugInfoRows();
	}

	@Override
	public Collection<DBRecord> searchWithLevenshtein(String name, int tolerance) {
		LOGGER.finest("@DBServer @searchWithLevenshtein(String name, int tolerance) = " + name + " " + tolerance);
		Collection<DBRecord> recordCollection =  this.fileHandler.searchWithLevenshtein(name, tolerance);
		LOGGER.info("@DBServer @searchWithLevenshtein(String name, int tolerance) + returned Collection<DBRecord> = " + name + " " + tolerance + "\n" + getCollectionContents(recordCollection));
		return recordCollection;
	}

	@Override
	public Collection<DBRecord> searchWithRegex(String regEx)  {
		LOGGER.finest("@DBServer @searchWithRegex(String regEx) = " + regEx);
		Collection<DBRecord> recordCollection = fileHandler.searchWithRegex(regEx);
		LOGGER.info("@DBServer @searchWithRegex(String regEx) + returned Collection<DBRecord> = " + regEx  + "\n" + getCollectionContents(recordCollection));
		return recordCollection;
	}


	@Override
	public void add(DBRecord dbRecord) {
		LOGGER.finest("@DBServer @add(DBRecord) = " + dbRecord);
		OperationUnit operationUnit =  this.fileHandler.add(dbRecord);
		getTransaction().registerAdd(operationUnit.addedRowBytePosition);
	}

	@Override
	public void update(Long rowNumber, final DBRecord newRecord) {
		LOGGER.finest("@DBServer @update(Long rowNumberOldRecord, DBRecord newRecord) = " + rowNumber + " "  + newRecord);
		String name;
		try {
			if (checkRowNumber(rowNumber)) {
				DBRecord existingRowNumberRecord = read(rowNumber); // todo remove existingRowNumberRecord?? once Transactions working?
				assert existingRowNumberRecord != null;
				name = existingRowNumberRecord.getName();
				if (Index.getInstance().hasNameInIndex(name)) {
					OperationUnit operationUnit = this.fileHandler.updateByRow(rowNumber, newRecord, existingRowNumberRecord);
					ITransaction transaction = getTransaction();
					transaction.registerAdd(operationUnit.addedRowBytePosition);
					transaction.registerDelete(operationUnit.deletedRowBytePosition);
				} else
					throw new NameDoesNotExistException(String.format("The row you are trying to update with name ('%s') does not exist in the name index", name));
			}
		} catch (NameDoesNotExistException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void update(String name, DBRecord newRecord) {
		LOGGER.finest("@DBServer @update(name, newRecord) " + name + " "  + newRecord.getName());
		try {
			if (Index.getInstance().hasNameInIndex(name)) {
				DBRecord existingRowNumberRecord = read(Index.getInstance().getRowNumberByName(name));
				OperationUnit operationUnit = this.fileHandler.updateByName(name, newRecord, existingRowNumberRecord);
				ITransaction transaction = getTransaction();
				transaction.registerAdd(operationUnit.addedRowBytePosition);
				transaction.registerDelete(operationUnit.deletedRowBytePosition);

			} else
				throw new NameDoesNotExistException(String.format("The name you are trying to update ('%s') does not exist", name));
		}catch (NameDoesNotExistException e) {
			e.printStackTrace();
		}


	}

	@Override
	public void delete(Long rowNumber) {
		LOGGER.finest("@DBServer @delete(rowNumber) = " + rowNumber);
		if (checkRowNumber(rowNumber)) {
			DBRecord existingRowNumberRecord = read(rowNumber); // todo necessary to delete name index entry explore
			OperationUnit operationUnit = this.fileHandler.deleteRow(rowNumber, existingRowNumberRecord);
			this.getTransaction().registerDelete(operationUnit.deletedRowBytePosition);
		}

	}

	@Override
	public DBRecord read(Long rowNumber) {
		LOGGER.finest("@DBServer @read(rowNumber) = " + rowNumber);
		DBRecord dbRecord = null;
		if (checkRowNumber(rowNumber)) {
			dbRecord = this.fileHandler.readRow(rowNumber);
			LOGGER.info("@DBServer read(Long rowNumber) return. rowNumber = " + rowNumber + " " + dbRecord);
			return dbRecord;
		}
		return dbRecord;
	}


	private boolean checkRowNumber(Long rowNumber) {
		LOGGER.finest("@DBServer @checkRowNumber(rowNumber) >0 = " + rowNumber);
		try {
			if (rowNumber < 0) {
				throw new IOException("Row number is less than 0");
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	private StringBuilder getCollectionContents(final Collection<DBRecord> recordCollection) {
		StringBuilder sb = new StringBuilder();

		for(DBRecord dbr: recordCollection) {
			sb.append(dbr.toString());
			sb.append(System.getProperty("line.separator"));
		}

		return sb;
	}
	@Override
	public Long getTotalRecordAmount() {
		return Index.getInstance().getTotalNumberOfRows();
	}
}
