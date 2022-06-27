package fun.madeby.dbserver;

import fun.madeby.CarOwner;
import fun.madeby.DBRecord;
import fun.madeby.Index;
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
			assertEquals(index.getTotalNumberOfRows(), 1);
		}catch(IOException e) {
			System.out.println("addTest: add threw IOException");
		}
	}

	@Test
	@DisplayName("DBServer Add, then delete 0 == OK")
	void deleteTest() {
		try {
			this.db.add(carOwner);
			assertEquals(index.getTotalNumberOfRows(), 1);
			this.db.delete(0L);
			assertEquals(index.getTotalNumberOfRows(), 0);
		}catch(IOException e) {
			System.out.println("deleteTest: add threw IOException");
		}
	}

	@Test
	@DisplayName("DBServer readTest : 1 == OK and then test equality of each field") //shows on fail
	void readTest(){
		try {
			this.db.add(carOwner);
			assertEquals(index.getTotalNumberOfRows(), 1);
			DBRecord carOwner = this.db.read(0L);
			assertEquals(carOwner.getName(), "Rezzi Delamdi");
			assertEquals(carOwner.getAddress(), "Repoke Street, Antwerp, 2000");
			assertEquals(carOwner.getCarPlateNumber(), "3AR 4NVERS");
			assertEquals(carOwner.getDescription(), "The place under the bridge..");
			assertEquals(carOwner.getAge(), 34);
		}catch(IOException e) {
			System.out.println("readTest: read threw IOException");
		}
	}

}