package fun.madeby;

import fun.madeby.dbserver.exceptions.DuplicateNameException;

import java.io.*;

import static java.lang.Math.toIntExact;


/**
 * Created by Gra_m on 2022 06 24
 */

public class FileHandler {
	private RandomAccessFile dbFile;
	private static final int INTEGER_LENGTH_IN_BYTES = 4;
	private static final int BOOLEAN_LENGTH_IN_BYTES = 1;

	public FileHandler(String fileName) throws FileNotFoundException {
		this.dbFile = new RandomAccessFile(fileName, "rw");
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
		Index.getInstance().add(currentPositionToInsert);
		Index.getInstance().addNameToIndex(name, Index.getInstance().getTotalNumberOfRows());
		return true;
	}


	public DBRecord readRow(Long rowNumber) throws IOException {

		// get/check rows byte position
		Long rowsBytePosition = Index.getInstance().getRowsBytePosition(rowNumber);
		if (rowsBytePosition == -1L)
			return null;

		byte[] row = this.readRawRecord(rowsBytePosition);
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(row));
		return readFromByteStream(stream);
	}

	private DBRecord readFromByteStream(final DataInputStream stream) throws IOException {

		int nameLength = stream.readInt();
		byte[] nameBytes = new byte[nameLength];
		stream.read(nameBytes); // fill array, advance pointer
		String name = new String(nameBytes);

		int age = stream.readInt();

		byte[] addressBytes = new byte[stream.readInt()];
		stream.read(addressBytes); // fill array, advance pointer
		String address = new String(addressBytes);

		byte[] carPlateBytes = new byte[stream.readInt()];
		stream.read(carPlateBytes); // fill array, advance pointer
		String carPlateNumber = new String(carPlateBytes);

		byte[] descriptionBytes = new byte[stream.readInt()];
		stream.read(descriptionBytes); // fill array, advance pointer
		String description = new String(descriptionBytes);

		return new CarOwner(name, age, address, carPlateNumber, description);

	}


	/**
	 * Reads the raw record, returns record data without storage information.
	 *
	 * @param rowsBytePosition Not working with index currently, working if passed 0L.
	 * @return empty byte[] if boolean(deleted),  byte[] of row requested beginning with 4 bytes representing the name
	 * length int.
	 */
	private byte[] readRawRecord(Long rowsBytePosition) {
		byte[] data = null;

		try {
			dbFile.seek(rowsBytePosition);
			if (dbFile.readBoolean())
				return new byte[0];
			dbFile.seek(rowsBytePosition + 1); // 1 byte boolean
			int recordLength = dbFile.readInt();
			dbFile.seek(rowsBytePosition + 5); // 5 bytes boolean + int
			data = new byte[recordLength];
			this.dbFile.read(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public void populateIndex() {
		long rowNum = 0;
		int recordLength = 0;
		long currentPosition = 0;
		long deletedRows = 0;

		if (checkForHistoricData()) {
			try {
				while (currentPosition < this.dbFile.length()) {
					this.dbFile.seek(currentPosition);
					boolean isDeleted = this.dbFile.readBoolean();
					if (!isDeleted) {
						Index.getInstance().add(currentPosition);
					} else deletedRows++;


					System.out.println("populateIndex: Total deletedRows in db = " + deletedRows);
					currentPosition += BOOLEAN_LENGTH_IN_BYTES;
					recordLength = this.dbFile.readInt();
					currentPosition += INTEGER_LENGTH_IN_BYTES;
					// retrieve current record
					if(!isDeleted) {
						this.dbFile.seek(currentPosition);
						byte[] retrieveRecord = new byte[recordLength];
						dbFile.read(retrieveRecord);
						DBRecord retrievedRecord = readFromByteStream(new DataInputStream(new ByteArrayInputStream(retrieveRecord)));
						Index.getInstance().addNameToIndex(retrievedRecord.getName(), rowNum++);
					}
					currentPosition += recordLength;
					System.out.println("populateIndex... rows= " + rowNum);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private boolean checkForHistoricData() {
		try {
			if (this.dbFile.length() == 0) {
				System.out.println("Db file is empty, nothing to index.");
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public void close() throws IOException {
		this.dbFile.close();
	}

	public void deleteRow(Long rowNumber) throws IOException {
		Index indexInstance = Index.getInstance();
		long rowsBytePosition = indexInstance.getRowsBytePosition(rowNumber);
		if (rowsBytePosition == -1)
			throw new IOException("Row does not exist in index");
		this.dbFile.seek(rowsBytePosition);
		this.dbFile.writeBoolean(true);

		// update the index component.
		indexInstance.remove(rowNumber);

	}

	public void updateByRow(Long rowNumber, DBRecord dbRecord) {
		try {
			this.deleteRow(rowNumber);
			this.add(dbRecord);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
