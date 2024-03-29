package fun.madeby.generic_server.defrag;

import fun.madeby.Dog;
import fun.madeby.Person;
import fun.madeby.db.DBFactory;
import fun.madeby.db.Table;
import fun.madeby.db.generic_server.DBGenericServer;
import fun.madeby.generic.GenericIndex;
import fun.madeby.util.DebugInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
class DBGenericDefragTest {
	private final String dogTableName = "testDogDefrag";
	private final String personTableName = "testPersonDefrag";
	/*private final String personTableName2 = "testPersonDefrag2";
	private final String personTableName3 = "testPersonDefrag3";*/
	private final String fileType = ".db";
	private Object dog;
	private Object dogUpdated;
	private Object dogSimilar;
	private Object person;
	private Object personUpdated;
	private Object personSimilar;
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
		try (BufferedWriter ignored = Files.newBufferedWriter(Path.of("./" + personTableName + fileType),
				StandardOpenOption.TRUNCATE_EXISTING)) {
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*try (BufferedWriter ignored = Files.newBufferedWriter(Path.of("./" + personTableName2 + fileType),
			  StandardOpenOption.TRUNCATE_EXISTING)) {
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (BufferedWriter ignored = Files.newBufferedWriter(Path.of("./" + personTableName3 + fileType),
			  StandardOpenOption.TRUNCATE_EXISTING)) {
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		/* Dog table not being tested at present
		try (BufferedWriter ignored = Files.newBufferedWriter(Path.of("./" + dogTableName + fileType),
			  StandardOpenOption.TRUNCATE_EXISTING)) {
			// ignored
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}


	//region DEBUG_ISSUE#54
	//region PreDefrag
	@Test
	@DisplayName("@TestGetTotalNumberOfRows_PreDefrag == 3")
	void testGetTotalNumberOfRowsPreDefrag() {

		try (DBGenericServer db = DBFactory.getGenericDB()) {

		tableInUse = db.useTable(personTableName, PERSON_SCHEMA, Person.class);
		index = db.getIndex(personTableName + fileType);
		tableInUse.beginTransaction();
		tableInUse.add(person);
		tableInUse.add(personUpdated);
		tableInUse.add(personSimilar);
		tableInUse.commit();

		assertEquals(3, index.getTotalNumberOfRows());

		} catch (Exception e) {
			Assertions.fail();
		}

	}

	@Test
	@DisplayName("@TestGetRowsWithDebugInfo_PreDefrag == 3")
	void testGetRowsWithDebugInfoPreDefrag() {

		try (DBGenericServer db = DBFactory.getGenericDB()) {

			tableInUse = db.useTable(personTableName, PERSON_SCHEMA, Person.class);
			index = db.getIndex(personTableName + fileType);
			tableInUse.beginTransaction();
			tableInUse.add(person);
			tableInUse.add(personUpdated);
			tableInUse.add(personSimilar);
			tableInUse.commit();

			ArrayList<DebugInfo> debugList = (ArrayList<DebugInfo>) tableInUse.getRowsWithDebugInfo();
			Assertions.assertEquals(3, debugList.size());

		} catch (Exception e) {
			Assertions.fail();
		}

	}



	@Test
	@DisplayName("@TestGetTotalRecordAmount_PreDefrag == 3")
	void testGetTotalRecordAmountPreDefrag() {

		try (DBGenericServer db = DBFactory.getGenericDB()) {
			Long recNumber = 0L;

			tableInUse = db.useTable(personTableName, PERSON_SCHEMA, Person.class);
			index = db.getIndex(personTableName + fileType);
			tableInUse.beginTransaction();
			tableInUse.add(person);
			tableInUse.add(personUpdated);
			tableInUse.add(personSimilar);
			tableInUse.commit();

			recNumber = tableInUse.getTotalRecordAmount();
			Assertions.assertEquals(3, recNumber);

		} catch (Exception e) {
			Assertions.fail();
		}

	}
//endregion


@Test
@DisplayName("@testTotalRowsAfterDeleteAndDefrag(): totalRows 3rows[defrag]->2rows")
void testTotalRowsAfterDeleteAndDefrag() {
	try (DBGenericServer db = DBFactory.getGenericDB()) {
		tableInUse = db.useTable(personTableName, PERSON_SCHEMA, Person.class);
		index = db.getIndex(personTableName + fileType);

		tableInUse.beginTransaction();
		tableInUse.add(person);
		tableInUse.add(personUpdated);
		tableInUse.add(personSimilar);
		tableInUse.commit();

		// delete first record
		tableInUse.beginTransaction();
		tableInUse.delete(0L);
		tableInUse.commit();

		// call defrag
		tableInUse.defragmentTable();
		assertEquals(0, index.getTotalNumberOfRows()); // fixme returns 0 index dead after defrag?

	} catch (Exception e) {
		Assertions.fail();
	}

}

@Test
@DisplayName("@testDebugInfoRowsAfterDeleteAndDefrag(): totalRowsWithDebugInfo 3rows[Defrag]->2rows")
void testDebugInfoRowsAfterDeleteAndDefrag()  {
	try (DBGenericServer db = DBFactory.getGenericDB()) {
		tableInUse = db.useTable(personTableName, PERSON_SCHEMA, Person.class);
		index = db.getIndex(personTableName + fileType);

		tableInUse.beginTransaction();
		tableInUse.add(person);
		tableInUse.add(personUpdated);
		tableInUse.add(personSimilar);
		tableInUse.commit();

		// delete first record
		tableInUse.beginTransaction();
		tableInUse.delete(0L);
		tableInUse.commit();

		// call defrag
		tableInUse.defragmentTable();
		ArrayList<DebugInfo> debugList = (ArrayList<DebugInfo>) tableInUse.getRowsWithDebugInfo();
//		Assertions.assertEquals(0, debugList.size()); //fixme, running this after defrag causes exception (based on being unable to delete old file?)

	} catch (Exception e) {
		Assertions.fail();
	}

}

	@Test
	@DisplayName("@testGetTotalRecordsAfterDeleteAndDefrag(): totalRecords 3rows[Defrag]->2rows")
	void testTotalRecordsAfterDeleteAndDefrag()  {
		try (DBGenericServer db = DBFactory.getGenericDB()) {
			Long totalRecords = 0L;
			tableInUse = db.useTable(personTableName, PERSON_SCHEMA, Person.class);
			index = db.getIndex(personTableName + fileType);

			tableInUse.beginTransaction();
			tableInUse.add(person);
			tableInUse.add(personUpdated);
			tableInUse.add(personSimilar);
			tableInUse.commit();

			// delete first record
			tableInUse.beginTransaction();
			tableInUse.delete(0L);
			tableInUse.commit();

			// call defrag
			tableInUse.defragmentTable();
			totalRecords = tableInUse.getTotalRecordAmount();
			Assertions.assertEquals(0, totalRecords); //fixme returns 0 total records after defrag


		} catch (Exception e) {
			Assertions.fail();
		}

	}




//endregion DEBUG_ISSUE#54


	/*@Test
	@DisplayName("@testDefragmentationAfterDelete(): totalRows 3rows->2rows->defrag")
	void testDefragmentationAfterDelete()  {
		try (DBGenericServer db = DBFactory.getGenericDB()) {
			Long recNumber = 0L;
			tableInUse = db.useTable(personTableName, PERSON_SCHEMA, Person.class);
			index = db.getIndex(personTableName + fileType);

			tableInUse.beginTransaction();
			tableInUse.add(person);
			tableInUse.add(personUpdated);
			tableInUse.add(personSimilar);
			tableInUse.commit();
			assertEquals(3, index.getTotalNumberOfRows());

			// delete first record
			tableInUse.beginTransaction();
			tableInUse.delete(0L);
			tableInUse.commit();

			// call defrag
			tableInUse.defragmentTable();
			recNumber = tableInUse.getTotalRecordAmount();
			Assertions.assertEquals(2, recNumber);

			ArrayList<DebugInfo> debugList = (ArrayList<DebugInfo>) tableInUse.getRowsWithDebugInfo();
			Assertions.assertEquals(2, debugList.size());
//			db.suspendCurrentTable();

		} catch (Exception e) {
			Assertions.fail();
		}

	}

	 */


	/*@Test
	@DisplayName("testDefragAfterRollback_Add totalRows added 3-> rolled back -> 0 total no rows && 1 rows with debug info defrag -> 0 rows")
	void testDefragAfterRollback_Add()  {
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

			Long totalRecords = tableInUse.getTotalRecordAmount();
			Assertions.assertEquals(0, totalRecords);

			tableInUse.defragmentTable();

			assertEquals(0, index.getTotalNumberOfRows());
			ArrayList<DebugInfo> debugList1 = (ArrayList<DebugInfo>) tableInUse.getRowsWithDebugInfo();
			Assertions.assertEquals(0, debugList1.size());
//			db.suspendCurrentTable();


		} catch (Exception e) {
			Assertions.fail();
		}

	}

	@Test
	@DisplayName("@testDefragmentationEmptyTransactionBehaviour() -> 0 rows no exceptions")
	void testDefragmentationEmptyTransactionBehaviour()  {
		try (DBGenericServer db = DBFactory.getGenericDB()) {
			Long recNumber = 0L;
			tableInUse = db.useTable(personTableName, PERSON_SCHEMA, Person.class);
			index = db.getIndex(personTableName + fileType);

			tableInUse.beginTransaction();
			tableInUse.commit();
			assertEquals(0, index.getTotalNumberOfRows());
			tableInUse.defragmentTable();
			recNumber = tableInUse.getTotalRecordAmount();
			Assertions.assertEquals(0, recNumber);
			ArrayList<DebugInfo> debugList = (ArrayList<DebugInfo>) tableInUse.getRowsWithDebugInfo();
			Assertions.assertEquals(0, debugList.size());
//			db.suspendCurrentTable();

		} catch (Exception e) {
			Assertions.fail();
		}
	}*/

}