package fun.madeby.server;

import fun.madeby.CarOwner;
import fun.madeby.DBRecord;
import fun.madeby.db.specific_server.DB;
import fun.madeby.db.specific_server.DBServer;
import io.javalin.http.Handler;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.LongStream;

/**
 * Created by Gra_m on 2022 07 19
 */

public final class DBController {
	private static DB database;

	static {
		try {
			database = new DBServer("restTest.db");
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static Handler fetchAllRecords = ctx -> {
		Long totalRecordNumber = database.getTotalRecordAmount();
		ArrayList<String> allRecordList = new ArrayList<>(Math.toIntExact(totalRecordNumber));
		LongStream.range(0, totalRecordNumber).forEach(i-> {
				allRecordList.add(database.read(i).toJSON());
		});

	 ctx.json(allRecordList);
	};

	// localhost:7001/add?name=test&age=45&address=somewheretest&carplate=qqq-123&description=testEntry
	public static Handler addCarOwner = ctx -> {
		String name = ctx.queryParam("name");
		int age = Integer.parseInt(ctx.queryParam("age"));
		String address = ctx.queryParam("address");
		String carPlate = ctx.queryParam("carplate");
		String description = ctx.queryParam("description");

		if(name == null || address == null || carPlate == null || description == null)
			ctx.json("{Error: Parameter is missing}");
		else {
			database.beginTransaction();
			database.add(new CarOwner(name, age, address, carPlate, description));
			database.commit();
		}

		ctx.json(true);

	};

	// searchLevenshtein?name=test
	public static Handler searchLevenshtein = ctx -> {
		String  name = ctx.queryParam("name");

		if (name == null) {
			ctx.json("{\"Error\": \"Parameter is missing\"}");
			return;
		}

		Collection<DBRecord> levenshteinReturn = database.searchWithLevenshtein(name, 1);
		LinkedList<String> result = new LinkedList<>();
		levenshteinReturn.forEach(i -> {
			result.add(i.toJSON());
		});

		ctx.json(result);
	};


}
