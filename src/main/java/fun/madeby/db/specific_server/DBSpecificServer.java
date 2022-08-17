package fun.madeby.db.specific_server;

import fun.madeby.DBRecord;
import fun.madeby.exceptions.DuplicateNameException;
import fun.madeby.specific.FileHandler;
import fun.madeby.specific.Index;
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

public final class DBSpecificServer implements DB {
	private FileHandler fileHandler;
	private Logger LOGGER;
	public Map<Long, ITransaction> transactions;

	{
		try {
			LOGGER = LoggerSetUp.setUpLogger("DbSpecificServer");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public DBSpecificServer(final String dbFileName) throws IOException
		{
			LOGGER.finest("@DBSpecificServer(String dbFileName) = " + dbFileName);
			this.fileHandler = new FileHandler(dbFileName);
			this.transactions = new LinkedHashMap<>();
			this.initialise();
		}

	private void initialise() throws IOException
		{
			LOGGER.finest("@DBSpecificServer intialise()");
			this.fileHandler.writeVersionInfoIfNewFile();
			this.fileHandler.populateIndex();
		}

	@Override
	public ITransaction beginTransaction()
		{
			LOGGER.finest("@DBSpecificServer beginTransaction()");
			long threadId = Thread.currentThread().getId();
			boolean threadAlreadyHasTransaction = transactions.containsKey(threadId);

			if (threadAlreadyHasTransaction)
				return transactions.get(threadId);

			ITransaction transaction = new Transaction();
			this.transactions.put(threadId, transaction);
			return new Transaction();
		}

	@Override
	public void commit()
		{
			LOGGER.finest("@DBSpecificServer commit() entered");
			ITransaction transaction = getTransaction();

			if (transaction != null) {
				Boolean successfullyCommitted = fileHandler.
						commit(transaction.getNewRowsBytePosition(), transaction.getDeletedRowsBytePosition());
				if (successfullyCommitted) {
					transactions.remove(Thread.currentThread().getId()); // cannot be retrieved..
					transaction.clear();// clearing data in object that can no longer be retrieved...
					LOGGER.info("@DBSpecificServer commit() completed");
				}
			} else
				LOGGER.info("@DBSpecificServer commit() transaction could not be found");
		}

	@Override
	public void rollback()
		{
			LOGGER.finest("@DBSpecificServer rollback() entered");
			ITransaction transaction = getTransaction();
			if (transaction == null)
				return;

			Boolean successfullyRolledBack = fileHandler.rollback(transaction.getNewRowsBytePosition(), transaction.getDeletedRowsBytePosition());

			if (successfullyRolledBack) {
				transactions.remove(Thread.currentThread().getId()); // cannot be retrieved..
				transaction.clear();// clearing data in object that can no longer be retrieved...
			}
			LOGGER.info("@DBSpecificServer rollback() completed");
		}


	private ITransaction getTransaction()
		{
			LOGGER.finest("@DBSpecificServer getTransaction()");
			long threadId = Thread.currentThread().getId();
			return transactions.getOrDefault(threadId, null);
		}

	public void close() throws IOException
		{
			LOGGER.severe("@DBSpecificServer closing()");
			Index.getInstance().clear();
			this.fileHandler.close();
		}

	@Override
	public void defragmentDatabase() throws IOException, DuplicateNameException
		{
			LOGGER.finest("@DBSpecificServer defragmentDatabase()");
			String prefix = "defrag";
			String suffix = "dat";

			File tmpFile = File.createTempFile(prefix, suffix);
			Index.getInstance().clear();

			// open temp file:
			FileHandler defragFH = new FileHandler(new RandomAccessFile(tmpFile, "rw"), tmpFile.getName());
			defragFH.writeVersionInfoIfNewFile();

			Collection<DebugInfo> currentDebugInfoRows = this.fileHandler.getCurrentDebugInfoRows();

			for (DebugInfo info : currentDebugInfoRows) {
				if (info.isDeleted() || info.isTemporary())
					continue;
				DBRecord object = (DBRecord) info.getDbRecord();
				defragFH.add(object, true);
			}

			replaceOldFileWithNew(tmpFile);
			defragFH.close();
			Index.getInstance().clear();
			this.initialise();
		}

	private void replaceOldFileWithNew(File tmpFile)
		{
			LOGGER.finest("@DBSpecificServer replaceOldFileWithNew(File tmpFile) = " + tmpFile.getName());
			String oldDBName = this.fileHandler.getDbFileName();
			boolean oldFileDeleted = this.fileHandler.deleteFile();
			try {
				if (oldFileDeleted) {
					this.fileHandler.close();
					Files.copy(tmpFile.toPath(), FileSystems.getDefault().getPath("", oldDBName),
							StandardCopyOption.REPLACE_EXISTING);
					this.fileHandler = new FileHandler(oldDBName);
					fileHandler.writeVersionInfoIfNewFile();
				} else {
					boolean tmpFileDeleted = tmpFile.delete();
					LOGGER.warning("@DBSpecificServer @replaceOldFileWithNew(File tmpFile)\n->Database file could not be deleted during defragmentation ||" +
							" \nOutcome for tmpFile.delete() (necessary on fail) == " + tmpFileDeleted);
					this.initialise();
					throw new IOException("Old DB file could not be deleted, defrag failed, check logs");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	@Override
	public DBRecord search(String name)
		{
			LOGGER.finest("@DBSpecificServer @search(String name) = " + name);
			DBRecord object = this.fileHandler.search(name);
			LOGGER.info("@DBSpecificServer search(String name) return. name = " + name + " " + object);
			return object;
		}

	@Override
	public void refreshIndex() throws IOException
		{
			LOGGER.finest("@DBSpecificServer @refreshIndex()");
			this.fileHandler.populateIndex();
		}

	@Override
	public Collection<DebugInfo> getRowsWithDebugInfo()
		{
			LOGGER.finest("@DBSpecificServer @getData()");
			return this.fileHandler.getCurrentDebugInfoRows();
		}

	@Override
	public Collection<DBRecord> searchWithLevenshtein(String name, int tolerance)
		{
			LOGGER.finest("@DBSpecificServer @searchWithLevenshtein(String name, int tolerance) = " + name + " " + tolerance);
			Collection<DBRecord> recordCollection = this.fileHandler.searchWithLevenshtein(name, tolerance);
			LOGGER.info("@DBSpecificServer @searchWithLevenshtein(String name, int tolerance) + returned Collection<DBRecord> = " + name + " " + tolerance + "\n" + getCollectionContents(recordCollection));
			return recordCollection;
		}

	@Override
	public Collection<DBRecord> searchWithRegex(String regEx)
		{
			LOGGER.finest("@DBSpecificServer @searchWithRegex(String regEx) = " + regEx);
			Collection<DBRecord> recordCollection = fileHandler.searchWithRegex(regEx);
			LOGGER.info("@DBSpecificServer @searchWithRegex(String regEx) + returned Collection<DBRecord> = " + regEx + "\n" + getCollectionContents(recordCollection));
			return recordCollection;
		}


	@Override
	public void add(DBRecord object) throws DuplicateNameException
		{
			LOGGER.finest("@DBSpecificServer @add(DBRecord) = " + object);
			OperationUnit operationUnit = this.fileHandler.add(object, false);
			getTransaction().registerAdd(operationUnit.addedRowBytePosition);
		}

	@Override
	public void update(Long rowNumber, final DBRecord newRecord) throws DuplicateNameException
		{
			LOGGER.finest("@DBSpecificServer @update(Long rowNumberOldRecord, DBRecord newRecord) = " + rowNumber + " " + newRecord);
			String name;
			try {
				if (checkRowNumber(rowNumber)) { //Checks name is in index before going ahead with delete
					DBRecord existingRowNumberRecord = read(rowNumber);
					assert existingRowNumberRecord != null;
					name = existingRowNumberRecord.getName();
					if (Index.getInstance().hasNameInIndex(name)) {
						OperationUnit operationUnit = this.fileHandler.updateByRow(rowNumber, newRecord);
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
	public void update(String name, DBRecord newRecord) throws DuplicateNameException
		{
			LOGGER.finest("@DBSpecificServer @update(name, newRecord) " + name + " " + newRecord.getName());
			try {
				if (Index.getInstance().hasNameInIndex(name)) {
					OperationUnit operationUnit = this.fileHandler.updateByName(name, newRecord);
					ITransaction transaction = getTransaction();
					transaction.registerAdd(operationUnit.addedRowBytePosition);
					transaction.registerDelete(operationUnit.deletedRowBytePosition);

				} else
					throw new NameDoesNotExistException(String.format("The name you are trying to update ('%s') does not exist", name));
			} catch (NameDoesNotExistException e) {
				e.printStackTrace();
			}


		}

	@Override
	public void delete(Long rowNumber)
		{
			LOGGER.finest("@DBSpecificServer @delete(rowNumber) = " + rowNumber);
			if (checkRowNumber(rowNumber)) {
				OperationUnit operationUnit = this.fileHandler.deleteRow(rowNumber);
				this.getTransaction().registerDelete(operationUnit.deletedRowBytePosition);
			}

		}

	@Override
	public DBRecord read(Long rowNumber)
		{
			LOGGER.finest("@DBSpecificServer @read(rowNumber) = " + rowNumber);
			DBRecord object = null;
			if (checkRowNumber(rowNumber)) {
				object = this.fileHandler.readRow(rowNumber);
				LOGGER.info("@DBSpecificServer read(Long rowNumber) return. rowNumber = " + rowNumber + " " + object);
				return object;
			}
			return object;
		}


	private boolean checkRowNumber(Long rowNumber)
		{
			LOGGER.finest("@DBSpecificServer @checkRowNumber(rowNumber) >0 = " + rowNumber);
			try {
				if (rowNumber < 0) {
					throw new IOException("Row number is less than 0");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}

	private StringBuilder getCollectionContents(final Collection<DBRecord> recordCollection)
		{
			StringBuilder sb = new StringBuilder();

			for (DBRecord dbr : recordCollection) {
				sb.append(dbr.toString());
				sb.append(System.getProperty("line.separator"));
			}

			return sb;
		}

	@Override
	public Long getTotalRecordAmount()
		{
			return Index.getInstance().getTotalNumberOfRows();
		}
}
