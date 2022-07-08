package fun.madeby;

import fun.madeby.exceptions.DuplicateNameException;
import fun.madeby.exceptions.NameDoesNotExistException;
import fun.madeby.util.Levenshtein;

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
	public boolean add(DBRecord dbRecord){
		writeLock.lock();
		try {
			try {
				if (Index.getInstance().hasNameInIndex(dbRecord.getName())) {
					throw new DuplicateNameException(String.format("Name '%s' already exists!", dbRecord.getName()));
				}
			} catch (DuplicateNameException e) {
				e.printStackTrace();
			}

			int length = 0;
			long currentPositionToInsert = this.dbFile.length();
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
			dbFile.writeBoolean(false);
			dbFile.writeInt(length);

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

			// set the start point of the record just inserted
			Index.getInstance().add(currentPositionToInsert); // todo on clean add enters 0:0
			Index.getInstance().addNameToIndex(name, Index.getInstance().getTotalNumberOfRows() - 1); // todo on clean add enters Name:1
			if (Index.getInstance().getMapRowNumberBytePositionSize() == 0)
				System.out.println("How is that possible");
		}catch (IOException e) {
			e.printStackTrace();
		}finally {
			writeLock.unlock();
		}
		return true;
	}


	public DBRecord readRow(Long rowNumber) {
		readLock.lock();
		DBRecord result = null;
		try {
			Long rowsBytePosition = Index.getInstance().getRowsBytePosition(rowNumber);
			if (rowsBytePosition == -1L)
				return null;
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

	public void deleteRow(Long rowNumber, DBRecord existingRowNumberRecord) {
		writeLock.lock();
		try {
			Index indexInstance = Index.getInstance();
			long rowsBytePosition = indexInstance.getRowsBytePosition(rowNumber);
			if (rowsBytePosition == -1)
				throw new IOException("Row does not exist in index");
			this.dbFile.seek(rowsBytePosition);
			this.dbFile.writeBoolean(true);

			// update the index component.
			indexInstance.remove(rowNumber, existingRowNumberRecord);
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			writeLock.unlock();
		}

	}

	public void updateByRow(Long rowNumber, DBRecord newRecord, DBRecord existingRowNumberRecord) {
		writeLock.lock();
		try {
			this.deleteRow(rowNumber, existingRowNumberRecord);
			this.add(newRecord);
		}finally {
			writeLock.unlock();
		}
	}

	public void updateByName(String name, DBRecord newRecord, DBRecord existingRowNumberRecord) {
		writeLock.lock();
		Long namesRowNumber = Index.getInstance().getRowNumberByName(name);
		try {
			if (namesRowNumber == -1)
				throw new NameDoesNotExistException(String.format("Thread issue, name %s existed @DBServer, but could not be found here", name));
			else {
				updateByRow(namesRowNumber, newRecord, existingRowNumberRecord);
			}
		} catch (NameDoesNotExistException e) {
			e.printStackTrace();
		} finally {
			writeLock.unlock();
		}
	}

	public DBRecord search(String name) {
		Long rowNumber = Index.getInstance().getRowNumberByName(name);
		// Index.getInstance().printNameIndex(); //todo PRINT INFO
		if (rowNumber == -1)
			return null;
		return this.readRow(rowNumber);

		/*List<DBRecord> result = new ArrayList<>();
		LongStream.range(0, Index.getInstance().getTotalNumberOfRows()).forEach(i->{
			DBRecord dbRecord = null;
			try {
				dbRecord = this.readRow(i);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (dbRecord.equals(name))
				result.add(dbRecord);

		});
		return result;*/

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
