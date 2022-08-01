package fun.madeby.db.generic_server;

import com.google.gson.Gson;
import fun.madeby.exceptions.NameDoesNotExistException;
import fun.madeby.generic.GenericFileHandler;
import fun.madeby.generic.Schema;
import fun.madeby.generic.SchemaField;
import fun.madeby.specific.Index;
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

	public DBGenericServer(String dbFileName, String schemaString, Class aClass) throws FileNotFoundException {
		LOGGER.finest("@DBGenericServer(String dbFileName) = " + dbFileName);
		this.genericFileHandler = new GenericFileHandler(dbFileName);
		this.aClass = aClass;
		this.transactions = new LinkedHashMap<>();
		this.initialise();
		this.schema = this.readSchema(schemaString);
	}

	private Schema readSchema(final String schema) {
		Gson gson = new Gson();
		Schema tmpSchema = gson.fromJson(schema, Schema.class);
		for(SchemaField field : tmpSchema.schemaFields) {
			LOGGER.info("@DBGenericServer readSchema(String schema), field: " + field.fieldName + " type: " + field.fieldType);
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
		LOGGER.info("[" + this.getClass().getSimpleName() + "]" + "Read Object: " + sb);
	}


	@Override
	public void add(Object obj) {
		LOGGER.finest("@DBGenericServer @add(DBRecord) = " + obj);
		OperationUnit operationUnit = this.genericFileHandler.add(obj);
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
		Index.getInstance().clear();
		this.genericFileHandler.close();
	}


	@Override
	public void commit() {
		LOGGER.finest("@DBGenericServer commit() entered");
		ITransaction transaction = getTransaction();

		if (transaction != null) {
			Boolean successfullyCommitted = genericFileHandler.
					commit(transaction.getNewRowsBytePosition(), transaction.getDeletedRowsBytePosition());
			if (successfullyCommitted) {
				transactions.remove(Thread.currentThread().getId()); // cannot be retrieved..
				transaction.clear();// clearing data in object that can no longer be retrieved...
				LOGGER.info("@DBGenericServer commit() completed");
			}
		} else
			LOGGER.info("@DBGenericServer commit() transaction could not be found");
	}

	private void initialise() {
		LOGGER.finest("@DBGenericServer intialise()");
		this.genericFileHandler.writeVersionInfoIfNewFile();
		this.genericFileHandler.populateIndex();
	}


	@Override
	public void defragmentDatabase() throws IOException {
		LOGGER.finest("@DBGenericServer defragmentDatabase()");
		String prefix = "defrag";
		String suffix = "dat";

		File tmpFile = File.createTempFile(prefix, suffix);
		Index.getInstance().clear();

		// open temp file:
		GenericFileHandler defragGFH = new GenericFileHandler(new RandomAccessFile(tmpFile, "rw"), tmpFile.getName());

		Collection<DebugInfo> currentDebugInfoRows = this.genericFileHandler.getCurrentDebugInfoRows();

		for (DebugInfo info : currentDebugInfoRows) {
			if (info.isDeleted() || info.isTemporary())
				continue;
			Object object = info.getDbRecord();
			defragGFH.add(object);
		}

		replaceOldFileWithNew(tmpFile);
		defragGFH.close();
		Index.getInstance().clear();
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
		return Index.getInstance().getTotalNumberOfRows();
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
	public void refreshIndex() {
		LOGGER.finest("@DBGenericServer @refreshIndex()");
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
	public void update(String name, Object newObject) {
		LOGGER.finest("@DBGenericServer @update(name, newRecord) " + name + " " + newObject.getClass().getSimpleName());
		try {
			if (Index.getInstance().hasNameInIndex(name)) {
				OperationUnit operationUnit = this.genericFileHandler.updateByIndexedFieldName(name, newObject);
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
	public Object search(String indexedFieldName) {
		LOGGER.finest("@DBGenericServer @search(String indexedFieldName) = " + indexedFieldName);
		Object object = this.genericFileHandler.search(indexedFieldName);
		LOGGER.info("@DBGenericServer search(String indexedFieldName) return. indexedFieldName = " + indexedFieldName + " " + object);
		return object;
	}


	@Override
	public Collection<Object> searchWithLevenshtein(String indexedFieldName, int tolerance) {
		LOGGER.finest("@DBGenericServer @searchWithLevenshtein(String name, int tolerance) = " + indexedFieldName + " " + tolerance);
		Collection<Object> recordCollection = this.genericFileHandler.searchWithLevenshtein(indexedFieldName, tolerance);
		LOGGER.info("@DBGenericServer [New Alternative output below:] @searchWithLevenshtein(String name, int tolerance) + returned Collection<DBRecord> = " + indexedFieldName + " " + tolerance + "\n" + getCollectionContents(recordCollection));
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
	public void update(Long rowNumber, final Object newObj) {
		LOGGER.finest("@DBGenericServer @update(Long rowNumberOldRecord, DBRecord newRecord) = " + rowNumber + " " + newObj);
		String indexedFieldName;
		try {
			if (checkRowNumber(rowNumber)) { //todo extra. Checks indexedFieldName is in index before going ahead with del
				Object existingRowNumberRecord = read(rowNumber);
				assert existingRowNumberRecord != null;
				indexedFieldName = existingRowNumberRecord.getClass().getSimpleName();
				if (Index.getInstance().hasNameInIndex(indexedFieldName)) {
					OperationUnit operationUnit = this.genericFileHandler.updateByRow(rowNumber, newObj);
					ITransaction transaction = getTransaction();
					transaction.registerAdd(operationUnit.addedRowBytePosition);
					transaction.registerDelete(operationUnit.deletedRowBytePosition);
				} else
					throw new NameDoesNotExistException(String.format("The row you are trying to update with indexedFieldName ('%s') does not exist in the indexedFieldName index", indexedFieldName));
			}
		} catch (NameDoesNotExistException e) {
			e.printStackTrace();
		}

	}


	private ITransaction getTransaction() {
		LOGGER.finest("@DBGenericServer getTransaction()");
		long threadId = Thread.currentThread().getId();
		return transactions.getOrDefault(threadId, null);
	}


	private boolean checkRowNumber(Long rowNumber) {
		LOGGER.finest("@DBGenericServer @checkRowNumber(rowNumber) >0 = " + rowNumber);
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


	private void replaceOldFileWithNew(File tmpFile) {
		LOGGER.finest("@DBGenericServer replaceOldFileWithNew(File tmpFile) = " + tmpFile.getName());
		String oldDBName = this.genericFileHandler.getDbFileName();
		boolean oldFileDeleted = this.genericFileHandler.deleteFile();
		try {
			if (oldFileDeleted) {
				this.genericFileHandler.close();
				Files.copy(tmpFile.toPath(), FileSystems.getDefault().getPath("", oldDBName),
						StandardCopyOption.REPLACE_EXISTING);
				this.genericFileHandler = new GenericFileHandler(oldDBName);
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

}
