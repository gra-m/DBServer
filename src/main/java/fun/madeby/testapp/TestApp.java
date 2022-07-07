package fun.madeby.testapp;

import fun.madeby.CarOwner;
import fun.madeby.DBRecord;
import fun.madeby.Index;
import fun.madeby.dbserver.DB;
import fun.madeby.dbserver.DBServer;
import fun.madeby.util.DebugInfo;
import fun.madeby.util.Levenshtein;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

/**
 * Created by Gra_m on 2022 06 25
 */

public class TestApp {
	final static int AMOUNT_OF_EACH = 2;
	final static String dbFile = "DBServer.db";

	public static void main(String[] args) throws IOException {
		TestApp testApp = new TestApp();

		//testApp.clearDataInExistingFile(); // @ #14 this causes IOException when file empty, this is the #13 bug helped by extending closeable
		//testApp.addOneRecord();
		testApp.performTest();
	}

	private void performTest() throws FileNotFoundException {
		try {
			clearDataInExistingFile();
			fillDB();
			/*delete(0); // todo Never works row numbers change after first delete..
			delete(1);
			delete(2);*/
			delete("Frank Demian");
			delete("Frank Demlan");
			delete("Funk Adelic");
			listAllFileRecords();
			testSearch("Frank Demian");
			testLevenshtein();
			printLevenshtein();
			testRegEx();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void testRegEx() {
		try (DB dbServer = new DBServer(dbFile)) {
			ArrayList<DBRecord> result = (ArrayList) dbServer.searchWithRegex("Fra.*");
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

	private void testLevenshtein()throws IOException{
		try (DB dbServer = new DBServer(dbFile)) {
			ArrayList<DBRecord> result = (ArrayList) dbServer.searchWithLevenshtein("Frank Demian1", 0);
			System.out.println("---------searchWithLevenshtein()-----------");
			for(DBRecord record: result) {
				System.out.println(record);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addOneRecord() throws FileNotFoundException {

		try (DB dbServer = new DBServer(dbFile)) {
			DBRecord carOwner = new CarOwner("Frank Demian",
					20,
					"Herbert Street, Antwerp, 2000",
					"VJW707S",
					"Doesn't know we have a file on him at all");
			dbServer.add(carOwner);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void testSearch(String name) {

		try (DB dbServer = new DBServer(dbFile)) {
			System.out.println("TEST SEARCH: ");
			addOneRecord();
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


	private void listAllFileRecords() throws IOException {

		try (DB dbServer = new DBServer(dbFile)) {
			long count = 1L;
			long rowPosition = 0L;
			ArrayList<DebugInfo> data = (ArrayList<DebugInfo>) dbServer.getData();
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
		String debugChar = di.isDeleted() ? "-" : "+";
		String formatted = String.format("%d %d %s name: %s age: %d address: %s carplateNumber %s description %s",
				count,
				rowPosition,
				debugChar,
				carOwner.getName(),
				carOwner.getAge(),
				carOwner.getAddress(),
				carOwner.getCarPlateNumber(),
				carOwner.getDescription());
		System.out.println(formatted);
	}

	void delete(long rowNumber) throws IOException {

		try (DB dbServer = new DBServer(dbFile)) {
			dbServer.delete(rowNumber);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void delete(String name) {
		try (DB dbServer = new DBServer(dbFile)) {
			dbServer.delete(Index.getInstance().getRowNumberByName(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void fillDB() throws IOException {
		int count = 0;


		try (DB dbServer = new DBServer(dbFile)) {
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

					dbServer.add(carOwner);
					dbServer.add(carOwner2);
					dbServer.add(carOwner3);
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

				dbServer.add(carOwner);
				dbServer.add(carOwner2);
				dbServer.add(carOwner3);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}