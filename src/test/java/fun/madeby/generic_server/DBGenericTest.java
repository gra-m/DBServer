package fun.madeby.generic_server;

import fun.madeby.Dog;
import fun.madeby.db.DBFactory;
import fun.madeby.db.generic_server.DBGenericServer;
import fun.madeby.specific.Index;
import fun.madeby.util.DebugInfo;
import fun.madeby.util.DebugRowInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
class DBGenericTest {
	private final String dbFileName = "testGeneric.db";
	private Object dog;
	private Object dogUpdated;
	private Object dogSimilar;
	private Index index;
	private static final String DOG_SCHEMA = """
  			{
			"version": "0.1",
			"schemaFields":[
			{"fieldName":"pName","fieldType":"String"},
			{"fieldName":"age","fieldType":"int"},
			{"fieldName":"owner","fieldType":"String"}
			]
			}""";


	@BeforeEach
	public void setUp() {

		// get singleton
		index = Index.getInstance();

		//Empties existing file comment out to see
		clearDataInExistingFile();

		// create fun.madeby.Dog test objects:
		dog = new Dog("Fritx",
				3,
				"Rezzi Delamdi");

		dogUpdated = new Dog("Chingu",
				4,
				"Ruzzi Dalemdi");
		dogSimilar = new Dog("xtirF",
				3,
				"Rezzi Delamdi");
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
		try (DBGenericServer db = DBFactory.getGenericDB(dbFileName, DOG_SCHEMA, Dog.class)){
			db.beginTransaction();
			db.add(dog);
			db.add(dogSimilar); //"xtirF"
			db.commit();
			assertEquals(2, index.getTotalNumberOfRows());
			ArrayList<Object> returnedMatchesWithinTolerance = (ArrayList<Object>) db.searchWithLevenshtein("Fritx", 5);
			assertEquals(2, returnedMatchesWithinTolerance.size());
		}catch(IOException e) {
			System.out.println("5Tolerance Levenshtein: threw Exception");
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("testLevenshtein4_1():  4 tolerance return 1")
	void testLevenshtein4_1() {
		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)){
			db.beginTransaction();
			db.add(dog);
			db.add(dogUpdated); //"xtirF"
			db.commit();
			assertEquals(2, index.getTotalNumberOfRows());
			ArrayList<Object> returnedMatchesWithinTolerance = (ArrayList<Object>) db.searchWithLevenshtein("Fritx", 4);
			assertEquals(1, returnedMatchesWithinTolerance.size());
		}catch(IOException e) {
			System.out.println("5Tolerance Levenshtein: threw Exception");
			e.printStackTrace();
		}

	}

	@Test
	@DisplayName("testRegEx(): 'R.*'")
	void testRegEx() {
		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)){
			db.beginTransaction();
			db.add(dog);
			db.add(dogUpdated);
			db.commit();
			assertEquals(2, index.getTotalNumberOfRows());
			ArrayList<Object> returnedMatchesRegex = (ArrayList<Object>) db.searchWithRegex("R.*");
			assertEquals(2, returnedMatchesRegex.size());
		}catch(IOException e) {
			System.out.println("5Tolerance Levenshtein: threw Exception");
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("testRegEx2():  'Re.*'")
	void testRegEx2() {
		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)){
			db.beginTransaction();
			db.add(dog);
			db.add(dogUpdated);
			db.commit();
			assertEquals(2, index.getTotalNumberOfRows());
			ArrayList<Object> returnedMatchesRegex = (ArrayList<Object>) db.searchWithRegex("Re.*");
			assertEquals(1, returnedMatchesRegex.size());
		}catch(IOException e) {
			System.out.println("5Tolerance Levenshtein: threw Exception");
			e.printStackTrace();
		}
	}


	@Test
	@DisplayName("addTest(): DBGenericServer Add, via FileHandler test: 1 == OK")
	void addTest(){
		try (DBGenericServer db = DBFactory.getGenericDB(dbFileName, DOG_SCHEMA, Dog.class)){
			db.beginTransaction();
			db.add(dog);
			db.commit();
			assertEquals(1, index.getTotalNumberOfRows());
		}catch(IOException e) {
			System.out.println("addTest: add threw Exception");
		}
	}

/*	@Test //todo see issue 34
	@DisplayName("deleteTest(): DBServer Add then delete separate transactions")
	void deleteTest() {
		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, fun.madeby.Dog.class)){
			db.beginTransaction();
			db.add(dog);
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
		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)){
			db.beginTransaction();
			db.add(dog);
			db.commit();
			assertEquals(1, index.getTotalNumberOfRows());
			Dog retrieved = (Dog) db.search("Fritx");
			assertNotNull(retrieved);
			assertEquals("Fritx", retrieved.pName);
			assertEquals(3, retrieved.age);
			assertEquals("Rezzi Delamdi", retrieved.owner);

		}catch(IOException e) {
			System.out.println("searchTest: threw Exception");
			e.printStackTrace();
		}
	}



	@Test
	@DisplayName("DBServer readTest : 1 == OK and then test equality of each field") //shows on fail
	void readTest(){
		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)){
			db.beginTransaction();
			db.add(dog);
			db.commit();
			assertEquals(index.getTotalNumberOfRows(), 1);
			Dog readdog = (Dog) db.read(Index.getInstance().getRowNumberByName("Fritx"));
			assertNotNull(readdog);
			assertEquals("Fritx", readdog.pName);
			assertEquals(3, readdog.age);
			assertEquals("Rezzi Delamdi", readdog.owner);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("UpdateByRowTest(): Sets existing row 0L to deleted in .db file, then creates new row with modified data")
	void updateByRowTest() {
		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)){
			// normal
			db.beginTransaction();
			db.add(dog);
			db.commit();
			assertEquals(1, index.getTotalNumberOfRows());
			db.beginTransaction();
			db.update(0L, dogUpdated );
			db.commit();
			assertEquals(1, index.getTotalNumberOfRows());
			//db.defragmentDatabase(); // todo defrag breaks this test (adding as bug @issue 35) retrieved == null
			//Object retrieved = db.read(Index.getInstance().getRowNumberByName("Razzgu Dulemdi")); // worked fine
			Dog retrieved =  (Dog) db.read(1L);
			assert retrieved != null;
			assertEquals("Chingu", retrieved.pName);
			assertEquals(4, retrieved.age);
			assertEquals("Ruzzi Dalemdi", retrieved.owner);

		}catch(IOException e) {
			System.out.println("updateByRowTest:  threw Exception");
			e.printStackTrace();
		}
	}



	@Test // todo see issue 34
	@DisplayName("UpdateByNameTest: Sets existing row 0L (found by name) to deleted in .db file, then creates new row with modified data")
	void updateByNameTest() {
		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)){
			db.beginTransaction();
			db.add(dog);
			db.commit();
			assertEquals(1, index.getTotalNumberOfRows());
			Long retrievedRowNum = index.getRowNumberByName("Fritx");
			System.out.println("Retrieved row for 'Rezzi Delamdi' " + retrievedRowNum);
			db.beginTransaction();
			db.update("Rezzi Delamdi", dogUpdated );
			db.commit();
			assertEquals(1, index.getTotalNumberOfRows());
			Dog retrieved = (Dog) db.read(index.getRowNumberByName("Razzgu Dulemdi"));
			if (retrieved != null) {
				assertEquals("Chingu", retrieved.pName);
				assertEquals(4, retrieved.age);
				assertEquals("Ruzzi Dalemdi", retrieved.owner);
			}
		} catch (IOException e) {
			System.out.println("updateByNameTest:  threw Exception");
		}
	}



	@Test
	@DisplayName("Searches for commited Object with regex, confirms List.size() and expected name.")
	public void transactionTest_COMMIT() {

		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)){
			db.beginTransaction();
			db.add(dog);
			db.commit();

			List<Object> result = (List<Object>) db.searchWithRegex("Fr.*");
			assertEquals(1, result.size());
			Dog dog = (Dog) result.get(0);
			assertEquals("Fritx", dog.pName);

		}catch (Exception e) {
			System.out.println("transactionTest_COMMIT():  threw Exception");
		}
	}

	@Test
	@DisplayName("COMMIT with MultiBegin.")
	public void transactionTest_COMMIT_with_MultiBegin() {

		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)){
			db.beginTransaction();
			db.add(dog);
			db.beginTransaction();
			db.commit();

			List<Object> result = (List<Object>) db.searchWithRegex("Fr.*");
			assertEquals(1, result.size());
			Dog dog = (Dog) result.get(0);
			assertEquals("Fritx", dog.pName);

		}catch (Exception e) {
			System.out.println("transactionTest_COMMIT():  threw Exception");
		}
	}

	@Test
	@DisplayName("Transaction add is started but rolled back, confirms record cannot be retrieved, confirms record is included in debug info.")
	public void transactionTest_ROLLBACK() {
		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)){
			db.beginTransaction();
			db.add(dog);
			db.rollback();

			List<Object> result = (List<Object>) db.searchWithRegex("Re.*");
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

		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)){
			db.beginTransaction();
			db.add(dog);
			db.beginTransaction();
			db.add(dogUpdated);
			db.rollback();

			List<Object> result = (List<Object>) db.searchWithRegex("Re.*");
			assertEquals(0, result.size());
			List<Object> result1 = (List<Object>) db.searchWithRegex("Ra.*");
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