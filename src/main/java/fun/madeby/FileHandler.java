package fun.madeby;

import java.io.*;

import static java.lang.Math.toIntExact;


/**
 * Created by Gra_m on 2022 06 24
 */

public class FileHandler implements Serializable {
	private RandomAccessFile dbFile;

	public FileHandler(String fileName) throws FileNotFoundException {
		this.dbFile = new RandomAccessFile(fileName, "rw");
	}

	public boolean add (DbRecord dbRecord) throws IOException {
		int length = 0;

		// seek to the end of the file
		this.dbFile.seek(this.dbFile.length());
		// calculate record length record uses int Record future proofed with long
		DbRecord returnedRec = dbRecord.populateOwnRecordLength(dbRecord);
		try {
			length = toIntExact(returnedRec.getLength());



			System.out.println("Length of Raw Row: " + returnedRec.getLength());



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




		System.out.println("Length of retrieved row: " + row.length);




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


	/**
	 * Test version (Will) seek to index of row required
	 *
	 * @param rowNumber pass 0 in testing to be refactored.
	 * @return empty byte[] if boolean(deleted),  byte[] of row requested beginning with 4 bytes representing the name
	 * length int.
	 */
	private byte[] readRawRecord(Long rowNumber) {
		byte[] data = null;

		// no index component yet works with test pass 0L
		try {
			dbFile.seek(rowNumber);
			if (dbFile.readBoolean())
				return new byte[0];
			dbFile.seek(rowNumber + 1); // 1 byte boolean
			int recordLength = dbFile.readInt(); //first int found is recordLength??
			dbFile.seek(rowNumber + 5); // 5 bytes boolean + int

			System.out.println(recordLength);


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
