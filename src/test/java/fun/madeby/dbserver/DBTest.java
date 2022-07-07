package fun.madeby.dbserver;

import fun.madeby.CarOwner;
import fun.madeby.DBRecord;
import fun.madeby.Index;
import fun.madeby.exceptions.NameDoesNotExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
class DBTest {
	private final String dbFileName = "testdb.db";
	private DBRecord carOwner;
	private DBRecord carOwnerUpdated;
	private Index index;



	@BeforeEach
	public void setUp() {

		// get singleton
		index = Index.getInstance();

		//Empties existing file comment out to see
		clearDataInExistingFile();

		// create CarOwner test objects:
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
	}

	private void clearDataInExistingFile() {
		try (BufferedWriter ignored = Files.newBufferedWriter(Path.of("./" + dbFileName),
				StandardOpenOption.TRUNCATE_EXISTING)) {
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("Test LevenshteinList 5 tolerance return 2")
	void testLevenshtein1_2() {
		try (DB db = new DBServer(dbFileName)){
			db.add(carOwner);
			db.add(carOwnerUpdated); //"R1zz23 D4l5mdi"
			assertEquals(2, index.getTotalNumberOfRows());
			ArrayList<DBRecord> returnedMatchesWithinTolerance = (ArrayList<DBRecord>) db.searchWithLevenshtein("Rezzi Delamdi", 5);
			assertEquals(2, returnedMatchesWithinTolerance.size());
		}catch(IOException e) {
			System.out.println("5Tolerance Levenshtein: threw Exception");
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("Test LevenshteinList 4 tolerance return 1")
	void testLevenshtein4_1() {
		try (DB db = new DBServer(dbFileName)){
			db.add(carOwner);
			db.add(carOwnerUpdated); //"R1zz23 D4l5mdi"
			assertEquals(2, index.getTotalNumberOfRows());
			ArrayList<DBRecord> returnedMatchesWithinTolerance = (ArrayList<DBRecord>) db.searchWithLevenshtein("Rezzi Delamdi", 4);
			assertEquals(1, returnedMatchesWithinTolerance.size());
		}catch(IOException e) {
			System.out.println("5Tolerance Levenshtein: threw Exception");
			e.printStackTrace();
		}

	}

	@Test
	@DisplayName("Test regex 'R.*'")
	void testRegEx() {
		try (DB db = new DBServer(dbFileName)){
			db.add(carOwner);
			db.add(carOwnerUpdated);
			assertEquals(2, index.getTotalNumberOfRows());
			ArrayList<DBRecord> returnedMatchesRegex = (ArrayList<DBRecord>) db.searchWithRegex("R.*");
			assertEquals(2, returnedMatchesRegex.size());
		}catch(IOException e) {
			System.out.println("5Tolerance Levenshtein: threw Exception");
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("Test regex 'Re.*'")
	void testRegEx2() {
		try (DB db = new DBServer(dbFileName)){
			db.add(carOwner);
			db.add(carOwnerUpdated);
			assertEquals(2, index.getTotalNumberOfRows());
			ArrayList<DBRecord> returnedMatchesRegex = (ArrayList<DBRecord>) db.searchWithRegex("Re.*");
			assertEquals(1, returnedMatchesRegex.size());
		}catch(IOException e) {
			System.out.println("5Tolerance Levenshtein: threw Exception");
			e.printStackTrace();
		}
	}


	@Test
	@DisplayName("DBServer Add, via FileHandler test: 1 == OK")
	void addTest(){
		try (DB db = new DBServer(dbFileName)){
			db.add(carOwner);
			assertEquals(1, index.getTotalNumberOfRows());
		}catch(IOException e) {
			System.out.println("addTest: add threw Exception");
		}
	}

	@Test
	@DisplayName("DBServer Add, then delete 0 == OK")
	void deleteTest() {
		try (DB db = new DBServer(dbFileName)){
			db.add(carOwner);
			assertEquals(1, index.getTotalNumberOfRows());
			db.delete(0L);
			assertEquals(0, index.getTotalNumberOfRows());
		}catch(IOException e) {
			System.out.println("deleteTest: threw Exception");
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("DBServer search, then compare retrieved")
	void searchTest() {
		try (DB db = new DBServer(dbFileName)){
			db.add(carOwner);
			assertEquals(1, index.getTotalNumberOfRows());
			CarOwner retrieved = (CarOwner) db.search("Rezzi Delamdi");
			assertNotNull(retrieved);
			assertEquals("Rezzi Delamdi", retrieved.getName() );
			assertEquals("Repoke Street, Antwerp, 2000", retrieved.getAddress());
			assertEquals("3AR 4NVERS", retrieved.getCarPlateNumber());
			assertEquals("The place under the bridge..", retrieved.getDescription());
			assertEquals(34, retrieved.getAge());

		}catch(IOException e) {
			System.out.println("searchTest: threw Exception");
			e.printStackTrace();
		}
	}



	@Test
	@DisplayName("DBServer readTest : 1 == OK and then test equality of each field") //shows on fail
	void readTest(){
		try (DB db = new DBServer(dbFileName)){
			db.add(carOwner);
			assertEquals(index.getTotalNumberOfRows(), 1);
			DBRecord readCarOwner = db.read(Index.getInstance().getRowNumberByName("Rezzi Delamdi"));
			assertNotNull(readCarOwner);
			assertEquals("Rezzi Delamdi", readCarOwner.getName() );
			assertEquals("Repoke Street, Antwerp, 2000", readCarOwner.getAddress());
			assertEquals("3AR 4NVERS", readCarOwner.getCarPlateNumber());
			assertEquals("The place under the bridge..", readCarOwner.getDescription());
			assertEquals(34, readCarOwner.getAge());
		}catch(EOFException e) {
			System.out.println("readTest: read threw Exception");
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("Sets existing row 0L to deleted in .db file, then creates new row with modified data")
	void updateByRowTest() {
		try (DB db = new DBServer(dbFileName)){
			// normal
			db.add(carOwner);
			assertEquals(1, index.getTotalNumberOfRows());
			db.update(0L, carOwnerUpdated );
			assertEquals(1, index.getTotalNumberOfRows());
			DBRecord retrieved = db.read(0L);
			assert retrieved != null;
			assertEquals( "Razzgu Dulemdi", retrieved.getName());
			assertEquals("Repoke Street, Antwerp, 2000", retrieved.getAddress());
			assertEquals("BAR ANVERS", retrieved.getCarPlateNumber());
			assertEquals("The place under the bridge..", retrieved.getDescription());
			assertEquals(34, carOwner.getAge());

		}catch(IOException e) {
			System.out.println("updateByRowTest:  threw Exception");
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("Sets existing row 0L (found by name) to deleted in .db file, then creates new row with modified data")
	void updateByNameTest() {
		try (DB db = new DBServer(dbFileName)){
			db.add(carOwner);
			assertEquals(1, index.getTotalNumberOfRows());
			Long retrievedRowNum = index.getRowNumberByName("Rezzi Delamdi");
			System.out.println("Retrieved row for 'Rezzi Delamdi' " + retrievedRowNum);
			db.update("Rezzi Delamdi", carOwnerUpdated );
			assertEquals(1, index.getTotalNumberOfRows());
			DBRecord retrieved = db.read(index.getRowNumberByName("Razzgu Dulemdi"));
			if (retrieved != null){
				assertEquals("Razzgu Dulemdi", Objects.requireNonNull(retrieved).getName());
				assertEquals("Repoke Street, Antwerp, 2000", retrieved.getAddress());
				assertEquals("BAR ANVERS", retrieved.getCarPlateNumber());
				assertEquals("The place under the bridge..", retrieved.getDescription());
				assertEquals(34, retrieved.getAge());
			}
		}catch(IOException e) {
			System.out.println("updateByRowTest:  threw Exception");
		} catch (NameDoesNotExistException e) {
			throw new RuntimeException(e);
		}
	}


}