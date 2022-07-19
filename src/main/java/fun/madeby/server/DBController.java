package fun.madeby.server;

import fun.madeby.dbserver.DB;
import fun.madeby.dbserver.DBServer;
import io.javalin.http.Handler;

import java.io.FileNotFoundException;
import java.util.ArrayList;
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

	public static Handler addDBRecord = ctx -> {
		Long totalRecordNumber = database.getTotalRecordAmount();
		ArrayList<String> allRecordList = new ArrayList<>(Math.toIntExact(totalRecordNumber));
		LongStream.range(0, totalRecordNumber).forEach(i-> {
			allRecordList.add(database.read(i).toJSON());
		});

		ctx.json(allRecordList);
	};

}
