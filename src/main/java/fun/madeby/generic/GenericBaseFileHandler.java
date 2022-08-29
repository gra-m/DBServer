package fun.madeby.generic;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fun.madeby.DataHandlerGeneric;
import fun.madeby.exceptions.DBException;
import fun.madeby.util.DebugInfo;
import fun.madeby.util.DebugRowInfo;
import fun.madeby.util.GeneralUtils;
import fun.madeby.util.LoggerSetUp;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import static fun.madeby.db.DBFactory.DEFAULT_ENCODING;

/**
 * Created by Gra_m on 2022 06 30
 */

@SuppressFBWarnings({"NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "EI_EXPOSE_REP2"})
public class GenericBaseFileHandler implements DataHandlerGeneric {
	RandomAccessFile dbFile;
	String dbFileName;
	private static final String VERSION = "0.1";
	private static final int START_OF_FILE = 0;
	private static final int HEADER_INFO_SPACE = 100;
	final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	final Lock readLock = readWriteLock.readLock();
	final Lock writeLock = readWriteLock.writeLock();
	protected static final int LONG_LENGTH_IN_BYTES = 8;
	protected static final int INTEGER_LENGTH_IN_BYTES = 4;
	protected static final int BOOLEAN_LENGTH_IN_BYTES = 1;
	protected Schema schema;
	protected Class aClass;
	protected GenericIndex index;
	Logger LOGGER;

	{
		try {
			LOGGER = LoggerSetUp.setUpLogger("BaseFileHandler/FH");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public GenericBaseFileHandler(RandomAccessFile randomAccessFile, final String fileName, final GenericIndex index) {
		this.dbFile = randomAccessFile;
		this.dbFileName = fileName;
		this.index = index;
	}


	public GenericBaseFileHandler(final String fileName, GenericIndex index) throws FileNotFoundException
		{
		this.dbFile = new RandomAccessFile(fileName, "rw");
		this.dbFileName = fileName;
		this.index = index;
		writeVersionInfoIfNewFile();
	}

	public void setSchema(Schema schema) {
		this.schema = schema;
	}

	public void setAClass(Class aClass) {
		this.aClass = aClass;
	}


	public void writeVersionInfoIfNewFile() {
		try {
			if (dbFile.length() == 0) {
				this.setDBVersion();
			} else {
				LOGGER.finest("@BFH writeVersionInfoIfNewFile() and dbFile.length() > 0 DBVersion is: " + VERSION);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public Boolean commit(Collection<Long> newRowsBytePosition, Collection<Long> deletedRowsBytePosition) throws DBException
		{
		writeLock.lock();
		try {
			// commit new Rows
			for (Long position : newRowsBytePosition) {
				this.dbFile.seek(position);
				dbFile.writeBoolean(false); //  !isTemporary
				// re-read the record
				byte[] b = this.readRawRecord(position);
				Object object = readFromByteStream(new DataInputStream(new ByteArrayInputStream(b)));
				// add to index

				this.index.add(position); // increments total number of rows
				String genericIndexedValue = (String)object.getClass().getDeclaredField(schema.indexBy).get(object);
				this.index.addGenericIndexedValue(genericIndexedValue, this.index.getTotalNumberOfRows() - 1); // does not increment total num of rows.
			}

			// commit deletedRows
			for (Long position : deletedRowsBytePosition) {
				this.dbFile.seek(position);
				dbFile.writeBoolean(false); // !isTemporary
				this.index.removeByFilePosition(position);
			}
		} catch (IOException | IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
		} finally {
			writeLock.unlock();
		}
		return true;
	}



	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("SF_SWITCH_NO_DEFAULT")
	public Object readFromByteStream(final DataInputStream stream) throws IOException, DBException
		{
		Object object = null;
		// Get empty object of class passed via reflection, via constructor now as safer.
		try {
			object = Class.forName(this.aClass.getCanonicalName()).getDeclaredConstructor().newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			LOGGER.severe("@GBFileHandler readObjFromByteStream(stream): " + this.aClass.getSimpleName());
			e.printStackTrace();
		}

		try {
		for (SchemaField field: this.schema.schemaFields) {

			switch (field.fieldType.toLowerCase()) {
				case "string" -> {
					String value = null;
					int fieldLength = stream.readInt();
					byte[] bArray = new byte[fieldLength];
					int length = stream.read(bArray);
					if (GeneralUtils.testInputStreamReadLength("GBFH/readFromByteStream", length, fieldLength))
						value =  new String(bArray, 0, length, DEFAULT_ENCODING);
					object.getClass()
							.getDeclaredField(field.fieldName)
							.set(object, value);

				}
				case "boolean" -> {
					boolean value = stream.readBoolean();
					object.getClass()
							.getDeclaredField((field.fieldName))
							.set(object, value);

				}
				case "int" -> {
					int value = stream.readInt();
					object.getClass()
							.getDeclaredField(field.fieldName)
							.set(object, value);
				}
				case "long" -> {
					long value = stream.readLong();
					object.getClass()
							.getDeclaredField(field.fieldName)
							.set(object, value);
				}
				default -> {
					String unfoundFieldType = "fieldType_of_" + field.fieldType + "_of_name_" + field.fieldName + "_not_found";
					throw new DBException("@GBFH/readFromByteStream: Field type not recognised by switch " + unfoundFieldType);
				}
			}
		}

		} catch (NoSuchFieldException | IllegalAccessException e) {
			LOGGER.info("@GBFileHandler readObjFromByteStream(stream): " + e.getMessage());
			e.printStackTrace();
		}


		return object;
	}

	@Override
	public Boolean rollback(Collection<Long> newRowsBytePosition, Collection<Long> deletedRowsBytePosition) throws DBException
		{
		writeLock.lock();
		try {
			// rollback new Rows
			for (Long position : newRowsBytePosition) {
				this.dbFile.seek(position);
				dbFile.writeBoolean(false); //  !isTemporary
				this.dbFile.seek(position + BOOLEAN_LENGTH_IN_BYTES);
				dbFile.writeBoolean(true); // isDeleted

				this.index.removeByFilePosition(position);
			}
			// rollback deletedRows
			for (Long position : deletedRowsBytePosition) {
				this.dbFile.seek(position);
				dbFile.writeBoolean(false); // !isTemporary
				this.dbFile.seek(position + BOOLEAN_LENGTH_IN_BYTES);
				dbFile.writeBoolean(false); // !isDeleted flag

				byte[] b = this.readRawRecord(position);
				Object object = readFromByteStream(new DataInputStream(new ByteArrayInputStream(b)));
				String genericIndexedValue = (String) object.getClass().getDeclaredField(schema.indexBy).get(object);
				this.index.addGenericIndexedValue(genericIndexedValue, this.index.getTotalNumberOfRows()); // does not increment total num of rows.
				this.index.add(position); //
			}
		} catch (IOException | IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
		} finally {
			writeLock.unlock();
		}
		return true;
	}


	public void populateIndex() throws DBException
		{
		LOGGER.finest("@GBFH PopulateIndex()");
		long rowNum = 0;
		int recordLength = 0;
		long currentPosition = HEADER_INFO_SPACE;
		long deletedRows = 0;
		long temporaryRows = 0;

		if (isExistingData()) {
			writeLock.lock();
			try {
				this.index.resetTotalNumberOfRows();
				while (currentPosition < this.dbFile.length()) {
					this.dbFile.seek(currentPosition);
					boolean isTemporary = this.dbFile.readBoolean(); // new read ifTemporary
					if (isTemporary)
						++temporaryRows;
					this.dbFile.seek(currentPosition + BOOLEAN_LENGTH_IN_BYTES);
					boolean isDeleted = this.dbFile.readBoolean();
					if (!isDeleted) {
						this.index.add(currentPosition);
					} else deletedRows++;

					currentPosition += BOOLEAN_LENGTH_IN_BYTES + BOOLEAN_LENGTH_IN_BYTES;
					recordLength = this.dbFile.readInt();
					currentPosition += INTEGER_LENGTH_IN_BYTES;
					if (!isDeleted) {
						this.dbFile.seek(currentPosition);
						byte[] byteArrayOfObject = new byte[recordLength];
						int readLength = dbFile.read(byteArrayOfObject);
						GeneralUtils.testInputStreamReadLength("@GBFH/populateIndex", readLength, readLength);
						Object retrievedObject = readFromByteStream(new DataInputStream(new ByteArrayInputStream(byteArrayOfObject)));
						String genericIndexedValue = (String) retrievedObject.getClass().getDeclaredField(schema.indexBy).get(retrievedObject);
						this.index.addGenericIndexedValue(genericIndexedValue, rowNum++);
					}
					currentPosition += recordLength;
					System.out.printf("BFH: PopulateIndex(): total rows - %d | total deleted - %d | total - temporary - %d %n", rowNum, deletedRows, temporaryRows);
				}
			} catch (IOException | NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			} finally {
				writeLock.unlock();
			}
		}
	}


	public boolean isExistingData() {
		try {
			if (this.dbFile.length() == HEADER_INFO_SPACE) {
				LOGGER.severe("BFH: populateIndex -> isExistingData() no existing data, nothing to index.");
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
	 * @return empty byte[] if boolean(deleted),  byte[] of row requested beginning with 3 bytes representing the genericIndexedValue
	 * length int.
	 */
	public byte[] readRawRecord(Long rowsBytePosition) throws DBException
		{
		byte[] data = null;

		readLock.lock();
		try {
			dbFile.seek(rowsBytePosition);
			boolean isTemporary = dbFile.readBoolean();
			dbFile.seek(rowsBytePosition + BOOLEAN_LENGTH_IN_BYTES);
			boolean isDeleted = dbFile.readBoolean();
			if (isTemporary) {
				LOGGER.severe("@BaseFileHandler readRawRecord(Long rowsBytePosition) attempting to read isTemporary row");
				return new byte[]{-1};
			} else if (isDeleted) {
				LOGGER.severe("@BaseFileHandler readRawRecord(Long rowsBytePosition) attempting to read isDeleted row");
				return new byte[]{-1};
			} else {
				dbFile.seek(rowsBytePosition + BOOLEAN_LENGTH_IN_BYTES + BOOLEAN_LENGTH_IN_BYTES); // 2 byte = 2* boolean
				int recordLength = dbFile.readInt();
				dbFile.seek(rowsBytePosition + BOOLEAN_LENGTH_IN_BYTES + BOOLEAN_LENGTH_IN_BYTES + INTEGER_LENGTH_IN_BYTES); // 6 bytes boolean + int
				data = new byte[recordLength];
				int length = this.dbFile.read(data);
				GeneralUtils.testInputStreamReadLength("@GBFH/readRawRecord", length, recordLength);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			readLock.unlock();
		}
		return data;
	}


	public void close() throws IOException {
		this.dbFile.close();
	}

	public Collection<DebugInfo> getCurrentDebugInfoRows() throws DBException
		{
		LOGGER.finest("@BFH getCurrentDebugInfoRows() collecting debugInfo 1Object 2isTemporary 3isDeleted for each row in .db");
		readLock.lock();
		DataInputStream stream;
		DebugInfo debugInfo;
		ArrayList<DebugInfo> returnArrayList = null;
		try {
			if (dbFile.length() == HEADER_INFO_SPACE)
				return new ArrayList<>();
			else {
				boolean isTemporary;
				boolean isDeleted;
				Object object;
				int recordLength;
				long currentPosition = HEADER_INFO_SPACE;
				returnArrayList = new ArrayList<>();
				this.dbFile.seek(currentPosition);

				while (currentPosition < this.dbFile.length()) {
					LOGGER.finest("@BFH getCurrentDebugInfoRows() while loop file length is: " + this.dbFile.length() + " current position is: " + currentPosition);
					isTemporary = dbFile.readBoolean();
					dbFile.seek(currentPosition + BOOLEAN_LENGTH_IN_BYTES);

					isDeleted = dbFile.readBoolean();
					dbFile.seek(currentPosition + BOOLEAN_LENGTH_IN_BYTES + BOOLEAN_LENGTH_IN_BYTES);

					recordLength = dbFile.readInt();
					dbFile.seek(currentPosition + BOOLEAN_LENGTH_IN_BYTES + BOOLEAN_LENGTH_IN_BYTES + INTEGER_LENGTH_IN_BYTES);
					byte[] rowDataOnly = new byte[recordLength];
					int readLength = dbFile.read(rowDataOnly);
					GeneralUtils.testInputStreamReadLength("@GBFH/getCurrentDebugInfoRows", readLength, readLength);
					stream = new DataInputStream(new ByteArrayInputStream(rowDataOnly));
					object = readFromByteStream(stream);

					debugInfo = new DebugRowInfo(object, isTemporary, isDeleted);
					returnArrayList.add(debugInfo);

					currentPosition += recordLength + BOOLEAN_LENGTH_IN_BYTES + BOOLEAN_LENGTH_IN_BYTES + INTEGER_LENGTH_IN_BYTES;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			readLock.unlock();
		}

		return returnArrayList;
	}


	public String getTableName() {
		return dbFileName;
	}


	public boolean deleteFile() {
		writeLock.lock();
		try {
			this.dbFile.close();
			if (new File(this.dbFileName).delete()) {
				System.out.println("File successfully deleted");
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			writeLock.unlock();
		}

		System.out.println("File deletion failed");
		return false;
	}

	private void setDBVersion() {
		try {
			this.dbFile.seek(START_OF_FILE);
			this.dbFile.writeBytes(VERSION);
			char[] characterFiller = new char[HEADER_INFO_SPACE - VERSION.length()];
			Arrays.fill(characterFiller, ' ');
			this.dbFile.write(new String(characterFiller).getBytes(DEFAULT_ENCODING));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getDBVersion() throws DBException
		{
		readLock.lock();
		try {
			this.dbFile.seek(START_OF_FILE);
			byte[] bytes = new byte[HEADER_INFO_SPACE];
			int readLength = this.dbFile.read(bytes);
			GeneralUtils.testInputStreamReadLength("@GBFH/getDBVersion", readLength, HEADER_INFO_SPACE);
			return new String(bytes, 0, readLength, DEFAULT_ENCODING).trim();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			readLock.unlock();
		}
		return "Read Fail @ getDBVersion";
	}

}