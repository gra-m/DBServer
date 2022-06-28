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

		try {
			dbServer.add(carOwner);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Read

		try {
			DBRecord carOwner2 = dbServer.read(0L);
			System.out.println(carOwner2);
			System.out.println("TestApp: Total rows in db = " + Index.getInstance().getTotalNumberOfRows());
			dbServer.delete(0L);
			System.out.println("TestApp: Now, after deleting row 0 in db = " + Index.getInstance().getTotalNumberOfRows());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
