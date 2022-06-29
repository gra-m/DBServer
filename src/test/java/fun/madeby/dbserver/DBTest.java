package fun.madeby.dbserver;

import fun.madeby.CarOwner;
import fun.madeby.DBRecord;
import fun.madeby.Index;
import fun.madeby.dbserver.exceptions.NameDoesNotExistException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import static org.junit.jupiter.api.Assertions.*;

class DBTest {
	private DB db;
	File file;
	private String dbFileName = "testdb.db";
	private DBRecord carOwner;
	private DBRecord carOwnerUpdated;
	private Index index;



	@BeforeEach
	public void setUp() {
		// delete and recreate testdb.db
		file = new File(dbFileName);
		if (file.exists())
			file.delete();
		try {
			this.db = new DBServer(dbFileName);
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// create CarOwner test object:
		carOwner = new CarOwner("Rezzi Delamdi",
				34,
				"Repoke Street, Antwerp, 2000",
				"3AR 4NVERS",
				"The place under the bridge..");

		carOwnerUpdated = new CarOwner("Razzgu Dulemdi",
				34,
				"Repoke Street, Antwerp, 2000",
				"BAR ANVERS",
				"The place under the bridge..");

		// get singleton
		index = Index.getInstance();
	}

	@AfterEach
	public void tearDown() throws IOException {
		//Clears index..
		this.db.close();
	}


	@Test
	@DisplayName("DBServer Add, via FileHandler test: 1 == OK")
	void addTest(){
		try {
			this.db.add(carOwner);
			assertEquals(1, index.getTotalNumberOfRows());
		}catch(IOException e) {
			System.out.println("addTest: add threw Exception");
		}
	}

	@Test
	@DisplayName("DBServer Add, then delete 0 == OK")
	void deleteTest() {
		try {
			this.db.add(carOwner);
			assertEquals(1, index.getTotalNumberOfRows());
			this.db.delete(0L);
			assertEquals(0, index.getTotalNumberOfRows());
		}catch(IOException e) {
			e.printStackTrace();
			System.out.println("deleteTest: threw Exception");
		}
	}

	@Test
	@DisplayName("DBServer readTest : 1 == OK and then test equality of each field") //shows on fail
	void readTest(){
		try {
			this.db.add(carOwner);
			assertEquals(index.getTotalNumberOfRows(), 1);
			DBRecord carOwner = this.db.read(0L);
			assertEquals("Rezzi Delamdi", carOwner.getName() );
			assertEquals("Repoke Street, Antwerp, 2000", carOwner.getAddress());
			assertEquals("3AR 4NVERS", carOwner.getCarPlateNumber());
			assertEquals("The place under the bridge..", carOwner.getDescription());
			assertEquals(34, carOwner.getAge());
		}catch(IOException e) {
			System.out.println("readTest: read threw Exception");
		}
	}

	@Test
	@DisplayName("Sets existing row 0L to deleted in .db file, then creates new row with modified data")
	void updateByRowTest() {
		try {
			// normal
			this.db.add(carOwner);
			assertEquals(1, index.getTotalNumberOfRows());
			this.db.update(0L, carOwnerUpdated );
			assertEquals(1, index.getTotalNumberOfRows());
			DBRecord retrieved = this.db.read(0L);
			assertEquals( "Razzgu Dulemdi", retrieved.getName());
			assertEquals("Repoke Street, Antwerp, 2000", retrieved.getAddress());
			assertEquals("BAR ANVERS", retrieved.getCarPlateNumber());
			assertEquals("The place under the bridge..", retrieved.getDescription());
			assertEquals(34, carOwner.getAge());

		}catch(IOException e) {
			System.out.println("updateByRowTest:  threw Exception");
		}
	}

	@Test
	@DisplayName("Sets existing row 0L (found by name) to deleted in .db file, then creates new row with modified data")
	void updateByNameTest() {
		try {
			// normal
			this.db.add(carOwner);
			assertEquals(1, index.getTotalNumberOfRows());
			Long retrievedRowNum = index.getRowNumberByName("Rezzi Delamdi");
			System.out.println("Retrieved row for 'Rezzi Delamdi' " + retrievedRowNum);
			this.db.update(retrievedRowNum -1, carOwnerUpdated );
			assertEquals(1, index.getTotalNumberOfRows());
			System.out.println("Rezzi Delamdi should have been deleted: ");
			index.printNameIndex();
			System.out.println("Update has been written in row position 0");
			DBRecord retrieved = this.db.read(index.getRowNumberByName("Razzgu Dulemdi") - index.getTotalNumberOfRows());
			assertEquals( "Razzgu Dulemdi", retrieved.getName());
			assertEquals("Repoke Street, Antwerp, 2000", retrieved.getAddress());
			assertEquals("BAR ANVERS", retrieved.getCarPlateNumber());
			assertEquals("The place under the bridge..", retrieved.getDescription());
			assertEquals(34, retrieved.getAge());
		}catch(IOException e) {
			System.out.println("updateByRowTest:  threw Exception");
		}
	}

}