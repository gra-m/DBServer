package fun.madeby.generic_server.defrag;

import fun.madeby.Dog;
import fun.madeby.Person;
import fun.madeby.db.DBFactory;
import fun.madeby.db.generic_server.DBGenericServer;
import fun.madeby.exceptions.DBException;
import fun.madeby.exceptions.DuplicateNameException;
import fun.madeby.generic.GenericIndex;
import fun.madeby.util.DebugInfo;
import fun.madeby.util.DebugRowInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
class DBGenericDefragTest {
	private final String dbFileName = "testGenericDefrag.db";
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
	@DisplayName("@testDefragmentationAfterDelete(): totalRows 3rows->2rows->defrag")
	void testDefragmentationAfterDelete()  {
		try (DBGenericServer db = DBFactory.getGenericDB(dbFileName, PERSON_SCHEMA, Person.class)) {
			Long recNumber = 0L;

			db.beginTransaction();
			db.add(person);
			db.add(personUpdated);
			db.add(personSimilar);
			db.commit();
			assertEquals(3, GenericIndex.getInstance().getTotalNumberOfRows());

			// delete first record
			db.beginTransaction();
			db.delete(0L);
			db.commit();

			// call defrag
			db.defragmentDatabase();
			recNumber = db.getTotalRecordAmount();
			Assertions.assertEquals(2, recNumber);

			ArrayList<DebugInfo> debugList = (ArrayList<DebugInfo>) db.getRowsWithDebugInfo();
			Assertions.assertEquals(2, debugList.size());

		} catch (Exception e) {
			Assertions.fail();
		}

	}


	@Test
	@DisplayName("testDefragAfterRollback_Add totalRows added 3-> rolled back -> 0 total no rows && 1 rows with debug info defrag -> 0 rows")
	void testDefragAfterRollback_Add()  {
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

			Long totalRecords = db.getTotalRecordAmount();
			Assertions.assertEquals(0, totalRecords);

			db.defragmentDatabase();

			assertEquals(0, GenericIndex.getInstance().getTotalNumberOfRows());
			ArrayList<DebugInfo> debugList1 = (ArrayList<DebugInfo>) db.getRowsWithDebugInfo();
			Assertions.assertEquals(0, debugList1.size());


		} catch (Exception e) {
			Assertions.fail();
		}

	}


	@Test
	@DisplayName("@testDefragmentationEmptyTransactionBehaviour() -> 0 rows no exceptions")
	void testDefragmentationEmptyTransactionBehaviour()  {
		try (DBGenericServer db = DBFactory.getGenericDB(dbFileName, PERSON_SCHEMA, Person.class)) {
			Long recNumber = 0L;

			db.beginTransaction();
			db.commit();
			assertEquals(0, GenericIndex.getInstance().getTotalNumberOfRows());
			db.defragmentDatabase();
			recNumber = db.getTotalRecordAmount();
			Assertions.assertEquals(0, recNumber);
			ArrayList<DebugInfo> debugList = (ArrayList<DebugInfo>) db.getRowsWithDebugInfo();
			Assertions.assertEquals(0, debugList.size());

		} catch (Exception e) {
			Assertions.fail();
		}

	}



}