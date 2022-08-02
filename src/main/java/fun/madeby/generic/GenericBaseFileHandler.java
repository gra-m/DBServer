package fun.madeby.generic;

import fun.madeby.DBRecord;
import fun.madeby.DataHandlerGeneric;
import fun.madeby.specific.Index;
import fun.madeby.util.DebugInfo;
import fun.madeby.util.DebugRowInfo;
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

/**
 * Created by Gra_m on 2022 06 30
 */

public class GenericBaseFileHandler implements DataHandlerGeneric {
	RandomAccessFile dbFile;
	String dbFileName;
	private final String VERSION = "0.1";
	private static final int START_OF_FILE = 0;
	private static final int HEADER_INFO_SPACE = 100;
	final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	final Lock readLock = readWriteLock.readLock();
	final Lock writeLock = readWriteLock.writeLock();
	protected final int LONG_LENGTH_IN_BYTES = 8;
	protected final int INTEGER_LENGTH_IN_BYTES = 4;
	protected final int BOOLEAN_LENGTH_IN_BYTES = 1;
	protected Schema schema;
	protected Class aClass;
	Logger LOGGER;

	{
		try {
			LOGGER = LoggerSetUp.setUpLogger("BaseFileHandler/FH");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public GenericBaseFileHandler(RandomAccessFile randomAccessFile, final String fileName) {
		this.dbFile = randomAccessFile;
		this.dbFileName = fileName;
	}


	public GenericBaseFileHandler(final String fileName) throws FileNotFoundException {
		this.dbFile = new RandomAccessFile(fileName, "rw");
		this.dbFileName = fileName;
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
	public Boolean commit(Collection<Long> newRowsBytePosition, Collection<Long> deletedRowsBytePosition) {
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

				Index.getInstance().add(position);
				String name = (String)object.getClass().getDeclaredField("pName").get(object);
				Index.getInstance().addNameToIndex(name, Index.getInstance().getTotalNumberOfRows() - 1); // does not increment total num of rows.
			}

			// commit deletedRows
			for (Long position : deletedRowsBytePosition) {
				this.dbFile.seek(position);
				dbFile.writeBoolean(false); // !isTemporary
				Index.getInstance().removeByFilePosition(position);
			}
		} catch (IOException | IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
		} finally {
			writeLock.unlock();
		}
		return true;
	}



	public Object readFromByteStream(final DataInputStream stream) throws IOException {
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
					int fieldLength = stream.readInt();
					byte[] bArray = new byte[fieldLength];
					//stream.read(bArray); IJ always complains about this
					//String value = new String(bArray);
					String value = (String.valueOf(stream.read(bArray)));
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
			}
		}

		} catch (NoSuchFieldException | IllegalAccessException e) {
			LOGGER.info("@GBFileHandler readObjFromByteStream(stream): " + e.getMessage());
			e.printStackTrace();
		}


		return object;
	}

	@Override
	public Boolean rollback(Collection<Long> newRowsBytePosition, Collection<Long> deletedRowsBytePosition) {
		writeLock.lock();
		try {
			// rollback new Rows
			for (Long position : newRowsBytePosition) {
				this.dbFile.seek(position);
				dbFile.writeBoolean(false); //  !isTemporary
				this.dbFile.seek(position + BOOLEAN_LENGTH_IN_BYTES);
				dbFile.writeBoolean(true); // isDeleted

				Index.getInstance().removeByFilePosition(position);
			}
			// rollback deletedRows
			for (Long position : deletedRowsBytePosition) {
				this.dbFile.seek(position);
				dbFile.writeBoolean(false); // !isTemporary
				this.dbFile.seek(position + BOOLEAN_LENGTH_IN_BYTES);
				dbFile.writeBoolean(false); // !isDeleted flag

				byte[] b = this.readRawRecord(position);
				Object object = readFromByteStream(new DataInputStream(new ByteArrayInputStream(b)));
				String name = (String) object.getClass().getDeclaredField("pName").get(object);
				Index.getInstance().addNameToIndex(name, Index.getInstance().getTotalNumberOfRows()); // does not increment total num of rows.
				Index.getInstance().add(position); //
			}
		} catch (IOException | IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
		} finally {
			writeLock.unlock();
		}
		return true;
	}

	// populateIndexFromFile == better his name better!
	public void populateIndex() {
		LOGGER.finest("@GBFH PopulateIndex()");
		long rowNum = 0;
		int recordLength = 0;
		long currentPosition = HEADER_INFO_SPACE;
		long deletedRows = 0;
		long temporaryRows = 0;

		if (isExistingData()) {
			writeLock.lock();
			try {
				Index.getInstance().resetTotalNumberOfRows();
				while (currentPosition < this.dbFile.length()) {
					this.dbFile.seek(currentPosition);
					boolean isTemporary = this.dbFile.readBoolean(); // new read ifTemporary
					if (isTemporary)
						++temporaryRows;
					this.dbFile.seek(currentPosition + BOOLEAN_LENGTH_IN_BYTES);
					boolean isDeleted = this.dbFile.readBoolean();
					if (!isDeleted) {
						Index.getInstance().add(currentPosition);
					} else deletedRows++;

					currentPosition += BOOLEAN_LENGTH_IN_BYTES + BOOLEAN_LENGTH_IN_BYTES;
					recordLength = this.dbFile.readInt();
					currentPosition += INTEGER_LENGTH_IN_BYTES;
					if (!isDeleted) {
						this.dbFile.seek(currentPosition);
						byte[] byteArrayOfObject = new byte[recordLength];
						dbFile.read(byteArrayOfObject);
						Object retrievedObject = readFromByteStream(new DataInputStream(new ByteArrayInputStream(byteArrayOfObject)));
						String name = (String) retrievedObject.getClass().getDeclaredField("pName").get(retrievedObject);
						Index.getInstance().addNameToIndex(name, rowNum++);
					}
					currentPosition += recordLength;
					System.out.printf("BFH: PopulateIndex(): total rows - %d | total deleted - %d | total - temporary - %d \n", rowNum, deletedRows, temporaryRows);
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
			if (this.dbFile.length() == 0) {
				System.out.println("BFH: populateIndex -> isExistingData() no existing data, nothing to index.");
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
				this.dbFile.read(data);
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

	public Collection<DebugInfo> getCurrentDebugInfoRows() {
		LOGGER.finest("@BFH getCurrentDebugInfoRows()");
		readLock.lock();
		DataInputStream stream;
		DebugInfo debugInfo;
		ArrayList<DebugInfo> returnArrayList = null;
		try {
			if (dbFile.length() == 0)
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
					dbFile.read(rowDataOnly);
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


	public String getDbFileName() {
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
			//this.dbFile.write(VERSION.getBytes());
			char[] characterFiller = new char[HEADER_INFO_SPACE - VERSION.length()];
			Arrays.fill(characterFiller, ' ');
			this.dbFile.write(new String(characterFiller).getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getDBVersion() {
		readLock.lock();
		try {
			this.dbFile.seek(START_OF_FILE);
			byte[] bytes = new byte[HEADER_INFO_SPACE];
			this.dbFile.read(bytes);
			return new String(bytes).trim();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			readLock.unlock();
		}
		return "Read Fail @ getDBVersion";
	}

}