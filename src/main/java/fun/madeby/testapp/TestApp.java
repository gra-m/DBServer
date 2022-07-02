package fun.madeby.testapp;

import fun.madeby.CarOwner;
import fun.madeby.DBRecord;
import fun.madeby.dbserver.DBServer;
import fun.madeby.util.DebugInfo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Gra_m on 2022 06 25
 */

public class TestApp {
	final static int AMOUNT_OF_EACH = 3;
	final static String dbFile = "DBServer.db";
	public static void main(String[] args) throws IOException {
		DBServer dbServer = new DBServer(dbFile);
		dbServer.close();

		new TestApp().performTest();
	}

	private void performTest() throws FileNotFoundException {
		try {
			fillDB(AMOUNT_OF_EACH);
			delete(2);
			delete(5);
			delete(8);
			listAllFileRecords();

		}catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void listAllFileRecords() throws IOException {
		DBServer dbServer = new DBServer(dbFile);
		long count = 1L;
		long rowPosition = 0L;
		ArrayList<DebugInfo> data = (ArrayList<DebugInfo>) dbServer.getData();
		for (DebugInfo di: data) {
			prettyPrint(di, count, rowPosition);
			count++;
			rowPosition++;
		}
		dbServer.close();
	}

	private void prettyPrint(DebugInfo di, long count, long rowPosition) {
		CarOwner carOwner = (CarOwner) di.getDbRecord();
		String debugChar = di.isDeleted() ? "-":"+";
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
		DBServer dbServer = new DBServer(dbFile);
		dbServer.delete(rowNumber);
		dbServer.close();
	}

	void fillDB(int amountOfEach) throws IOException {
		DBServer dbServer = new DBServer(dbFile);
		dbServer.refreshIndex();

		for(int i=0; i< amountOfEach; i++) {
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
		}
		dbServer.close();
	}
}