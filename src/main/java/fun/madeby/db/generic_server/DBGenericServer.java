package fun.madeby.db.generic_server;

import com.google.gson.Gson;
import fun.madeby.exceptions.DBException;
import fun.madeby.exceptions.DuplicateNameException;
import fun.madeby.exceptions.NameDoesNotExistException;
import fun.madeby.generic.GenericFileHandler;
import fun.madeby.generic.GenericIndex;
import fun.madeby.generic.Schema;
import fun.madeby.generic.SchemaField;
import fun.madeby.transaction.ITransaction;
import fun.madeby.transaction.Transaction;
import fun.madeby.util.DebugInfo;
import fun.madeby.util.LoggerSetUp;
import fun.madeby.util.OperationUnit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Gra_m on 2022 07 30
 */

public class DBGenericServer implements DBGeneric {
	private GenericFileHandler genericFileHandler;
	private Schema schema;
	private Class aClass;
	private Logger LOGGER;
	public Map<Long, ITransaction> transactions;

	{
		try {
			LOGGER = LoggerSetUp.setUpLogger("DbGenericServer");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public DBGenericServer(String dbFileName, String schemaString, Class aClass) throws DBException, FileNotFoundException {
		LOGGER.finest("@DBGenericServer(String dbFileName) = " + dbFileName);
		this.schema = this.readSchema(schemaString);
		this.aClass = aClass;
		this.genericFileHandler = new GenericFileHandler(dbFileName);
		this.genericFileHandler.setSchema(schema);
		this.genericFileHandler.setAClass(aClass);
		this.transactions = new LinkedHashMap<>();
		this.initialise();
	}

	public DBGenericServer(GenericFileHandler genericFileHandler, Schema schema, Class aClass) throws DBException {
		this.schema = schema;
		this.aClass = aClass;
		this.genericFileHandler = genericFileHandler;
		this.transactions = new LinkedHashMap<>();
		this.initialise();
	}

	private Schema readSchema(final String schema) {
		LOGGER.finest("@DBGenericServer readSchema(String schema):" + schema);
		Gson gson = new Gson();
		Schema tmpSchema = gson.fromJson(schema, Schema.class);
		for(SchemaField field : tmpSchema.schemaFields) {
			LOGGER.finest("@DBGenericServer readSchema(String schema), field: " + field.fieldName + " type: " + field.fieldType);
		}
		return tmpSchema;
	}

	private void logObjectInfo(final Object obj) {
		LOGGER.info("[" + this.getClass().getSimpleName() + "]" + "Read Object: " + obj);
	}

	private void logObjectListInfo(Collection<Object> objects) {
		StringBuilder sb = new StringBuilder(300);
		for(Object obj: objects) {
		//	sb.append("[").append(obj.getClass().getSimpleName()).append("]").append("Read Object: ").append(obj).append(System.getProperty("line"));
		sb.append(obj.toString()).append(System.getProperty("line separator"));
		}
		// todo check output, not seen
		LOGGER.finest("[" + this.getClass().getSimpleName() + "]" + "Read Object: " + sb);
	}


	@Override
	public void add(Object obj) throws DuplicateNameException, DBException {
		LOGGER.finest("@DBGenericServer @add(DBRecord) = " + obj);
		OperationUnit operationUnit = this.genericFileHandler.add(obj, false);
		getTransaction().registerAdd(operationUnit.addedRowBytePosition);
	}


	@Override
	public ITransaction beginTransaction() {
		LOGGER.finest("@DBGenericServer beginTransaction()");
		long threadId = Thread.currentThread().getId();
		boolean threadAlreadyHasTransaction = transactions.containsKey(threadId);

		if (threadAlreadyHasTransaction)
			return transactions.get(threadId);

		ITransaction transaction = new Transaction();
		this.transactions.put(threadId, transaction);
		return new Transaction();
	}


	@Override
	public void close() throws IOException {
		LOGGER.info("@DBGenericServer close()");
		GenericIndex.getInstance().clear();
		this.genericFileHandler.close();
	}


	@Override
	public void commit() {
		LinkedList<Long> deletesToBeCommitted;
		LinkedList<Long> addsToBeCommitted;

		LOGGER.finest("@DBGenericServer commit() entered");
		ITransaction transaction = getTransaction();

		if (transaction != null) {
			addsToBeCommitted = (LinkedList<Long>) transaction.getNewRowsBytePosition();
			deletesToBeCommitted = (LinkedList<Long>) transaction.getDeletedRowsBytePosition();
			printBeforeCommit(addsToBeCommitted, deletesToBeCommitted);
			Boolean successfullyCommitted = genericFileHandler.
					commit(addsToBeCommitted, deletesToBeCommitted);
			if (successfullyCommitted) {
				transactions.remove(Thread.currentThread().getId()); // cannot be retrieved..
				transaction.clear();// clearing data in object that can no longer be retrieved...
				LOGGER.info("@DBGenericServer commit() completed");
			}
		} else
			LOGGER.info("@DBGenericServer commit() transaction could not be found");
	}

	private void printBeforeCommit(LinkedList<Long> addsToBeCommitted, LinkedList<Long> deletesToBeCommitted) {
		String adds = addsToBeCommitted.size() == 0 ? "none" : addsToBeCommitted.toString();
		String deletes = deletesToBeCommitted.size() == 0 ? "none" : deletesToBeCommitted.toString();
		LOGGER.info("Adds to be committed: " + adds + " Deletes to be committed: " + deletes);
	}

	private void initialise() throws DBException {
		GenericIndex.getInstance().initialiseGenericIndexSchema(this.schema);
		this.genericFileHandler.setAClass(this.aClass);
		LOGGER.finest("@DBGenericServer intialise()");
		this.genericFileHandler.writeVersionInfoIfNewFile();
		this.genericFileHandler.populateIndex();
	}


	@Override
	public void defragmentDatabase() throws IOException, DuplicateNameException, DBException {
		LOGGER.finest("@DBGenericServer defragmentDatabase()");
		String prefix = "defrag";
		String suffix = "dat";

		File tmpFile = File.createTempFile(prefix, suffix);
		GenericIndex.getInstance().clear();

		// open temp file and GFH perhaps initialiseTemp??:
		GenericFileHandler defragGFH = new GenericFileHandler(new RandomAccessFile(tmpFile, "rw"), tmpFile.getName());
		defragGFH.setSchema(this.schema);
		defragGFH.setAClass(this.aClass);
		defragGFH.writeVersionInfoIfNewFile();

		Collection<DebugInfo> currentDebugInfoRows = this.genericFileHandler.getCurrentDebugInfoRows();


		LOGGER.severe("->currentDebugInfoRows = " + currentDebugInfoRows.size() + currentDebugInfoRows);
		if(currentDebugInfoRows.size() > 0) {
			for (DebugInfo info : currentDebugInfoRows) {
				if (info.isDeleted() || info.isTemporary())
					continue;
				Object object = info.getDbRecord();
				defragGFH.add(object, true);
			}
		}

		replaceOldFileWithNew(tmpFile);
		defragGFH.close();
		GenericIndex.getInstance().clear();
		GenericIndex.getInstance().initialiseGenericIndexSchema(this.schema);
		this.initialise();
	}


	@Override
	public void delete(Long rowNumber) {
		LOGGER.finest("@DBGenericServer @delete(rowNumber) = " + rowNumber);
		if (checkRowNumber(rowNumber)) {
			OperationUnit operationUnit = this.genericFileHandler.deleteRow(rowNumber);
			this.getTransaction().registerDelete(operationUnit.deletedRowBytePosition);
		}

	}


	@Override
	public Collection<DebugInfo> getRowsWithDebugInfo() {
		LOGGER.finest("@DBGenericServer @getData()");
		return this.genericFileHandler.getCurrentDebugInfoRows();
	}


	@Override
	public Long getTotalRecordAmount() {
		return GenericIndex.getInstance().getTotalNumberOfRows();
	}


	@Override
	public Object read(Long rowNumber) {
		LOGGER.finest("@DBGenericServer @read(rowNumber) = " + rowNumber);
		Object obj = null;
		if (checkRowNumber(rowNumber)) {
			obj = this.genericFileHandler.readRow(rowNumber);
			LOGGER.info("@DBGenericServer read(Long rowNumber) return. rowNumber = " + rowNumber + " " + obj);
			return obj;
		}
		return obj;
	}


	@Override
	public void refreshGenericIndex() {
		LOGGER.finest("@DBGenericServer @refreshGenericIndex()");
		this.genericFileHandler.populateIndex();
	}


	@Override
	public void rollback() {
		LOGGER.finest("@DBGenericServer rollback() entered");
		ITransaction transaction = getTransaction();
		if (transaction == null)
			return;

		Boolean successfullyRolledBack = genericFileHandler.rollback(transaction.getNewRowsBytePosition(), transaction.getDeletedRowsBytePosition());

		if (successfullyRolledBack) {
			transactions.remove(Thread.currentThread().getId()); // cannot be retrieved..
			transaction.clear();// clearing data in object that can no longer be retrieved...
		}
		LOGGER.info("@DBGenericServer rollback() completed");
	}


	@Override
	public void update(String GenericIndexedValue, Object newObject) throws DuplicateNameException, DBException {
		LOGGER.finest("@DBGenericServer @update(name, newRecord) " + GenericIndexedValue + " " + newObject.getClass().getSimpleName());
		try {
			if (GenericIndex.getInstance().hasGenericIndexedValueInGenericIndex(GenericIndexedValue)) {
				OperationUnit operationUnit = this.genericFileHandler.updateByIndexedFieldName(GenericIndexedValue, newObject);
				ITransaction transaction = getTransaction();
				transaction.registerAdd(operationUnit.addedRowBytePosition);
				transaction.registerDelete(operationUnit.deletedRowBytePosition);

			} else
				throw new NameDoesNotExistException(String.format("The name you are trying to update ('%s') does not exist", GenericIndexedValue));
		} catch (NameDoesNotExistException e) {
			e.printStackTrace();
		}
	}


	@Override
	public Object search(String GenericIndexedFieldName) {
		LOGGER.finest("@DBGenericServer @search(String GenericIndexedFieldName) = " + GenericIndexedFieldName);
		Object object = this.genericFileHandler.search(GenericIndexedFieldName);
		LOGGER.info("@DBGenericServer search(String GenericIndexedFieldName) return. GenericIndexedFieldName = " + GenericIndexedFieldName + " " + object);
		return object;
	}


	@Override
	public Collection<Object> searchWithLevenshtein(String GenericIndexedFieldName, int tolerance) {
		LOGGER.finest("@DBGenericServer @searchWithLevenshtein(String name, int tolerance) = " + GenericIndexedFieldName + " " + tolerance);
		Collection<Object> recordCollection = this.genericFileHandler.searchWithLevenshtein(GenericIndexedFieldName, tolerance);
		LOGGER.info("@DBGenericServer [New Alternative output below:] @searchWithLevenshtein(String name, int tolerance) + returned Collection<DBRecord> = " + GenericIndexedFieldName + " " + tolerance + "\n" + getCollectionContents(recordCollection));
		this.logObjectListInfo(recordCollection);
		return recordCollection;
	}


	@Override
	public Collection<Object> searchWithRegex(String regEx) {
		LOGGER.finest("@DBGenericServer @searchWithRegex(String regEx) = " + regEx);
		Collection<Object> recordCollection = genericFileHandler.searchWithRegex(regEx);
		LOGGER.info("@DBGenericServer @searchWithRegex(String regEx) + returned Collection<DBRecord> = " + regEx + "\n" + getCollectionContents(recordCollection));
		return recordCollection;
	}


	@Override
	public void update(Long rowNumber, final Object newObj) throws DuplicateNameException, DBException {
		LOGGER.finest("@DBGenericServer @update(Long rowNumberOldRecord, DBRecord newRecord) = " + rowNumber + " " + newObj);
		String GenericIndexedFieldName;
		try {
			if (checkRowNumber(rowNumber)) { //todo extra. Checks GenericIndexedFieldName is in GenericIndex before going ahead with del
				Object existingRowNumberRecord = read(rowNumber);
				assert existingRowNumberRecord != null;
				GenericIndexedFieldName = (String) existingRowNumberRecord.getClass().getDeclaredField("pName").get(existingRowNumberRecord);
				if (GenericIndex.getInstance().hasGenericIndexedValueInGenericIndex(GenericIndexedFieldName)) {
					OperationUnit operationUnit = this.genericFileHandler.updateByRow(rowNumber, newObj);
					ITransaction transaction = getTransaction();
					transaction.registerAdd(operationUnit.addedRowBytePosition);
					transaction.registerDelete(operationUnit.deletedRowBytePosition);
				} else
					throw new NameDoesNotExistException(String.format("The row you are trying to update with GenericIndexedFieldName ('%s') does not exist in the GenericIndexedFieldName GenericIndex", GenericIndexedFieldName));
			}
		} catch (NameDoesNotExistException | IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
		}

	}


	private ITransaction getTransaction() {
		LOGGER.finest("@DBGenericServer getTransaction()");
		long threadId = Thread.currentThread().getId();
		LOGGER.severe("@DBGenericServer getTransaction() getting transactions with ThreadId = " + threadId);
		return transactions.getOrDefault(threadId, null);
	}


	private boolean checkRowNumber(Long rowNumber) {
		LOGGER.finest("@DBGenericServer @checkRowNumber(rowNumber) >=0 = " + rowNumber);
		try {
			if (rowNumber < 0) {
				throw new IOException("Row number is less than 0");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}


	private StringBuilder getCollectionContents(final Collection<Object> recordCollection) {
		StringBuilder sb = new StringBuilder();

		for (Object dbr : recordCollection) {
			sb.append(dbr.toString());
			sb.append(System.getProperty("line.separator"));
		}

		return sb;
	}


	private void replaceOldFileWithNew(File tmpFile) throws DBException {
		LOGGER.finest("@DBGenericServer replaceOldFileWithNew(File tmpFile) = " + tmpFile.getName());
		String oldDBName = this.genericFileHandler.getDbFileName();
		boolean oldFileDeleted = this.genericFileHandler.deleteFile();
		try {
			if (oldFileDeleted) {
				this.genericFileHandler.close();
				Files.copy(tmpFile.toPath(), FileSystems.getDefault().getPath("", oldDBName),
						StandardCopyOption.REPLACE_EXISTING);
				this.genericFileHandler = new GenericFileHandler(oldDBName);
				genericFileHandler.setSchema(this.schema);
				genericFileHandler.setAClass(aClass);
				genericFileHandler.writeVersionInfoIfNewFile();
			} else {
				boolean tmpFileDeleted = tmpFile.delete();
				LOGGER.warning("@DBGenericServer @replaceOldFileWithNew(File tmpFile)\n->Database file could not be deleted during defragmentation ||" +
						" \nOutcome for tmpFile.delete() (necessary on fail) == " + tmpFileDeleted);
				this.initialise();
				throw new IOException("Old DB file could not be deleted, defrag failed, check logs");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getDBVersion() {
		return this.genericFileHandler.getDBVersion();
	}
}
