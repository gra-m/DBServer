package fun.madeby;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileHandlerTest {
	private RandomAccessFile dbFile;
	private static final int INTEGER_LENGTH_IN_BYTES = 4;
	private static final int BOOLEAN_LENGTH_IN_BYTES = 1;
	private String fileName = "fhTest.db";
	private DBRecord carOwner;
	private long fileLength;
	private Object fileHandler;
	private Method readRawRecord;

	
	

	@BeforeEach
	void setUp() throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		// bring in private method via reflection, this is just for experience:
		fileHandler = FileHandler.class.getDeclaredConstructor(String.class).newInstance(fileName);
		readRawRecord = fileHandler.getClass().getDeclaredMethod("readRawRecord", Long.class);
		readRawRecord.setAccessible(true);






		// remove existing data
		try (BufferedWriter ignored = Files.newBufferedWriter(Path.of(fileName),
				StandardOpenOption.TRUNCATE_EXISTING)) {
			dbFile = new RandomAccessFile(fileName, "rw");
		} catch (IOException e) {
			e.printStackTrace();
		}

		carOwner = new CarOwner("Rezzi Delamdi",
				34,
				"Repoke Street, Antwerp, 2000",
				"3AR 4NVERS",
				"The place under the bridge..");
		
		addtestDBRecord(carOwner);
		fileLength = getFileLength();
		
		
	}

	private long getFileLength() throws IOException {
		return this.dbFile.length();
	}

	private void addtestDBRecord(DBRecord carOwner) throws IOException {
		long length = carOwner.getLength();

		dbFile.seek(getFileLength());

		dbFile.writeBoolean(false);
		dbFile.writeInt((int) length);

		String name = carOwner.getName();
		dbFile.writeInt(name.length());
		dbFile.write(name.getBytes());

		int age = carOwner.getAge();
		dbFile.writeInt(age);

		String address = carOwner.getAddress();
		dbFile.writeInt(address.length());
		dbFile.write(address.getBytes());

		String carPlateNumber = carOwner.getCarPlateNumber();
		dbFile.writeInt(carPlateNumber.length());
		dbFile.write(carPlateNumber.getBytes());

		String description = carOwner.getDescription();
		dbFile.writeInt(description.length());
		dbFile.write(description.getBytes());
	}

	@Test
	void readRowTest() {
		long extraDataLength = BOOLEAN_LENGTH_IN_BYTES + INTEGER_LENGTH_IN_BYTES;
		System.out.println(fileLength);

	}

	@Test
	void readRawRecordTest() throws InvocationTargetException, IllegalAccessException {
		// testing of private methods "better at public level" but I read about Reflection and wanted to try:
		// https://stackoverflow.com/questions/34571/how-do-i-test-a-class-that-has-private-methods-fields-or-inner-classes
		byte[] data = null;
		data = (byte[]) readRawRecord.invoke(fileHandler, 0L);
		long extraDataLength = BOOLEAN_LENGTH_IN_BYTES + INTEGER_LENGTH_IN_BYTES;
		long expectedRawRecordLength = fileLength - extraDataLength;
		assertEquals(expectedRawRecordLength, data.length);


	}
}