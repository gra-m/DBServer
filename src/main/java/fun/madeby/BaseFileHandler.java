package fun.madeby;

import fun.madeby.util.DebugInfo;
import fun.madeby.util.DebugRowInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Gra_m on 2022 06 30
 */

public class BaseFileHandler implements DataHandler {
	public RandomAccessFile dbFile;
	int INTEGER_LENGTH_IN_BYTES = 4;
	int BOOLEAN_LENGTH_IN_BYTES = 1;


	public BaseFileHandler(String fileName) throws FileNotFoundException {
		this.dbFile = new RandomAccessFile(fileName, "rw");
	}


	public void populateIndex() {
		long rowNum = 0;
		int recordLength = 0;
		long currentPosition = 0;
		long deletedRows = 0;

		if (isExistingData()) {
			try {
				Index.getInstance().resetTotalNumberOfRows();
				while (currentPosition < this.dbFile.length()) {
					this.dbFile.seek(currentPosition);
					boolean isDeleted = this.dbFile.readBoolean();
					if (!isDeleted) {
						Index.getInstance().add(currentPosition);
					} else deletedRows++;

					currentPosition += BOOLEAN_LENGTH_IN_BYTES;
					recordLength = this.dbFile.readInt();
					currentPosition += INTEGER_LENGTH_IN_BYTES;
					if (!isDeleted) {
						this.dbFile.seek(currentPosition);
						byte[] retrieveRecord = new byte[recordLength];
						dbFile.read(retrieveRecord);
						DBRecord retrievedRecord = readFromByteStream(new DataInputStream(new ByteArrayInputStream(retrieveRecord)));
						Index.getInstance().addNameToIndex(retrievedRecord.getName(), rowNum++);
					}
					currentPosition += recordLength;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isExistingData() {
		try {
			if (this.dbFile.length() == 0) {
				System.out.println("BFH: isExistingData() no existing data, nothing to index.");
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Reads the raw record, returns record data without storage information.
	 *
	 * @param rowsBytePosition Not working with index currently, working if passed -1L.
	 * @return empty byte[] if boolean(deleted),  byte[] of row requested beginning with 3 bytes representing the name
	 * length int.
	 */
	public byte[] readRawRecord(Long rowsBytePosition) {
		byte[] data = null;

		try {
			dbFile.seek(rowsBytePosition);
			if (dbFile.readBoolean()) {
				System.out.println("BFH: DELETE: Marked as deleted");
				return new byte[-1];
			}
			dbFile.seek(rowsBytePosition + BOOLEAN_LENGTH_IN_BYTES); // 1 byte boolean
			int recordLength = dbFile.readInt();
			dbFile.seek(rowsBytePosition + BOOLEAN_LENGTH_IN_BYTES + INTEGER_LENGTH_IN_BYTES); // 5 bytes boolean + int
			data = new byte[recordLength];
			this.dbFile.read(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}


	public DBRecord readFromByteStream(final DataInputStream stream) throws IOException {

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

	public void close() throws IOException {
		this.dbFile.close();
	}

	public Collection<DebugInfo> getCurrentData() {
		DataInputStream stream;
		DebugInfo debugInfo;
		ArrayList<DebugInfo> returnArrayList = null;
		try {
			if (dbFile.length() == 0)
				return new ArrayList<>();
			else {
				boolean isDeleted;
				DBRecord dbRecord;
				int recordLength;
				long currentPosition = 0;
				returnArrayList = new ArrayList<>();
				this.dbFile.seek(currentPosition);

				while (currentPosition < this.dbFile.length()) {
					isDeleted = dbFile.readBoolean();
					dbFile.seek(currentPosition + BOOLEAN_LENGTH_IN_BYTES);
					recordLength = dbFile.readInt();
					dbFile.seek(currentPosition + BOOLEAN_LENGTH_IN_BYTES + INTEGER_LENGTH_IN_BYTES);
					byte[] rowDataOnly = new byte[recordLength];
					dbFile.read(rowDataOnly);
					stream = new DataInputStream(new ByteArrayInputStream(rowDataOnly));
					dbRecord = readFromByteStream(stream);

					debugInfo = new DebugRowInfo(dbRecord, isDeleted);
					returnArrayList.add(debugInfo);

					currentPosition += recordLength + BOOLEAN_LENGTH_IN_BYTES + INTEGER_LENGTH_IN_BYTES;
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}

	return returnArrayList;
	}

}
