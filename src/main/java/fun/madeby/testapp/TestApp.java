package fun.madeby.testapp;

import fun.madeby.CarOwner;
import fun.madeby.FileHandler;
import fun.madeby.DbRecord;

import java.io.IOException;

/**
 * Created by Gra_m on 2022 06 25
 */

public class TestApp {
	public static void main(String[] args) {

		//Write
		CarOwner carOwner = new CarOwner("Frank Demian",
				20,
				"Herbert Street, Antwerp, 2000",
				"VJW707S",
				"Doesn't know we have a file on him at all");

		try {
			FileHandler fh = new FileHandler("DBServer.db");
			fh.add(carOwner);
			fh.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Read

		try {
			FileHandler fh = new FileHandler("DBServer.db");
			DbRecord carOwner2 = fh.readRow(0L);
			System.out.println(carOwner2);
			fh.close();
		} catch (IOException e) {
			e.printStackTrace();
		}



	}
}
