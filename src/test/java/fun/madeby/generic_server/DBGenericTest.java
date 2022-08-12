package fun.madeby.generic_server;

import fun.madeby.Dog;
import fun.madeby.Person;
import fun.madeby.db.DBFactory;
import fun.madeby.db.generic_server.DBGenericServer;
import fun.madeby.db.specific_server.DB;
import fun.madeby.db.specific_server.DBSpecificServer;
import fun.madeby.exceptions.DBException;
import fun.madeby.exceptions.DuplicateNameException;
import fun.madeby.generic.GenericIndex;
import fun.madeby.util.DebugInfo;
import fun.madeby.util.DebugRowInfo;
import org.junit.jupiter.api.*;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
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
	DBGenericServer db;
	private Object dog;
	private Object dogUpdated;
	private Object dogSimilar;
	private Object person;
	private Object personUpdated;
	private Object personSimilar;

	private static final String DOG_SCHEMA = """
					{
			"version": "0.1",
			"indexBy": "pName",
			"schemaFields":[
			{"fieldName":"pName","fieldType":"String"},
			{"fieldName":"age","fieldType":"int"},
			{"fieldName":"owner","fieldType":"String"}
			]
			}""";
	private static final String PERSON_SCHEMA = """
			{
			"version": "0.1",
			"indexBy": "name",
			"schemaFields":[
			{"fieldName":"name","fieldType":"String"},
			{"fieldName":"age","fieldType":"int"},
			{"fieldName":"address","fieldType":"String"},
			{"fieldName":"pet","fieldType":"String"},
			{"fieldName":"description","fieldType":"String"}
			]
			}""";

	private static final String PERSON_SCHEMA_NULL_INDEX_BY = """
			{
			"version": "0.1",
			"schemaFields":[
			{"fieldName":"name","fieldType":"String"},
			{"fieldName":"age","fieldType":"int"},
			{"fieldName":"address","fieldType":"String"},
			{"fieldName":"pet","fieldType":"String"},
			{"fieldName":"description","fieldType":"String"}
			]
			}""";

	private static final String PERSON_SCHEMA_EMPTY_INDEX_BY = """
			{
			"version": "0.1",
			"indexBy": "",
			"schemaFields":[
			{"fieldName":"name","fieldType":"String"},
			{"fieldName":"age","fieldType":"int"},
			{"fieldName":"address","fieldType":"String"},
			{"fieldName":"pet","fieldType":"String"},
			{"fieldName":"description","fieldType":"String"}
			]
			}""";

	@BeforeEach
	public void setUp() {

		//Empties existing file comment out to see
		clearDataInExistingFile();

		// create fun.madeby.Dog test objects:
		dog = new Dog("Fritx",
				3,
				"Rezzi Delamdi");

		dogUpdated = new Dog("Chingu",
				4,
				"Ruzzi Dalemdi");
		dogSimilar = new Dog("F1itx234",
				3,
				"Rezzi Delamdi");

		person = new Person("Rezzi Delamdi",
				41,
				"The world somewhere",
				"Fritx",
				"Uses dog as excuse for going outside and socialising");
		personUpdated = new Person("Ruzzi Dalamdi",
				44,
				"The horld hemewhere",
				"Chingu",
				"Eats pie, seriously");
		personSimilar = new Person("Rezzu Delamdi",
				20,
				"The chig hackle",
				"F1itx234",
				"Pret a then some");
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
	@DisplayName("testPopulateIndex totalRows 2")
	void testPopulateIndex() throws DuplicateNameException, DBException, FileNotFoundException {
		try (DBGenericServer db = DBFactory.getGenericDB(dbFileName, PERSON_SCHEMA, Person.class)) {
			db.beginTransaction();
			db.add(person);
			db.add(personSimilar);
			db.commit();
			assertEquals(2, GenericIndex.getInstance().getTotalNumberOfRows());

		} catch (Exception e) {
			Assertions.fail();
		}

		// reopen db
		try (DBGenericServer db = DBFactory.getGenericDB(dbFileName, PERSON_SCHEMA, Person.class)) {
			assertEquals(2, GenericIndex.getInstance().getTotalNumberOfRows());
			Long recNumber = db.getTotalRecordAmount();
			Assertions.assertEquals(2,recNumber);
		} catch (Exception e) {
			Assertions.fail();
		}
	}



	@Test
	@DisplayName("@testRollback_Delete(): totalRows added 1-> deleted 1 -> rolled back -> 1row")
	void testRollback_Delete()  {
		try (DBGenericServer db = DBFactory.getGenericDB(dbFileName, PERSON_SCHEMA, Person.class)) {
			Long recNumber = 0L;

			db.beginTransaction();
			db.add(person);
			db.commit();
			assertEquals(1, GenericIndex.getInstance().getTotalNumberOfRows());

			// delete first record
			db.beginTransaction();
			db.delete(0L);
			db.rollback();

			ArrayList<DebugInfo> debugList = (ArrayList<DebugInfo>) db.getRowsWithDebugInfo();
			Assertions.assertEquals(1, debugList.size());

		} catch (Exception e) {
			Assertions.fail();
		}

	}

	@Test
	@DisplayName("testRollback_Add totalRows added 3-> rolled back -> 0 total no rows && 1 rows with debug info")
	void testRollback_Add()  {
		try (DBGenericServer db = DBFactory.getGenericDB(dbFileName, PERSON_SCHEMA, Person.class)) {
			Long recNumber = 0L;

			db.beginTransaction();
			db.add(person);
			db.add(personUpdated);
			db.add(personSimilar);
			db.rollback();
			assertEquals(0, GenericIndex.getInstance().getTotalNumberOfRows());


			ArrayList<DebugInfo> debugList = (ArrayList<DebugInfo>) db.getRowsWithDebugInfo();
			Assertions.assertEquals(3, debugList.size());

		} catch (Exception e) {
			Assertions.fail();
		}

	}


	@Test
	@DisplayName("@getRowsWithDebugInfoTest()")
	void getRowsWithDebugInfoTest() throws DuplicateNameException, DBException {
		try (DBGenericServer db = new DBGenericServer(dbFileName, PERSON_SCHEMA, Person.class)) {
			db.beginTransaction();
			db.add(person);
			db.commit();

			ArrayList<DebugInfo> debugList1 = (ArrayList<DebugInfo>) db.getRowsWithDebugInfo();
			Assertions.assertEquals(1, debugList1.size());
		} catch (IOException e) {
			System.out.println("searchTest: threw Exception");
			e.printStackTrace();
		}
	}



	@Test
	@DisplayName("testNullIndexInfoInSchema() ")
	void testNullIndexInfoInSchema()   {
		try  {

			DBException dbException = Assertions.assertThrows(DBException.class, () -> {
				DBGenericServer db = DBFactory.getGenericDB(dbFileName, PERSON_SCHEMA_NULL_INDEX_BY, Person.class);
			});

			Assertions.assertEquals(dbException.getMessage(), "@GenericIndex/initialiseIndexSchema(Schema) -> Schema's indexBy field null");

		} catch (Exception e) {
			Assertions.fail();
		}

	}

	@Test
	@DisplayName("testEmptyIndexInfoInSchema() ")
	void testEmptyIndexInfoInSchema()  {
		try  {

			DBException dbException = Assertions.assertThrows(DBException.class, () -> {
				DBGenericServer db = DBFactory.getGenericDB(dbFileName, PERSON_SCHEMA_EMPTY_INDEX_BY, Person.class);
			});

			Assertions.assertEquals(dbException.getMessage(), "@GenericIndex/initialiseIndexSchema(Schema) -> Schema's indexBy field equals\"\"");

		} catch (Exception e) {
			Assertions.fail();
		}

	}


	@Test
	@DisplayName("testDBVersion(): 0.1")
	void testDBVersion() throws DBException {
		try (DBGenericServer db = DBFactory.getGenericDB(dbFileName, PERSON_SCHEMA, Person.class)) {
			String version = db.getDBVersion();
			Assertions.assertEquals("0.1", version);

		} catch (IOException e) {
			Assertions.fail();
		}
	}

	@Test
	@DisplayName("testLevenshtein5_2(): 5 tolerance return 2")
	void testLevenshtein5_2() throws DuplicateNameException, DBException {
		try (DBGenericServer db = DBFactory.getGenericDB(dbFileName, DOG_SCHEMA, Dog.class)) {
			db.beginTransaction();
			db.add(dog);
			db.add(dogSimilar); //"xtirF"
			db.commit();
			assertEquals(2, GenericIndex.getInstance().getTotalNumberOfRows());
			ArrayList<Object> returnedMatchesWithinTolerance = (ArrayList<Object>) db.searchWithLevenshtein("Fritx", 5);
			assertEquals(2, returnedMatchesWithinTolerance.size());
		} catch (IOException e) {
			Assertions.fail();
		}
	}

	@Test
	@DisplayName("testLevenshtein4_1():  4 tolerance return 1")
	void testLevenshtein4_1() throws DuplicateNameException, DBException {
		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)) {
			db.beginTransaction();
			db.add(dog);
			db.add(dogUpdated); //"xtirF"
			db.commit();
			assertEquals(2, GenericIndex.getInstance().getTotalNumberOfRows());
			ArrayList<Object> returnedMatchesWithinTolerance = (ArrayList<Object>) db.searchWithLevenshtein("Fritx", 4);
			assertEquals(1, returnedMatchesWithinTolerance.size());
		} catch (IOException e) {
			Assertions.fail();
		}

	}

	@Test
	@DisplayName("testRegEx(): 'F.*'")
	void testRegEx() throws DuplicateNameException, DBException {
		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)) {
			db.beginTransaction();
			db.add(dog);
			db.add(dogSimilar);
			db.commit();
			assertEquals(2, GenericIndex.getInstance().getTotalNumberOfRows());
			ArrayList<Object> returnedMatchesRegex = (ArrayList<Object>) db.searchWithRegex("F.*");
			assertEquals(2, returnedMatchesRegex.size());
		} catch (IOException e) {
			Assertions.fail();
		}
	}

	@Test
	@DisplayName("testRegEx2():  'Fr.*'")
	void testRegEx2() throws DuplicateNameException, DBException {
		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)) {
			db.beginTransaction();
			db.add(dog);
			db.add(dogUpdated);
			db.commit();
			assertEquals(2, GenericIndex.getInstance().getTotalNumberOfRows());
			ArrayList<Object> returnedMatchesRegex = (ArrayList<Object>) db.searchWithRegex("Fr.*");
			assertEquals(1, returnedMatchesRegex.size());
		} catch (IOException e) {
			Assertions.fail();
		}
	}


	@Test
	@DisplayName("addTest(): DBGenericServer Add, via FileHandler test: 1 == OK")
	void addTest() throws DuplicateNameException, DBException {
		try (DBGenericServer db = DBFactory.getGenericDB(dbFileName, DOG_SCHEMA, Dog.class)) {
			db.beginTransaction();
			db.add(dog);
			db.commit();
			assertEquals(1, GenericIndex.getInstance().getTotalNumberOfRows());
		} catch (IOException e) {
			Assertions.fail();
		}
	}

	@Test
	@DisplayName("addDuplicateTest(): DBGenericServer AddDuplicateTest throws DuplicateNameException == OK")
	void addDuplicateTest() throws DBException {
		try (DBGenericServer db = DBFactory.getGenericDB(dbFileName, DOG_SCHEMA, Dog.class)) {

			DuplicateNameException thrown = Assertions.assertThrows(DuplicateNameException.class, () -> {
				db.beginTransaction();
				db.add(dog);
				db.commit();
				db.beginTransaction();
				db.add(dog);
				db.commit();
			});

		} catch (IOException e) {
			Assertions.fail();
		}
	}

/*	@Test //todo see issue 34
	@DisplayName("deleteTest(): DBSpecificServer Add then delete separate transactions")
	void deleteTest() {
		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, fun.madeby.Dog.class)){
			db.beginTransaction();
			db.add(dog);
			db.commit();
			assertEquals(1, GenericIndex.getTotalNumberOfRows());
			db.beginTransaction();
			db.delete(0L);
			db.commit();
			assertEquals(0, GenericIndex.getTotalNumberOfRows());
		}catch(IOException e) {
			System.out.println("deleteTest: threw Exception");
			e.printStackTrace();
		}
	}*/


	@Test
	@DisplayName("searchTest(): DBSpecificServer search, then compare retrieved")
	void searchTest() throws DuplicateNameException, DBException {
		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)) {
			db.beginTransaction();
			db.add(dog);
			db.commit();
			assertEquals(1, GenericIndex.getInstance().getTotalNumberOfRows());
			Dog retrieved = (Dog) db.search("Fritx");
			assertNotNull(retrieved);
			assertEquals("Fritx", retrieved.pName);
			assertEquals(3, retrieved.age);
			assertEquals("Rezzi Delamdi", retrieved.owner);

		} catch (IOException e) {
			Assertions.fail();
		}
	}


	@Test
	@DisplayName("DBSpecificServer readTest : 1 == OK and then test equality of each field")
		//shows on fail
	void readTest() throws DuplicateNameException, DBException {
		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)) {
			db.beginTransaction();
			db.add(dog);
			db.commit();
			assertEquals(GenericIndex.getInstance().getTotalNumberOfRows(), 1);
			Dog readdog = (Dog) db.read(GenericIndex.getInstance().getRowNumberByName("Fritx"));
			assertNotNull(readdog);
			assertEquals("Fritx", readdog.pName);
			assertEquals(3, readdog.age);
			assertEquals("Rezzi Delamdi", readdog.owner);
		} catch (IOException e) {
			Assertions.fail();
		}
	}

	@Test
	@DisplayName("UpdateByRowTest(): Sets existing row 0L to deleted in .db file, then creates new row with modified data")
	void updateByRowTest() throws DuplicateNameException, DBException {
		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)) {
			// normal
			db.beginTransaction();
			db.add(dog);
			db.commit();
			assertEquals(1, GenericIndex.getInstance().getTotalNumberOfRows());
			db.beginTransaction();
			db.update(0L, dogUpdated);
			db.commit();
			assertEquals(1, GenericIndex.getInstance().getTotalNumberOfRows());
			//db.defragmentDatabase(); // todo defrag breaks this test (adding as bug @issue 35) retrieved == null
			//Object retrieved = db.read(GenericIndex.getInstance().getRowNumberByName("Razzgu Dulemdi")); // worked fine
			Dog retrieved = (Dog) db.read(1L);
			assert retrieved != null;
			assertEquals("Chingu", retrieved.pName);
			assertEquals(4, retrieved.age);
			assertEquals("Ruzzi Dalemdi", retrieved.owner);

		} catch (IOException e) {
			Assertions.fail();
		}
	}


	@Test // todo see issue 34
	@DisplayName("UpdateByNameTest: Sets existing row 0L (found by name) to deleted in .db file, then creates new row with modified data")
	void updateByNameTest() throws DuplicateNameException, DBException {
		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)) {
			db.beginTransaction();
			db.add(dog);
			db.commit();
			assertEquals(1, GenericIndex.getInstance().getTotalNumberOfRows());
			Long retrievedRowNum = GenericIndex.getInstance().getRowNumberByName("Fritx");
			System.out.println("Retrieved row for 'Fritx' " + retrievedRowNum);
			assertTrue(retrievedRowNum >= 0);
			db.beginTransaction();
			db.update("Fritx", dogUpdated);
			db.commit();
			assertEquals(1, GenericIndex.getInstance().getTotalNumberOfRows());
			Dog retrieved = (Dog) db.read(GenericIndex.getInstance().getRowNumberByName("Chingu"));
			if (retrieved != null) {
				assertEquals("Chingu", retrieved.pName);
				assertEquals(4, retrieved.age);
				assertEquals("Ruzzi Dalemdi", retrieved.owner);
			}
		} catch (IOException e) {
			Assertions.fail();
		}
	}


	@Test
	@DisplayName("Searches for commited Object with regex, confirms List.size() and expected name.")
	public void transactionTest_COMMIT() {

		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)) {
			db.beginTransaction();
			db.add(dog);
			db.commit();

			List<Object> result = (List<Object>) db.searchWithRegex("Fr.*");
			assertEquals(1, result.size());
			Dog dog = (Dog) result.get(0);
			assertEquals("Fritx", dog.pName);

		} catch (Exception e) {
			Assertions.fail();
		}
	}

	@Test
	@DisplayName("COMMIT with MultiBegin.")
	public void transactionTest_COMMIT_with_MultiBegin() {

		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)) {
			db.beginTransaction();
			db.add(dog);
			db.beginTransaction();
			db.commit();

			List<Object> result = (List<Object>) db.searchWithRegex("Fr.*");
			assertEquals(1, result.size());
			Dog dog = (Dog) result.get(0);
			assertEquals("Fritx", dog.pName);

		} catch (Exception e) {
			Assertions.fail();
		}
	}

	@Test
	@DisplayName("Transaction add is started but rolled back, confirms record cannot be retrieved, confirms record is included in debug info.")
	public void transactionTest_ROLLBACK() {
		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)) {
			db.beginTransaction();
			db.add(dog);
			db.rollback();

			List<Object> result = (List<Object>) db.searchWithRegex("Fr.*");
			assertEquals(0, result.size());
			List<DebugInfo> info = (List<DebugInfo>) db.getRowsWithDebugInfo();
			assertEquals(1, info.size());


			DebugRowInfo dri = (DebugRowInfo) info.get(0);
			assertFalse(dri.isTemporary());
			assertTrue(dri.isDeleted());

		} catch (Exception e) {
			e.printStackTrace();
			Assertions.fail();
		}
	}

	@Test
	@DisplayName("ROLLBACK with MultiBegin.")
	public void transactionTest_ROLLBACK_with_MultiBegin() {

		try (DBGenericServer db = new DBGenericServer(dbFileName, DOG_SCHEMA, Dog.class)) {
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

		} catch (Exception e) {
			Assertions.fail();
		}
	}

}