package fun.madeby.dbserver;

import fun.madeby.CarOwner;
import fun.madeby.DBRecord;
import fun.madeby.db.specific_server.DB;
import fun.madeby.db.specific_server.DBServer;
import fun.madeby.specific.Index;
import fun.madeby.util.DebugInfo;
import fun.madeby.util.DebugRowInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
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

	@SuppressWarnings("EmptyTryBlock")
	private void clearDataInExistingFile() {
		try (BufferedWriter ignored = Files.newBufferedWriter(Path.of("./" + dbFileName),
				StandardOpenOption.TRUNCATE_EXISTING)) {
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Test
	@DisplayName("testLevenshtein5_2(): 5 tolerance return 2")
	void testLevenshtein5_2() {
		try (DB db = new DBServer(dbFileName)){
			db.beginTransaction();
			db.add(carOwner);
			db.add(carOwnerUpdated); //"R1zz23 D4l5mdi"
			db.commit();
			assertEquals(2, index.getTotalNumberOfRows());
			ArrayList<DBRecord> returnedMatchesWithinTolerance = (ArrayList<DBRecord>) db.searchWithLevenshtein("Rezzi Delamdi", 5);
			assertEquals(2, returnedMatchesWithinTolerance.size());
		}catch(IOException e) {
			System.out.println("5Tolerance Levenshtein: threw Exception");
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("testLevenshtein4_1():  4 tolerance return 1")
	void testLevenshtein4_1() {
		try (DB db = new DBServer(dbFileName)){
			db.beginTransaction();
			db.add(carOwner);
			db.add(carOwnerUpdated); //"R1zz23 D4l5mdi"
			db.commit();
			assertEquals(2, index.getTotalNumberOfRows());
			ArrayList<DBRecord> returnedMatchesWithinTolerance = (ArrayList<DBRecord>) db.searchWithLevenshtein("Rezzi Delamdi", 4);
			assertEquals(1, returnedMatchesWithinTolerance.size());
		}catch(IOException e) {
			System.out.println("5Tolerance Levenshtein: threw Exception");
			e.printStackTrace();
		}

	}

	@Test
	@DisplayName("testRegEx(): 'R.*'")
	void testRegEx() {
		try (DB db = new DBServer(dbFileName)){
			db.beginTransaction();
			db.add(carOwner);
			db.add(carOwnerUpdated);
			db.commit();
			assertEquals(2, index.getTotalNumberOfRows());
			ArrayList<DBRecord> returnedMatchesRegex = (ArrayList<DBRecord>) db.searchWithRegex("R.*");
			assertEquals(2, returnedMatchesRegex.size());
		}catch(IOException e) {
			System.out.println("5Tolerance Levenshtein: threw Exception");
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("testRegEx2():  'Re.*'")
	void testRegEx2() {
		try (DB db = new DBServer(dbFileName)){
			db.beginTransaction();
			db.add(carOwner);
			db.add(carOwnerUpdated);
			db.commit();
			assertEquals(2, index.getTotalNumberOfRows());
			ArrayList<DBRecord> returnedMatchesRegex = (ArrayList<DBRecord>) db.searchWithRegex("Re.*");
			assertEquals(1, returnedMatchesRegex.size());
		}catch(IOException e) {
			System.out.println("5Tolerance Levenshtein: threw Exception");
			e.printStackTrace();
		}
	}


	@Test
	@DisplayName("addTest(): DBServer Add, via FileHandler test: 1 == OK")
	void addTest(){
		try (DB db = new DBServer(dbFileName)){
			db.beginTransaction();
			db.add(carOwner);
			db.commit();
			assertEquals(1, index.getTotalNumberOfRows());
		}catch(IOException e) {
			System.out.println("addTest: add threw Exception");
		}
	}

/*	@Test //todo see issue 34
	@DisplayName("deleteTest(): DBServer Add then delete separate transactions")
	void deleteTest() {
		try (DB db = new DBServer(dbFileName)){
			db.beginTransaction();
			db.add(carOwner);
			db.commit();
			assertEquals(1, index.getTotalNumberOfRows());
			db.beginTransaction();
			db.delete(0L);
			db.commit();
			assertEquals(0, index.getTotalNumberOfRows());
		}catch(IOException e) {
			System.out.println("deleteTest: threw Exception");
			e.printStackTrace();
		}
	}*/


	@Test
	@DisplayName("searchTest(): DBServer search, then compare retrieved")
	void searchTest() {
		try (DB db = new DBServer(dbFileName)){
			db.beginTransaction();
			db.add(carOwner);
			db.commit();
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
			db.beginTransaction();
			db.add(carOwner);
			db.commit();
			assertEquals(index.getTotalNumberOfRows(), 1);
			DBRecord readCarOwner = db.read(Index.getInstance().getRowNumberByName("Rezzi Delamdi"));
			assertNotNull(readCarOwner);
			assertEquals("Rezzi Delamdi", readCarOwner.getName() );
			assertEquals("Repoke Street, Antwerp, 2000", readCarOwner.getAddress());
			assertEquals("3AR 4NVERS", readCarOwner.getCarPlateNumber());
			assertEquals("The place under the bridge..", readCarOwner.getDescription());
			assertEquals(34, readCarOwner.getAge());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("UpdateByRowTest(): Sets existing row 0L to deleted in .db file, then creates new row with modified data")
	void updateByRowTest() {
		try (DB db = new DBServer(dbFileName)){
			// normal
			db.beginTransaction();
			db.add(carOwner);
			db.commit();
			assertEquals(1, index.getTotalNumberOfRows());
			db.beginTransaction();
			db.update(0L, carOwnerUpdated );
			db.commit();
			assertEquals(1, index.getTotalNumberOfRows());
			//db.defragmentDatabase(); // todo defrag breaks this test (adding as bug @issue 35) retrieved == null
			//DBRecord retrieved = db.read(Index.getInstance().getRowNumberByName("Razzgu Dulemdi")); // worked fine
			DBRecord retrieved = db.read(1L);
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



	@Test // todo see issue 34
	@DisplayName("UpdateByNameTest: Sets existing row 0L (found by name) to deleted in .db file, then creates new row with modified data")
	void updateByNameTest() {
		try (DB db = new DBServer(dbFileName)){
			db.beginTransaction();
			db.add(carOwner);
			db.commit();
			assertEquals(1, index.getTotalNumberOfRows());
			Long retrievedRowNum = index.getRowNumberByName("Rezzi Delamdi");
			System.out.println("Retrieved row for 'Rezzi Delamdi' " + retrievedRowNum);
			db.beginTransaction();
			db.update("Rezzi Delamdi", carOwnerUpdated );
			db.commit();
			assertEquals(1, index.getTotalNumberOfRows());
			DBRecord retrieved = db.read(index.getRowNumberByName("Razzgu Dulemdi"));
			if (retrieved != null) {
				assertEquals("Razzgu Dulemdi", Objects.requireNonNull(retrieved).getName());
				assertEquals("Repoke Street, Antwerp, 2000", retrieved.getAddress());
				assertEquals("BAR ANVERS", retrieved.getCarPlateNumber());
				assertEquals("The place under the bridge..", retrieved.getDescription());
				assertEquals(34, retrieved.getAge());
			}
		} catch (IOException e) {
			System.out.println("updateByNameTest:  threw Exception");
		}
	}



	@Test
	@DisplayName("Searches for commited DBRecord with regex, confirms List.size() and expected name.")
	public void transactionTest_COMMIT() {

		try (DB db = new DBServer(dbFileName)){
			db.beginTransaction();
			db.add(carOwner);
			db.commit();

			List<DBRecord> result = (List<DBRecord>) db.searchWithRegex("Re.*");
			assertEquals(1, result.size());
			CarOwner carOwner = (CarOwner) result.get(0);
			assertEquals("Rezzi Delamdi", carOwner.getName());

		}catch (Exception e) {
			System.out.println("transactionTest_COMMIT():  threw Exception");
		}
	}

	@Test
	@DisplayName("COMMIT with MultiBegin.")
	public void transactionTest_COMMIT_with_MultiBegin() {

		try (DB db = new DBServer(dbFileName)){
			db.beginTransaction();
			db.add(carOwner);
			db.beginTransaction();
			db.commit();

			List<DBRecord> result = (List<DBRecord>) db.searchWithRegex("Re.*");
			assertEquals(1, result.size());
			CarOwner carOwner = (CarOwner) result.get(0);
			assertEquals("Rezzi Delamdi", carOwner.getName());

		}catch (Exception e) {
			System.out.println("transactionTest_COMMIT():  threw Exception");
		}
	}

	@Test
	@DisplayName("Transaction add is started but rolled back, confirms record cannot be retrieved, confirms record is included in debug info.")
	public void transactionTest_ROLLBACK() {
		try(DB db = new DBServer(dbFileName)) {
			db.beginTransaction();
			db.add(carOwner);
			db.rollback();

			List<DBRecord> result = (List<DBRecord>) db.searchWithRegex("Re.*");
			assertEquals(0, result.size());
			List<DebugInfo> info = (List<DebugInfo>) db.getRowsWithDebugInfo();
			assertEquals(1, info.size());


			DebugRowInfo dri = (DebugRowInfo) info.get(0);
			assertFalse(dri.isTemporary());
			assertTrue(dri.isDeleted());

		}catch (Exception e) {
			System.out.println("transactionTest_ROLLBACK():  threw Exception");
		}
	}

	@Test
	@DisplayName("ROLLBACK with MultiBegin.")
	public void transactionTest_ROLLBACK_with_MultiBegin() {

		try (DB db = new DBServer(dbFileName)){
			db.beginTransaction();
			db.add(carOwner);
			db.beginTransaction();
			db.add(carOwnerUpdated);
			db.rollback();

			List<DBRecord> result = (List<DBRecord>) db.searchWithRegex("Re.*");
			assertEquals(0, result.size());
			List<DBRecord> result1 = (List<DBRecord>) db.searchWithRegex("Ra.*");
			assertEquals(0, result1.size());
			List<DebugInfo> info = (List<DebugInfo>) db.getRowsWithDebugInfo();
			assertEquals(2, info.size());


			DebugRowInfo dri = (DebugRowInfo) info.get(0);
			assertFalse(dri.isTemporary());
			assertTrue(dri.isDeleted());

			DebugRowInfo dri1 = (DebugRowInfo) info.get(1);
			assertFalse(dri1.isTemporary());
			assertTrue(dri1.isDeleted());

		}catch (Exception e) {
			System.out.println("transactionTest_COMMIT():  threw Exception");
		}
	}

}