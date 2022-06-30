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

class BaseFileHandlerTest {
	private RandomAccessFile dbFile;
	private static final int INTEGER_LENGTH_IN_BYTES = 4;
	private static final int BOOLEAN_LENGTH_IN_BYTES = 1;
	private String fileName = "bfhTest.db";
	private DBRecord carOwner;
	private long fileLength;
	private Object dataHandler;
	private Method readRawRecord;

	
	

	@BeforeEach
	void setUp() throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
// remove existing data and create dbFile.
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

		dataHandler = BaseFileHandler.class.getDeclaredConstructor(String.class).newInstance(fileName);
		readRawRecord = dataHandler.getClass().getDeclaredMethod("readRawRecord", Long.class);
		readRawRecord.setAccessible(true);

		Field field = dataHandler.getClass().getDeclaredField("dbFile");
		field.setAccessible(true);
		field.set(dbFile, new RandomAccessFile(fileName, "rw"));
	}

	private long getFileLength() throws IOException {
		return dbFile.length();
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
		byte[] data;
		data = (byte[]) readRawRecord.invoke(dataHandler, 0L);

		long extraDataLength = BOOLEAN_LENGTH_IN_BYTES + INTEGER_LENGTH_IN_BYTES;
		long expectedRawRecordLength = fileLength - extraDataLength;
		assertEquals(expectedRawRecordLength, data.length);


	}
}