package fun.madeby.testapp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fun.madeby.CarOwner;
import fun.madeby.DBRecord;
import fun.madeby.db.specific_server.DBSpecificServer;
import fun.madeby.exceptions.DBException;
import fun.madeby.exceptions.DuplicateNameException;
import fun.madeby.specific.Index;
import fun.madeby.db.specific_server.DB;
import fun.madeby.transaction.ITransaction;
import fun.madeby.util.DebugInfo;
import fun.madeby.util.Levenshtein;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Created by Gra_m on 2022 06 25
 */

@SuppressFBWarnings({"DMI_RANDOM_USED_ONLY_ONCE", "DMI_RANDOM_USED_ONLY_ONCE"})
@SuppressWarnings("InfiniteLoopStatement")
public class TestApp {
	final static int AMOUNT_OF_EACH = 2;
	final static String dbFile = "DBSpecificServer.db";


	public static void main(String[] args) throws DuplicateNameException, DBException
		{
		TestApp testApp = new TestApp();

		testApp.clearDataInExistingFile(); // @ #14 this causes IOException when file empty, this is the #13 bug helped by extending closeable
		testApp.addOneRecordWithTransaction();
		testApp.listAllFileRecords();
		//testApp.performTest();
		//testApp.performDefragTest();
		//testApp.performMultiThreadTest();
	}

	@SuppressFBWarnings("DMI_RANDOM_USED_ONLY_ONCE")
	private void performMultiThreadTest() throws DuplicateNameException, DBException
		{
		CountDownLatch cl = new CountDownLatch(3);
		Runnable runnableAdd = null;
		Runnable runnableUpdate = null;
		Runnable runnableListAll = null;
		try (DB dbServer = new DBSpecificServer(dbFile)) {
			runnableAdd = () -> {
				while (true) {
					int i = new Random().nextInt(0, 4000);
					CarOwner c = new CarOwner("John" + i, 44, "Berlin", "VJW707S", "This is a very enjoyable description, I only hope you enjoyed reading it as much as I enjoyed...");
					try {
						dbServer.add(c);
					} catch (DuplicateNameException e) {
						throw new RuntimeException("@PerformMultiThreadTest()/runnableAdd threw DuplicateNameException");

					}
				}
			};

			runnableUpdate = ()  -> {
				while (true) {
					int i = new Random().nextInt(0, 4000);
					CarOwner c = new CarOwner("John" + i, 44, "Berlin", "VJW707S", "This is a very enjoyable description, I only hope you enjoyed reading it as much as I enjoyed...");
					try {
						dbServer.update("John" + i, c);
					} catch (DuplicateNameException e) {
						throw new RuntimeException("@PerformMultiThreadTest()/runnableUpdate threw DuplicateNameException");

					}
				}
			};

			runnableListAll = () -> {
				while (true) {
					try {
						listAllFileRecords();
					} catch (DBException e) {
						throw new RuntimeException(e);
					}
				}
			};

			ExecutorService executorService = Executors.newFixedThreadPool(3);
			executorService.submit(runnableAdd);
			executorService.submit(runnableUpdate);
			executorService.submit(runnableListAll);
			cl.await();
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}



	private void performDefragTest() throws DuplicateNameException, DBException
		{
		clearDataInExistingFile();
		fragementDatabase();
		listAllFileRecords();
		System.out.println("\n\n-------------------NOW  DEFRAGGING----------------------\n\n");
		defragmentDatabase();
		//listAllFileRecords();

	}

	private void defragmentDatabase() throws DuplicateNameException, DBException
		{
			try (DB dbServer = new DBSpecificServer(dbFile)) {
				dbServer.defragmentDatabase();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private void fragementDatabase() throws DuplicateNameException, DBException
		{
		try (DB dbServer = new DBSpecificServer(dbFile)) {

			// create 100 records
			dbServer.beginTransaction();
			for (int i : IntStream.range(0, 2).toArray()) {
				CarOwner c = new CarOwner("John" + i, 44, "Berlin", "VJW707S", "This is a very enjoyable description, I only hope you enjoyed reading it as much as I enjoyed...");
				dbServer.add(c);
			}
			dbServer.commit();

			//listAllFileRecords();

			// update half of them
			dbServer.beginTransaction();
			for (long i : IntStream.range(0, 2).toArray()) {
				if (i % 2 == 0) {
					dbServer.update(i, new CarOwner("Rupert" + i + "__Updated", 44, "Berlin", "VJW707S", "This is a very enjoyable description, I only hope you enjoyed reading it as much as I enjoyed..."));
				}
			}
			dbServer.commit();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private void performTest() throws DuplicateNameException, DBException
		{
		clearDataInExistingFile();
		fillDB();
		delete("Frank Demian");
		delete("Frank Demlan");
		delete("Funk Adelic");
		listAllFileRecords();
		testSearch("Frank Demian");
		testLevenshtein();
		printLevenshtein();
		testRegEx();

	}

	private void testRegEx() throws DBException
		{
		try (DB dbServer = new DBSpecificServer(dbFile)) {
			ArrayList<DBRecord> result = (ArrayList<DBRecord>) dbServer.searchWithRegex("Fra.*");
			System.out.println("---------searchWithRegEx()-----------");
			for(DBRecord record: result) {
				System.out.println(record);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printLevenshtein() {
		Levenshtein.levenshteinDistance("ziggy", "zaggys", true);
	}

	private void testLevenshtein() throws DBException
		{
		try (DB dbServer = new DBSpecificServer(dbFile)) {
			ArrayList<DBRecord> result = (ArrayList<DBRecord>) dbServer.searchWithLevenshtein("Frank Demian1", 0);
			System.out.println("---------searchWithLevenshtein()-----------");
			for(DBRecord record: result) {
				System.out.println(record);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addOneRecordWithTransaction() throws DuplicateNameException, DBException
		{

		try (DB dbServer = new DBSpecificServer(dbFile)) {
			ITransaction transaction = dbServer.beginTransaction();
			DBRecord carOwner = new CarOwner("Frank Demian",
					20,
					"Herbert Street, Antwerp, 2000",
					"VJW707S",
					"Doesn't know we have a file on him at all");
			dbServer.add(carOwner);
			dbServer.commit();
			//dbServer.rollback();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void testSearch(String name) throws DuplicateNameException, DBException
		{

		try (DB dbServer = new DBSpecificServer(dbFile)) {
			System.out.println("TEST SEARCH: ");
			addOneRecordWithTransaction();
			dbServer.refreshIndex();
			CarOwner cO = (CarOwner) dbServer.search(name);
			System.out.println("Found carOwner: " + cO);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void clearDataInExistingFile() {
		try (BufferedWriter ignored = Files.newBufferedWriter(Path.of("./" + dbFile),
				StandardOpenOption.TRUNCATE_EXISTING)) {
			System.out.println("CLEARING EXISTING RECORDS..");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	private void listAllFileRecords() throws DBException
		{

		try (DB dbServer = new DBSpecificServer(dbFile)) {
			long count = 1L;
			long rowPosition = 0L;
			ArrayList<DebugInfo> data = (ArrayList<DebugInfo>) dbServer.getRowsWithDebugInfo();
			for (DebugInfo di : data) {
				prettyPrint(di, count, rowPosition);
				count++;
				rowPosition++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void prettyPrint(DebugInfo di, long count, long rowPosition) {
		CarOwner carOwner = (CarOwner) di.getDbRecord();
		String debugCharTemp = di.isTemporary() ? "temp" : "final";
		String debugCharDeleted = di.isDeleted() ? "-" : "+";
		String formatted = String.format("%d %d %s %s name: %s age: %d address: %s carplateNumber %s description %s",
				count,
				rowPosition,
				debugCharTemp,
				debugCharDeleted,
				carOwner.getName(),
				carOwner.getAge(),
				carOwner.getAddress(),
				carOwner.getCarPlateNumber(),
				carOwner.getDescription());
		System.out.println(formatted);
	}

	void delete(long rowNumber) throws DBException
		{

		try (DB dbServer = new DBSpecificServer(dbFile)) {
			dbServer.delete(rowNumber);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void delete(String name) throws DBException
		{
		try (DB dbServer = new DBSpecificServer(dbFile)) {
			dbServer.beginTransaction();
			dbServer.delete(Index.getInstance().getRowNumberByName(name));
			dbServer.commit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void fillDB() throws DuplicateNameException, DBException
		{
		int count = 0;


		try (DB dbServer = new DBSpecificServer(dbFile)) {
				for (int i = 0; i < 1; i++) {
					DBRecord carOwner = new CarOwner("Frank Demian",
							20,
							"Herbert Street, Antwerp, 2000",
							"VJW707S",
							"Doesn't know we have a file on him at all");
					DBRecord carOwner2 = new CarOwner(
							"Frank Demlan",
							20,
							"Herbert Street, Antwerp, 2000",
							"VJW7076",
							"Doesn't know that we know that he knows we have a file on him");
					DBRecord carOwner3 = new CarOwner(
							"Funk Adelic",
							20,
							"Herbert Street, Antwerp, 2000",
							"VJW7076",
							"Doesn't know that we know that he knows we have a file on him"
					);

					dbServer.beginTransaction();
					dbServer.add(carOwner);
					dbServer.add(carOwner2);
					dbServer.add(carOwner3);
					dbServer.commit();
					count++;
			}
			for (int i = 0; i < AMOUNT_OF_EACH; i++) {
				DBRecord carOwner = new CarOwner(
						"Frank Demian" + i,
						23 + i,
						"Herbert Street, Antwerp, 2000",
						"VJW707S",
						"Doesn't know we have a file on him at all");
				DBRecord carOwner2 = new CarOwner(
						"Frank Demlan" + i,
						23 + i,
						"Herbert Street, Antwerp, 2000",
						"VJW7076",
						"Doesn't know that we know that he knows we have a file on him");
				DBRecord carOwner3 = new CarOwner(
						"Funk Adelic" + i,
						23 + i,
						"Herbert Street, Antwerp, 2000",
						"VJW7076",
						"Doesn't know that we know that he knows we have a file on him"
				);

				dbServer.beginTransaction();
				dbServer.add(carOwner);
				dbServer.add(carOwner2);
				dbServer.add(carOwner3);
				dbServer.commit();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}