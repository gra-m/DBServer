package fun.madeby;

import fun.madeby.exceptions.DuplicateNameException;
import fun.madeby.exceptions.NameDoesNotExistException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

import static java.lang.Math.toIntExact;


/**
 * Created by Gra_m on 2022 06 24
 */

public class FileHandler extends BaseFileHandler {


	public FileHandler(String fileName) throws FileNotFoundException {
		super(fileName);
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
	public boolean add(DBRecord dbRecord) throws IOException{
		try {
			if (Index.getInstance().hasNameInIndex(dbRecord.getName())) {
				//System.out.printf("\nadd hasNameInIndex test: Name '%s' already exists!", dbRecord.getName());
				throw new DuplicateNameException(String.format("Name '%s' already exists!", dbRecord.getName()));
			}
		}catch (DuplicateNameException e) {
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
		Index.getInstance().addNameToIndex(name, Index.getInstance().getTotalNumberOfRows() -1); // todo on clean add enters Name:1
		if (Index.getInstance().getMapRowNumberBytePositionSize() == 0)
			System.out.println("How is that possible");
		return true;
	}


	public DBRecord readRow(Long rowNumber) throws IOException {
		// get/check rows byte position
		Long rowsBytePosition = Index.getInstance().getRowsBytePosition(rowNumber);
		if (rowsBytePosition == -1L)
			return null;
		byte[] row = readRawRecord(rowsBytePosition);
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(row));
		return readFromByteStream(stream);
	}

	public void deleteRow(Long rowNumber, DBRecord existingRowNumberRecord) throws IOException {
		Index indexInstance = Index.getInstance();
		long rowsBytePosition = indexInstance.getRowsBytePosition(rowNumber);
		if (rowsBytePosition == -1)
			throw new IOException("Row does not exist in index");
		this.dbFile.seek(rowsBytePosition);
		this.dbFile.writeBoolean(true);

		// update the index component.
		indexInstance.remove(rowNumber, existingRowNumberRecord);

	}

	public void updateByRow(Long rowNumber, DBRecord newRecord, DBRecord existingRowNumberRecord) {
		try {
			this.deleteRow(rowNumber, existingRowNumberRecord);
			this.add(newRecord);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updateByName(String name, DBRecord newRecord, DBRecord existingRowNumberRecord) {
		Long namesRowNumber = Index.getInstance().getRowNumberByName(name);
		try {
			if (namesRowNumber == -1)
				throw new NameDoesNotExistException(String.format("Thread issue, name %s existed @DBServer, but could not be found here", name));
			else {
				updateByRow(namesRowNumber, newRecord, existingRowNumberRecord);
			}
		} catch (NameDoesNotExistException e) {
			e.printStackTrace();
		}
	}

	public DBRecord search(String name) throws IOException {
		Long rowNumber = Index.getInstance().getRowNumberByName(name);
		Index.getInstance().printNameIndex();
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
}
