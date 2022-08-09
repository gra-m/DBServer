package fun.madeby.generic;

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

public class GenericFileHandler extends GenericBaseFileHandler {



	public GenericFileHandler(String fileName) throws FileNotFoundException {
		super(fileName);
	}

	public GenericFileHandler(RandomAccessFile randomAccessFile, String fileName) {
		super(randomAccessFile, fileName);
	}

	/**
	 * Writes a DbRecord to the RandomAccessFile dbFile.
	 *
	 * <p>First writes boolean isDeleted false, then own length, then actual record data. </p>
	 *
	 * <p>This method returns a {@code boolean} true, but testing will be added in the future.</p>
	 *
	 * @param object the record to be written
	 * @return not testing currently true
	 * @throws IOException if there is one
	 */
	public OperationUnit add(Object object){
		LOGGER.severe("@GFH add(Object) = " + object);
		writeLock.lock();
		Long currentPositionToInsert = null;
		int length = 0;
		OperationUnit operationUnit = new OperationUnit();
		try {

			try {
				String testName = object.getClass().getName()
				//reflection to get indexBy
				if (GenericIndex.getInstance().hasGenericIndexedValueInGenericIndex("Fritx")) {
					LOGGER.severe("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
					throw new DuplicateNameException(String.format("TODO: Name '%s' already exists!", object.getClass().getName())); // todo name mess
				}
			}catch(DuplicateNameException e) {
				e.printStackTrace();
			}

			currentPositionToInsert = this.dbFile.length();

			length = getObjectLength(object, this.schema.schemaFields);


			//todo remove test here
			if (length >0) {
				this.dbFile.seek(currentPositionToInsert);
				dbFile.writeBoolean(true); // isTemporary
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

	private void writeObject(final Object obj, final LinkedList<SchemaField> linkedList) {
		LOGGER.finest("@GenericFileHandler writeObject(obj, LinkedList)" + obj.getClass().getSimpleName() + " " + linkedList.toString());

		try {
			for (SchemaField field : linkedList) {
				Object objectValue = obj.getClass().getDeclaredField(field.fieldName).get(obj);

				switch (field.fieldType) {
					case "String" -> {
						this.dbFile.writeInt(((String)objectValue).length());
						this.dbFile.write(((String)objectValue).getBytes());
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
				}
			}
		}catch (NoSuchFieldException | IllegalAccessException | IOException e) {
			e.printStackTrace();
		}
	}


	private int getObjectLength(final Object obj, final LinkedList<SchemaField> linkedList) {
		LOGGER.finest("@GenericFileHandler getObjectLength(obj, LinkedList)" + obj.getClass().getSimpleName() + " " + linkedList.toString());
		int result = 0;
		int count = 0;


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
				}
			}
		}catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return result;
	}


	public Object readRow(Long rowNumber) {
		LOGGER.finest("@GFH readRow(rowNumber) " + rowNumber);
		readLock.lock();
		Object result = null;
		try {
			Long rowsBytePosition = GenericIndex.getInstance().getRowsBytePosition(rowNumber);
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
			GenericIndex indexInstance = GenericIndex.getInstance();
			rowsBytePosition = indexInstance.getRowsBytePosition(rowNumber);
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

	public OperationUnit updateByRow(Long rowNumber, Object newObject) {
		writeLock.lock();
		OperationUnit operation = new OperationUnit();
		try {
			operation.deletedRowBytePosition  = (this.deleteRow(rowNumber).deletedRowBytePosition);
			operation.addedRowBytePosition = (this.add(newObject)).addedRowBytePosition;
			operation.successfulOperation = true;
			return operation;

		}finally {
			writeLock.unlock();
		}
	}

	public OperationUnit updateByIndexedFieldName(String indexedFieldName, Object newObject) {
		writeLock.lock();
		Long namesRowNumber = GenericIndex.getInstance().getRowNumberByName(indexedFieldName);
		OperationUnit operationUnit = new OperationUnit();

		try {
			if (namesRowNumber == -1)
				throw new NameDoesNotExistException(String.format("Thread issue, indexedFieldName %s existed @DBServer, but could not be found here", indexedFieldName));
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

	public Object search(String indexedFieldName) {
		Long rowNumber = GenericIndex.getInstance().getRowNumberByName(indexedFieldName);
		if (rowNumber == -1)
			return null;
		return this.readRow(rowNumber);
	}

	public Collection<Object> searchWithLevenshtein(String indexedFieldName, int tolerance) {
		Collection<Object> result = new ArrayList<>();
		Set<String> names = (Set<String>) GenericIndex.getInstance().getGenericIndexedValues();
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

	public Collection<Object> searchWithRegex(String regEx) {
		Collection<Object> result = new ArrayList<>();
		Set<String> names = (Set<String>) GenericIndex.getInstance().getGenericIndexedValues();
		Collection<String> matchesRegEx = new ArrayList<>();

		for(String storedName: names) {
			if (storedName.matches(regEx))
				matchesRegEx.add(storedName);
		}
		// get records:
		for (String match: matchesRegEx) {
			result.add(search(match));
		}
		return result;
	}

}
