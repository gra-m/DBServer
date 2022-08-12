package fun.madeby.specific_server.defrag;

import fun.madeby.CarOwner;
import fun.madeby.DBRecord;
import fun.madeby.db.DBFactory;
import fun.madeby.db.specific_server.DBSpecificServer;
import fun.madeby.specific.Index;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
class DBSpecificDefragTest {
	private final String dbFileName = "testSpecificDefrag.db";
	private DBSpecificServer db;
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


	//@Test getRowsWithDebugInfo (this was crashing below)j

	@Test
	@DisplayName("@testSpecificDefragAfterDelete(): totalRows 2rows->1rows->defrag")
	void testSpecificDefragAfterDelete()  {
		try ( DBSpecificServer db = DBFactory.getSpecificDB(dbFileName)) {
			Long recNumber = 0L;

			db.beginTransaction();
			db.add(carOwner);
			db.add(carOwnerUpdated);
			db.commit();
			assertEquals(2, index.getTotalNumberOfRows());

			// delete first record
			db.beginTransaction();
			db.delete(0L);
			db.commit();

			// call defrag
			db.defragmentDatabase();
			recNumber = db.getTotalRecordAmount();
			Assertions.assertEquals(1, recNumber);

			ArrayList<DebugInfo> debugList = (ArrayList<DebugInfo>) db.getRowsWithDebugInfo();
			Assertions.assertEquals(2, debugList.size());

		} catch (Exception e) {
			Assertions.fail();
		}

	}


	@Test
	@DisplayName("testGenericDefragAfterRollback_Add totalRows added 3-> rolled back -> 0 total no rows && 1 rows with debug info defrag -> 0 rows")
	void testSpecificDefragAfterRollback_Add()  {
		try (DBSpecificServer db = DBFactory.getSpecificDB(dbFileName)) {

			db.beginTransaction();
			db.add(carOwner);
			db.add(carOwnerUpdated);
			db.rollback();
			assertEquals(0, index.getTotalNumberOfRows());

			ArrayList<DebugInfo> debugList = (ArrayList<DebugInfo>) db.getRowsWithDebugInfo();
			Assertions.assertEquals(2, debugList.size());

			Long totalRecords = db.getTotalRecordAmount();
			Assertions.assertEquals(0, totalRecords);

			db.defragmentDatabase();

			assertEquals(0, index.getTotalNumberOfRows());

		} catch (Exception e) {
			Assertions.fail();
		}

	}


	@Test
	@DisplayName("@testSpecificDefragEmptyTransactionBehaviour() -> 0 rows no exceptions")
	void testSpecificDefragEmptyTransactionBehaviour()  {
		try (DBSpecificServer db = DBFactory.getSpecificDB(dbFileName)) {
			Long recNumber = 0L;

			db.beginTransaction();
			db.commit();
			assertEquals(0, index.getTotalNumberOfRows());

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