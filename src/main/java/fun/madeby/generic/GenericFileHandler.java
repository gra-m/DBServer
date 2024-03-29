package fun.madeby.generic;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fun.madeby.exceptions.DBException;
import fun.madeby.exceptions.DuplicateNameException;
import fun.madeby.exceptions.NameDoesNotExistException;
import fun.madeby.util.Levenshtein;
import fun.madeby.util.OperationUnit;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;


/**
 * Created by Gra_m on 2022 06 24
 */

@SuppressFBWarnings({"NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "DM_DEFAULT_ENCODING"})
public class GenericFileHandler extends GenericBaseFileHandler {



	public GenericFileHandler(String fileName, GenericIndex index) throws DBException, FileNotFoundException {
		super(fileName, index);
	}

	public GenericFileHandler(RandomAccessFile randomAccessFile, String fileName, GenericIndex index) {
		super(randomAccessFile, fileName, index);
	}

	/**
	 * Writes a DbRecord to the RandomAccessFile dbFile.
	 *
	 *
	 * @param object   the record to be written
	 * @param isDefrag default == false, sent as true when part of defragmentation process and isTemporary needs to be false.
	 * @return not testing currently true
	 * @throws IOException if there is one
	 */
	public OperationUnit add(Object object, boolean isDefrag) throws DuplicateNameException, DBException {
		LOGGER.severe("@GFH add(Object) = " + object);
		writeLock.lock();
		Long currentPositionToInsert = null;
		int length = 0;
		OperationUnit operationUnit = new OperationUnit();
		try {

			try {
				String testName = (String)object.getClass().getDeclaredField(schema.indexBy).get(object);

				if (this.index.hasGenericIndexedValueInGenericIndex(testName)) {
					LOGGER.severe(String.format("@GenericFileHandler/add(Object)  Name/GenericIndexedValue '%s' already exists", testName));
					throw new DuplicateNameException(String.format("TODO: Name '%s' already exists!", object.getClass().getName()));
				}
			}catch(NoSuchFieldException e) {
				throw new DBException("@GenericFileHandler/add(Object) noSuchField");
			}catch (IllegalAccessException e) {
				throw new DBException("@GenericFileHandler/add(Object) IllegalAccess");
			}

			currentPositionToInsert = this.dbFile.length();

			length = getObjectLength(object, this.schema.schemaFields);


			if (length >0) {
				this.dbFile.seek(currentPositionToInsert);

				// isTemporary
				dbFile.writeBoolean(!isDefrag); // isTemporary
				dbFile.writeBoolean(false); // isDeleted
				dbFile.writeInt(length); // length of record bytes
				writeObject(object, this.schema.schemaFields);




			} else
				LOGGER.info("Object length returned as 0");



		}catch (IOException e) {
			e.printStackTrace();
		}finally {
			writeLock.unlock();
		}
		operationUnit.addedRowBytePosition = currentPositionToInsert;
		operationUnit.successfulOperation = true;
		return operationUnit;
	}

	private void writeObject(final Object obj, final LinkedList<SchemaField> linkedList) throws DBException
		{
		LOGGER.finest("@GenericFileHandler writeObject(obj, LinkedList)" + obj.getClass().getSimpleName() + " " + linkedList.toString());

		try {
			for (SchemaField field : linkedList) {
				Object objectValue = obj.getClass().getDeclaredField(field.fieldName).get(obj);

				switch (field.fieldType) {
					case "String" -> {
						this.dbFile.writeInt(((String)objectValue).length());
						this.dbFile.write(((String)objectValue).getBytes("UTF-8"));
					}
					case "boolean" -> {
						this.dbFile.writeBoolean((Boolean) objectValue);
					}
					case "int" -> {
						this.dbFile.writeInt((Integer) objectValue);
					}
					case "long" -> {
						this.dbFile.writeLong((Long) objectValue);
					}
					default -> {
						String unfoundFieldType = "fieldType_of_" + field.fieldType + "_of_name_" + field.fieldName + "_not_found";
						throw new DBException("@GFH/writeObject(): Field type not recognised by switch " + unfoundFieldType);
					}
				}
			}
		}catch (NoSuchFieldException | IllegalAccessException | IOException e) {
			e.printStackTrace();
		}
	}


	private int getObjectLength(final Object obj, final LinkedList<SchemaField> linkedList) throws DBException
		{
		LOGGER.info("@GenericFileHandler getObjectLength(obj, LinkedList)" + obj.getClass().getSimpleName() + " " + linkedList.toString());
		int result = 0;


		try {
			for (SchemaField field : linkedList) {
				switch (field.fieldType) {
					case "String" -> {
						String stringValue = (String) obj.getClass().getDeclaredField(field.fieldName).get(obj);
						result += stringValue.length(); // if I was using UTF-16 not recommended by IJ this would be 2x bytes per char lots of places on int == 2bytes but depends on encoding!
						result += INTEGER_LENGTH_IN_BYTES;
					}
					case "boolean" -> {
						result += BOOLEAN_LENGTH_IN_BYTES;
					}
					case "int" -> {
						result += INTEGER_LENGTH_IN_BYTES;
					}
					case "long" -> {
						result += LONG_LENGTH_IN_BYTES;
					}
					default -> {
						String unfoundFieldType = "fieldType_of_" + field.fieldType + "_of_name_" + field.fieldName + "_not_found";
						throw new DBException("@GFH/getObjectLength(): Field type not recognised by switch " + unfoundFieldType);
					}
				}
			}
		}catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return result;
	}


	public Object readRow(Long rowNumber) throws DBException
		{
		LOGGER.finest("@GFH readRow(rowNumber) " + rowNumber);
		readLock.lock();
		Object result = null;
		try {
			Long rowsBytePosition = this.index.getRowsBytePosition(rowNumber);
			if (rowsBytePosition == -1L) {
				LOGGER.finest("@GFH readRow. getRowsBytePosition == -1L");
				return null;
			}
			byte[] row = readRawRecord(rowsBytePosition);
			DataInputStream stream = new DataInputStream(new ByteArrayInputStream(row));
			result = readFromByteStream(stream);
		}catch (IOException e) {
			e.printStackTrace();
		}finally {
			readLock.unlock();
		}
		return result;
	}

	public OperationUnit deleteRow(Long rowNumber) {
		writeLock.lock();
		OperationUnit operationUnit = new OperationUnit();
		Long rowsBytePosition = null;
		try {
			rowsBytePosition = this.index.getRowsBytePosition(rowNumber);
			if (rowsBytePosition == -1)
				throw new IOException("Row does not exist in index");
			this.dbFile.seek(rowsBytePosition);
			this.dbFile.writeBoolean(true); // isTemporary
			this.dbFile.seek(rowsBytePosition + BOOLEAN_LENGTH_IN_BYTES);
			this.dbFile.writeBoolean(true); // isDeleted

		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			writeLock.unlock();
		}
		operationUnit.successfulOperation = true;
		operationUnit.deletedRowBytePosition = rowsBytePosition;
		return operationUnit;
	}

	public OperationUnit updateByRow(Long rowNumber, Object newObject) throws DuplicateNameException, DBException{
		writeLock.lock();
		OperationUnit operation = new OperationUnit();
		try {
			operation.deletedRowBytePosition  = (this.deleteRow(rowNumber).deletedRowBytePosition);
			operation.addedRowBytePosition = (this.add(newObject, false)).addedRowBytePosition;
			operation.successfulOperation = true;
			return operation;

		}finally {
			writeLock.unlock();
		}
	}

	public OperationUnit updateByIndexedFieldName(String indexedFieldName, Object newObject)
			throws DuplicateNameException, DBException {
		writeLock.lock();
		Long namesRowNumber = this.index.getRowNumberByName(indexedFieldName);
		OperationUnit operationUnit = new OperationUnit();

		try {
			if (namesRowNumber == -1)
				throw new NameDoesNotExistException(String.format("Thread issue, indexedFieldName %s existed @DBSpecificServer, but could not be found here", indexedFieldName));
			else {
				operationUnit = updateByRow(namesRowNumber, newObject);
			}
		} catch (NameDoesNotExistException e) {
			e.printStackTrace();
		} finally {
			writeLock.unlock();
		}
		return operationUnit;
	}

	public Object search(String indexedFieldName) throws DBException
		{
		Long rowNumber = this.index.getRowNumberByName(indexedFieldName);
		if (rowNumber == -1)
			return null;
		return this.readRow(rowNumber);
	}

	public Collection<Object> searchWithLevenshtein(String indexedFieldName, int tolerance) throws DBException
		{
		Collection<Object> result = new ArrayList<>();
		Set<String> names = (Set<String>) this.index.getGenericIndexedValues();
		Collection<String> exactOrCloseFitNames = new ArrayList<>();

		for(String storedName: names) {
			if (Levenshtein.levenshteinDistance(storedName, indexedFieldName, false) <= tolerance)
				exactOrCloseFitNames.add(storedName);
		}
		// get records:
		for (String exactOrCloseFitName: exactOrCloseFitNames) {
			result.add(search(exactOrCloseFitName));
		}
		return result;
	}

	public Collection<Object> searchWithRegex(String regEx) throws DBException
		{
		Collection<Object> result = new ArrayList<>();
		Object ifArrayListSkip = this.index.getGenericIndexedValues();
		Set<String> names;

		if(ifArrayListSkip.getClass() != ArrayList.class) {
			names = (Set<String>) this.index.getGenericIndexedValues();
			Collection<String> matchesRegEx = new ArrayList<>();

			for (String storedName : names) {
				if (storedName.matches(regEx))
					matchesRegEx.add(storedName);
			}
			// get records:
			for (String match : matchesRegEx) {
				result.add(search(match));
			}
		}
		return result;
	}

	public void setTableVersion()
		{

		}
}
