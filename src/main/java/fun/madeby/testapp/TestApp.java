package fun.madeby.testapp;

import fun.madeby.CarOwner;
import fun.madeby.DBRecord;
import fun.madeby.Index;
import fun.madeby.dbserver.DB;
import fun.madeby.dbserver.DBServer;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Gra_m on 2022 06 25
 */

public class TestApp {
	public static void main(String[] args) throws FileNotFoundException {
		final String dbFile = "DBServer.db";
		DB dbServer = new DBServer(dbFile);

		//Write
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

		try {
			dbServer.add(carOwner);
			dbServer.add(carOwner2);
			dbServer.add(carOwner3);
			System.out.println("TestApp: Total rows in db = " + Index.getInstance().getTotalNumberOfRows());
			System.out.println("TestApp: Total rows in in row index = " + Index.getInstance().getMapRowNumberBytePositionSize());
			System.out.println("TestApp: Total rows in name index = " + Index.getInstance().getMapDbRecordNameBytePositionSize());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Read

		try {
			DBRecord carOwnerReadBack = dbServer.read(0L);
			DBRecord carOwnerReadBack1 = dbServer.read(1L);
			DBRecord carOwnerReadBack2 = dbServer.read(2L);
			System.out.println("Test App printing read back '0L' " + carOwnerReadBack);
			System.out.println("Test App printing read back '1L' " + carOwnerReadBack1);
			System.out.println("Test App printing read back '2L' " + carOwnerReadBack2);

		} catch (IOException e) {
			e.printStackTrace();
		}

		/*//Update by row number (once only, update sets row boolean to deleted, deleted rows cannot be update).
		try {

			dbServer.update(Index.getInstance().getRowNumberByName("Frank Demlan"), carOwner2Updated);

			//read update back
			DBRecord retrievedCarOwner2Updated = dbServer.read(Index.getInstance().getRowNumberByName("Frank Demlan"));
			System.out.println("TestApp: printing carOwner2Updated: " + retrievedCarOwner2Updated);

		} catch(IOException e) {
			e.printStackTrace();
		}

		//Update by name "Frank Demlan"
		try {
			DBRecord carOwner3 = new CarOwner(
					"Funk Adelic",
					20,
					"Herbert Street, Antwerp, 2000",
					"VJW7076",
					"Doesn't know that we know that he knows we have a file on him"
			);
			dbServer.update( "Frank Demlan", carOwner3);

			//read update back
			DBRecord retrievedUpdatedByName = dbServer.read(Index.getInstance().getRowNumberByName("Funk Adelic"));
			System.out.println(retrievedUpdatedByName);

		} catch(Exception e) {
			e.printStackTrace();
		}
*/
	}
}