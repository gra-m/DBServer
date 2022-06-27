package fun.madeby;

import java.io.*;

import static java.lang.Math.toIntExact;


/**
 * Created by Gra_m on 2022 06 24
 */

public class FileHandler {
	private RandomAccessFile dbFile;

	public FileHandler(String fileName) throws FileNotFoundException {
		this.dbFile = new RandomAccessFile(fileName, "rw");
	}

	/** Writes a DbRecord to the RandomAccessFile dbFile.
	 *
	 * <p>First writes boolean isDeleted false, then own length, then actual record data. </p>
	 *
	 * <p>This method returns a {@code boolean} true, but testing will be added in the future.</p>
	 *
	 * @param dbRecord the record to be written
	 * @return not testing currently true
	 * @throws IOException if there is one
	 */
	public boolean add (DbRecord dbRecord) throws IOException {
		int length = 0;
		this.dbFile.seek(this.dbFile.length());

		// populate length
		DbRecord returnedRec = dbRecord.populateOwnRecordLength(dbRecord);
		try {
			length = toIntExact(returnedRec.getLength());
		if (length <= 0)
				throw new RuntimeException("Record length zero or less");
		}catch (ArithmeticException e) {
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

		return true;
	}

	public DbRecord readRow(Long rowNumber) throws IOException {
		byte[] row = this.readRawRecord(rowNumber);

		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(row));

		int nameLength = stream.readInt();
		byte[] nameBytes = new byte[nameLength];
		stream.read(nameBytes); // fill array, advance pointer
		String name = new String(nameBytes);

		int age  = stream.readInt();

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


	/** Reads the raw record, returns record data without storage information.
	 *
	 * @param rowNumber Not working with index currently, working if passed 0L.
	 * @return empty byte[] if boolean(deleted),  byte[] of row requested beginning with 4 bytes representing the name
	 * length int.
	 */
	private byte[] readRawRecord(Long rowNumber) {
		byte[] data = null;

		try {
			dbFile.seek(rowNumber);
			if (dbFile.readBoolean())
				return new byte[0];
			dbFile.seek(rowNumber + 1); // 1 byte boolean
			int recordLength = dbFile.readInt();
			dbFile.seek(rowNumber + 5); // 5 bytes boolean + int
			data = new byte[recordLength];
			this.dbFile.read(data);
		}catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public void close() throws IOException {
		this.dbFile.close();
	}
}
