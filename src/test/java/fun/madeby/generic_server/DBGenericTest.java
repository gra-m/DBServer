package fun.madeby.generic_server;

import fun.madeby.Dog;
import fun.madeby.Person;
import fun.madeby.db.DBFactory;
import fun.madeby.db.DbTable;
import fun.madeby.db.Table;
import fun.madeby.db.generic_server.DBGenericServer;
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
	// table names passed without .filetype:
	private final String dogTableName = "testDog";
	private final String personTableName = "testPerson";
	private final String fileType = ".db";
	private Object dog;
	private Object dogUpdated;
	private Object dogSimilar;
	private Object person;
	private Object personUpdated;
	private Object personSimilar;
	private DBGenericServer db;
	private Table tableInUse;
	private GenericIndex index;

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
		try (BufferedWriter ignored = Files.newBufferedWriter(Path.of("./" + dogTableName + fileType),
				StandardOpenOption.TRUNCATE_EXISTING)) {
			// ignored
		} catch (IOException e) {
			e.printStackTrace();
		}
		try (BufferedWriter ignored = Files.newBufferedWriter(Path.of("./" + personTableName + fileType),
			  StandardOpenOption.TRUNCATE_EXISTING)) {
			// ignored
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("testPopulateIndex totalRows 2")
	void testPopulateIndex() throws DuplicateNameException, DBException, FileNotFoundException {
		try (DBGenericServer db = DBFactory.getGenericDB()) {

			tableInUse = db.useTable(personTableName, PERSON_SCHEMA, Person.class);
			index = db.getIndex(personTableName + fileType);

				tableInUse.beginTransaction();
				tableInUse.add(person);
				tableInUse.add(personSimilar);
				tableInUse.commit();
				assertEquals(2, index.getTotalNumberOfRows());
				// can close table but closing DBGenericServer loses tablePool
				db.suspendCurrentTable();


			tableInUse = db.useTable(personTableName, PERSON_SCHEMA, Person.class);

			if(index != null) {
				assertEquals(2, index.getTotalNumberOfRows());
			}
			Long recNumber = tableInUse.getTotalRecordAmount();
			Assertions.assertEquals(2, recNumber);
			db.dropCurrentTable();

		} catch (Exception e) {
		Assertions.fail();
	}
	}

	@Test
	@DisplayName("@testRollback_Delete(): totalRows added 1-> deleted 1 -> rolled back -> 1row")
	void testRollback_Delete()  {
		try (DBGenericServer db = DBFactory.getGenericDB()) {
			Long recNumber = 0L;
			tableInUse = db.useTable(personTableName, PERSON_SCHEMA, Person.class);
			index = db.getIndex(personTableName + fileType);

			tableInUse.beginTransaction();
			tableInUse.add(person);
			tableInUse.commit();

			assertEquals(1, index.getTotalNumberOfRows());

			// delete first record
			tableInUse.beginTransaction();
			tableInUse.delete(0L);
			tableInUse.rollback();

			ArrayList<DebugInfo> debugList = (ArrayList<DebugInfo>) tableInUse.getRowsWithDebugInfo();
			Assertions.assertEquals(1, debugList.size());

		} catch (Exception e) {
			Assertions.fail();
		}

	}

	@Test
	@DisplayName("testRollback_Add totalRows added 3-> rolled back -> 0 total no rows && 1 rows with debug info")
	void testRollback_Add()  {
		try (DBGenericServer db = DBFactory.getGenericDB()) {
			Long recNumber = 0L;
			tableInUse = db.useTable(personTableName, PERSON_SCHEMA, Person.class);
			index = db.getIndex(personTableName + fileType);
			
			tableInUse.beginTransaction();
			tableInUse.add(person);
			tableInUse.add(personUpdated);
			tableInUse.add(personSimilar);
			tableInUse.rollback();
			assertEquals(0, index.getTotalNumberOfRows());

			ArrayList<DebugInfo> debugList = (ArrayList<DebugInfo>) tableInUse.getRowsWithDebugInfo();
			Assertions.assertEquals(3, debugList.size());

		} catch (Exception e) {
			Assertions.fail();
		}
	}

	@Test
	@DisplayName("@getRowsWithDebugInfoTest()")
	void getRowsWithDebugInfoTest() throws DuplicateNameException, DBException {
		try (DBGenericServer db = new DBGenericServer()) {

			tableInUse = db.useTable(personTableName, PERSON_SCHEMA, Person.class);
			tableInUse.beginTransaction();
			tableInUse.add(person);
			tableInUse.commit();

			ArrayList<DebugInfo> debugList1 = (ArrayList<DebugInfo>) tableInUse.getRowsWithDebugInfo();
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
				DBGenericServer db = DBFactory.getGenericDB();
				db.useTable(personTableName, PERSON_SCHEMA_NULL_INDEX_BY, Person.class);
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
				DBGenericServer db = DBFactory.getGenericDB();
				db.useTable(personTableName, PERSON_SCHEMA_EMPTY_INDEX_BY, Person.class);
			});

			Assertions.assertEquals(dbException.getMessage(), "@GenericIndex/initialiseIndexSchema(Schema) -> Schema's indexBy field equals\"\"");

		} catch (Exception e) {
			Assertions.fail();
		}

	}

	@Test
	@DisplayName("testDBVersion(): 0.1")
	void testDBVersion() throws DBException {
		try (DBGenericServer db = DBFactory.getGenericDB()) {
			tableInUse = db.useTable(personTableName, PERSON_SCHEMA, Person.class);
			DbTable dbTableMethod = (DbTable)  tableInUse; // todo poss add getDBVersion to Table API

			String version = dbTableMethod.getDBVersion();
			Assertions.assertEquals("0.1", version);

		} catch (IOException e) {
			Assertions.fail();
		}
	}


	@Test
	@DisplayName("testLevenshtein5_2(): 5 tolerance return 2")
	void testLevenshtein5_2() throws DuplicateNameException, DBException {
		try (DBGenericServer db = DBFactory.getGenericDB()) {
			tableInUse = db.useTable(dogTableName, DOG_SCHEMA, Dog.class);
			index = db.getIndex(dogTableName + fileType);

			tableInUse.beginTransaction();
			tableInUse.add(dog);
			tableInUse.add(dogSimilar); //"xtirF"
			tableInUse.commit();
			assertEquals(2, index.getTotalNumberOfRows());
			ArrayList<Object> returnedMatchesWithinTolerance = (ArrayList<Object>) tableInUse.searchWithLevenshtein("Fritx", 5);
			assertEquals(2, returnedMatchesWithinTolerance.size());
		} catch (IOException e) {
			Assertions.fail();
		}
	}
	@Test
	@DisplayName("testLevenshtein4_1():  4 tolerance return 1")
	void testLevenshtein4_1() throws DuplicateNameException, DBException {
		try (DBGenericServer db = new DBGenericServer()) {
			tableInUse = db.useTable(dogTableName, DOG_SCHEMA, Dog.class);
			index = db.getIndex(dogTableName + fileType);

			tableInUse.beginTransaction();
			tableInUse.add(dog);
			tableInUse.add(dogUpdated); //"xtirF"
			tableInUse.commit();
			assertEquals(2, index.getTotalNumberOfRows());
			ArrayList<Object> returnedMatchesWithinTolerance = (ArrayList<Object>) tableInUse.searchWithLevenshtein("Fritx", 4);
			assertEquals(1, returnedMatchesWithinTolerance.size());
		} catch (IOException e) {
			Assertions.fail();
		}

	}

	@Test
	@DisplayName("testRegEx(): 'F.*'")
	void testRegEx() throws DuplicateNameException, DBException {
		try (DBGenericServer db = new DBGenericServer()) {
			tableInUse = db.useTable(dogTableName, DOG_SCHEMA, Dog.class);
			index = db.getIndex(dogTableName + fileType);

			tableInUse.beginTransaction();
			tableInUse.add(dog);
			tableInUse.add(dogSimilar);
			tableInUse.commit();
			assertEquals(2, index.getTotalNumberOfRows());
			ArrayList<Object> returnedMatchesRegex = (ArrayList<Object>) tableInUse.searchWithRegex("F.*");
			assertEquals(2, returnedMatchesRegex.size());
		} catch (IOException e) {
			Assertions.fail();
		}
	}

	@Test
	@DisplayName("testRegEx2():  'Fr.*'")
	void testRegEx2() throws DuplicateNameException, DBException {
		try (DBGenericServer db = new DBGenericServer()) {

			tableInUse = db.useTable(dogTableName, DOG_SCHEMA, Dog.class);
			index = db.getIndex(dogTableName + fileType);

			tableInUse.beginTransaction();
			tableInUse.add(dog);
			tableInUse.add(dogUpdated);
			tableInUse.commit();
			assertEquals(2, index.getTotalNumberOfRows());
			ArrayList<Object> returnedMatchesRegex = (ArrayList<Object>) tableInUse.searchWithRegex("Fr.*");
			assertEquals(1, returnedMatchesRegex.size());
		} catch (IOException e) {
			Assertions.fail();
		}
	}

	@Test
	@DisplayName("addTest(): DBGenericServer Add, via FileHandler test: 1 == OK")
	void addTest() throws DuplicateNameException, DBException {
		try (DBGenericServer db = DBFactory.getGenericDB()) {
			tableInUse = db.useTable(dogTableName, DOG_SCHEMA, Dog.class);
			index = db.getIndex(dogTableName + fileType);

			tableInUse.beginTransaction();
			tableInUse.add(dog);
			tableInUse.commit();
			assertEquals(1, index.getTotalNumberOfRows());
		} catch (IOException e) {
			Assertions.fail();
		}
	}

	@Test
	@DisplayName("addDuplicateTest(): DBGenericServer AddDuplicateTest throws DuplicateNameException == OK")
	void addDuplicateTest() throws DBException {
		try (DBGenericServer db = DBFactory.getGenericDB()) {
			tableInUse = db.useTable(dogTableName, DOG_SCHEMA, Dog.class);
			index = db.getIndex(dogTableName + fileType);

			DuplicateNameException thrown = Assertions.assertThrows(DuplicateNameException.class, () -> {
				tableInUse.beginTransaction();
				tableInUse.add(dog);
				tableInUse.commit();
				tableInUse.beginTransaction();
				tableInUse.add(dog);
				tableInUse.commit();
			});

		} catch (IOException e) {
			Assertions.fail();
		}
	}


	@Test
	@DisplayName("searchTest(): DBSpecificServer search, then compare retrieved")
	void searchTest() throws DuplicateNameException, DBException {
		try (DBGenericServer db = new DBGenericServer()) {
			tableInUse = db.useTable(dogTableName, DOG_SCHEMA, Dog.class);
			index = db.getIndex(dogTableName + fileType);

			tableInUse.beginTransaction();
			tableInUse.add(dog);
			tableInUse.commit();
			assertEquals(1, index.getTotalNumberOfRows());
			Dog retrieved = (Dog) tableInUse.search("Fritx");
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
		try (DBGenericServer db = new DBGenericServer()) {
			tableInUse = db.useTable(dogTableName, DOG_SCHEMA, Dog.class);
			index = db.getIndex(dogTableName + fileType);

			tableInUse.beginTransaction();
			tableInUse.add(dog);
			tableInUse.commit();
			assertEquals(index.getTotalNumberOfRows(), 1);
			Dog readdog = (Dog) tableInUse.read(index.getRowNumberByName("Fritx"));
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
		try (DBGenericServer db = new DBGenericServer()) {
			tableInUse = db.useTable(dogTableName, DOG_SCHEMA, Dog.class);
			index = db.getIndex(dogTableName + fileType);

			// normal
			tableInUse.beginTransaction();
			tableInUse.add(dog); // written @0
			tableInUse.commit();
			assertEquals(1, index.getTotalNumberOfRows());
			tableInUse.beginTransaction();
			tableInUse.update(0L, dogUpdated);
			tableInUse.commit();
			assertEquals(1, index.getTotalNumberOfRows());
			long retrievedRowNumByName = index.getRowNumberByName("Chingu");
			Assertions.assertEquals(1, retrievedRowNumByName);
			Dog retrieved = (Dog) tableInUse.read(retrievedRowNumByName);
			assert retrieved != null;
			assertEquals("Chingu", retrieved.pName);
			assertEquals(4, retrieved.age);
			assertEquals("Ruzzi Dalemdi", retrieved.owner);

		} catch (IOException e) {
			Assertions.fail();
		}
	}


	@Test
	@DisplayName("UpdateByNameTest: Sets existing row 0L (found by name) to deleted in .db file, then creates new row with modified data")
	void updateByNameTest() throws DuplicateNameException, DBException {
		try (DBGenericServer db = new DBGenericServer()) {
			tableInUse = db.useTable(dogTableName, DOG_SCHEMA, Dog.class);
			index = db.getIndex(dogTableName + fileType);


			tableInUse.beginTransaction();
			tableInUse.add(dog);
			tableInUse.commit();
			assertEquals(1, index.getTotalNumberOfRows());
			Long retrievedRowNum = index.getRowNumberByName("Fritx");
			System.out.println("Retrieved row for 'Fritx' " + retrievedRowNum);
			assertTrue(retrievedRowNum >= 0);
			tableInUse.beginTransaction();
			tableInUse.update("Fritx", dogUpdated);
			tableInUse.commit();
			assertEquals(1, index.getTotalNumberOfRows());
			Dog retrieved = (Dog) tableInUse.read(index.getRowNumberByName("Chingu"));
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

		try (DBGenericServer db = new DBGenericServer()) {
			tableInUse = db.useTable(dogTableName, DOG_SCHEMA, Dog.class);
			index = db.getIndex(dogTableName + fileType);

			tableInUse.beginTransaction();
			tableInUse.add(dog);
			tableInUse.commit();

			List<Object> result = (List<Object>) tableInUse.searchWithRegex("Fr.*");
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

		try (DBGenericServer db = new DBGenericServer()) {
			tableInUse = db.useTable(dogTableName, DOG_SCHEMA, Dog.class);
			index = db.getIndex(dogTableName + fileType);

			tableInUse.beginTransaction();
			tableInUse.add(dog);
			tableInUse.beginTransaction();
			tableInUse.commit();

			List<Object> result = (List<Object>) tableInUse.searchWithRegex("Fr.*");
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
		try (DBGenericServer db = new DBGenericServer()) {
			tableInUse = db.useTable(dogTableName, DOG_SCHEMA, Dog.class);
			index = db.getIndex(dogTableName + fileType);

			tableInUse.beginTransaction();
			tableInUse.add(dog);
			tableInUse.rollback();

			List<Object> result = (List<Object>) tableInUse.searchWithRegex("Fr.*");
			assertEquals(0, result.size());
			List<DebugInfo> info = (List<DebugInfo>) tableInUse.getRowsWithDebugInfo();
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

		try (DBGenericServer db = new DBGenericServer()) {
			tableInUse = db.useTable(dogTableName, DOG_SCHEMA, Dog.class);
			index = db.getIndex(dogTableName + fileType);

			tableInUse.beginTransaction();
			tableInUse.add(dog);
			tableInUse.beginTransaction();
			tableInUse.add(dogUpdated);
			tableInUse.rollback();

			List<Object> result = (List<Object>) tableInUse.searchWithRegex("Re.*");
			assertEquals(0, result.size());
			List<Object> result1 = (List<Object>) tableInUse.searchWithRegex("Ra.*");
			assertEquals(0, result1.size());
			List<DebugInfo> info = (List<DebugInfo>) tableInUse.getRowsWithDebugInfo();
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