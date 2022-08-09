package fun.madeby.specific;

import fun.madeby.DBRecord;
import fun.madeby.exceptions.DuplicateNameException;
import fun.madeby.exceptions.NameDoesNotExistException;
import fun.madeby.util.Levenshtein;
import fun.madeby.util.OperationUnit;

import java.io.*;
import java.util.*;

import static java.lang.Math.toIntExact;


/**
 * Created by Gra_m on 2022 06 24
 */

public class FileHandler extends BaseFileHandler {



	public FileHandler(String fileName) throws FileNotFoundException {
		super(fileName);
	}

	public FileHandler(RandomAccessFile randomAccessFile, String fileName) {
		super(randomAccessFile, fileName);
	}


	/**
	 * Writes a DbRecord to the RandomAccessFile dbFile.
	 *
	 * <p>First writes boolean isDeleted false, then own length, then actual record data. </p>
	 *
	 * <p>This method returns a {@code boolean} true, but testing will be added in the future.</p>
	 *
	 * @param dbRecord the record to be written
	 * @return not testing currently true
	 * @throws IOException if there is one
	 */
	public OperationUnit add(DBRecord dbRecord) throws DuplicateNameException {
		writeLock.lock();
		Long currentPositionToInsert = null;
		OperationUnit operationUnit = new OperationUnit();
		String nameTest = dbRecord.getName();

				if (Index.getInstance().hasNameInIndex(nameTest)) {
					LOGGER.severe("@DBServer/add(DBRecord) Is duplicate name: " + nameTest);
					throw new DuplicateNameException(String.format("Name %s already exists!", nameTest));
				}

				try{

			int length = 0;
			currentPositionToInsert = this.dbFile.length();
			this.dbFile.seek(currentPositionToInsert);
			// populate length
			DBRecord returnedRec = dbRecord.populateOwnRecordLength(dbRecord);
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


	public DBRecord readRow(Long rowNumber) {
		LOGGER.severe("@FH readRow(rowNumber) " + rowNumber);
		readLock.lock();
		DBRecord result = null;
		try {
			Long rowsBytePosition = Index.getInstance().getRowsBytePosition(rowNumber);
			if (rowsBytePosition == -1L) {
				LOGGER.severe("FH readRow. getRowsBytePosition == -1L");
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

	public OperationUnit updateByRow(Long rowNumber, DBRecord newRecord) throws DuplicateNameException {
		writeLock.lock();
		OperationUnit operation = new OperationUnit();
		try {
			operation.deletedRowBytePosition  = (this.deleteRow(rowNumber).deletedRowBytePosition);
			operation.addedRowBytePosition = (this.add(newRecord)).addedRowBytePosition;
			operation.successfulOperation = true;
			return operation;

		}finally {
			writeLock.unlock();
		}
	}

	public OperationUnit updateByName(String name, DBRecord newRecord) throws DuplicateNameException {
		writeLock.lock();
		Long namesRowNumber = Index.getInstance().getRowNumberByName(name);
		OperationUnit operationUnit = new OperationUnit();

		try {
			if (namesRowNumber == -1)
				throw new NameDoesNotExistException(String.format("Thread issue, name %s existed @DBServer, but could not be found here", name));
			else {
				operationUnit = updateByRow(namesRowNumber, newRecord);
			}
		} catch (NameDoesNotExistException e) {
			e.printStackTrace();
		} finally {
			writeLock.unlock();
		}
		return operationUnit;
	}

	public DBRecord search(String name) {
		Long rowNumber = Index.getInstance().getRowNumberByName(name);
		if (rowNumber == -1)
			return null;
		return this.readRow(rowNumber);
	}

	public Collection<DBRecord> searchWithLevenshtein(String name, int tolerance) {
		Collection<DBRecord> result = new ArrayList<>();
		Set<String> names = (Set<String>) Index.getInstance().getNames();
		Collection<String> exactOrCloseFitNames = new ArrayList<>();

		for(String storedName: names) {
			if (Levenshtein.levenshteinDistance(storedName, name, false) <= tolerance)
				exactOrCloseFitNames.add(storedName);
		}
		// get records:
		for (String exactOrCloseFitName: exactOrCloseFitNames) {
			result.add(search(exactOrCloseFitName));
		}
		return result;
	}

	public Collection<DBRecord> searchWithRegex(String regEx) {
		Collection<DBRecord> result = new ArrayList<>();
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