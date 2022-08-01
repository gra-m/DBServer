package fun.madeby.generic;

import fun.madeby.exceptions.DuplicateNameException;
import fun.madeby.exceptions.NameDoesNotExistException;
import fun.madeby.specific.Index;
import fun.madeby.util.Levenshtein;
import fun.madeby.util.OperationUnit;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static java.lang.Math.toIntExact;


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
		writeLock.lock();
		Long currentPositionToInsert = null;
		OperationUnit operationUnit = new OperationUnit();
		try {
			try {
				if (Index.getInstance().hasNameInIndex(object.getClass().getName())) { // todo name mess
					throw new DuplicateNameException(String.format("TODO: Name '%s' already exists!", object.getClass().getName())); // todo name mess
				}
			} catch (DuplicateNameException e) {
				e.printStackTrace();
			}

			int length = 0;
			currentPositionToInsert = this.dbFile.length();
			this.dbFile.seek(currentPositionToInsert);
			// populate length
			Object returnedRec = object.populateOwnRecordLength(object);
			try {
				length = toIntExact(returnedRec.getLength());
				if (length <= 0)
					throw new RuntimeException("Record length zero or less");
			} catch (ArithmeticException e) {
				e.printStackTrace();
			}
			// write data
			dbFile.writeBoolean(true); // isTemporary
			dbFile.writeBoolean(false); // isDeleted
			dbFile.writeInt(length); // length of record bytes

			String name = returnedRec.getName();
			dbFile.writeInt(name.length());
			dbFile.write(name.getBytes());

			int age = returnedRec.getAge();
			dbFile.writeInt(age);

			String address = returnedRec.getAddress();
			dbFile.writeInt(address.length());
			dbFile.write(address.getBytes());

			String carPlateNumber = returnedRec.getCarPlateNumber();
			dbFile.writeInt(carPlateNumber.length());
			dbFile.write(carPlateNumber.getBytes());

			String description = returnedRec.getDescription();
			dbFile.writeInt(description.length());
			dbFile.write(description.getBytes());

		}catch (IOException e) {
			e.printStackTrace();
		}finally {
			writeLock.unlock();
		}
		operationUnit.addedRowBytePosition = currentPositionToInsert;
		operationUnit.successfulOperation = true;
		return operationUnit;
	}


	public Object readRow(Long rowNumber) {
		LOGGER.severe("@GFH readRow(rowNumber) " + rowNumber);
		readLock.lock();
		Object result = null;
		try {
			Long rowsBytePosition = Index.getInstance().getRowsBytePosition(rowNumber);
			if (rowsBytePosition == -1L) {
				LOGGER.severe("@GFH readRow. getRowsBytePosition == -1L");
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
			Index indexInstance = Index.getInstance();
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
		Long namesRowNumber = Index.getInstance().getRowNumberByName(indexedFieldName);
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
		Long rowNumber = Index.getInstance().getRowNumberByName(indexedFieldName);
		if (rowNumber == -1)
			return null;
		return this.readRow(rowNumber);
	}

	public Collection<Object> searchWithLevenshtein(String indexedFieldName, int tolerance) {
		Collection<Object> result = new ArrayList<>();
		Set<String> names = (Set<String>) Index.getInstance().getNames();
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
		Set<String> names = (Set<String>) Index.getInstance().getNames();
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
