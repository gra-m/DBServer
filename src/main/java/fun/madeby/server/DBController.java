package fun.madeby.server;

import fun.madeby.CarOwner;
import fun.madeby.DBRecord;
import fun.madeby.db.specific_server.DB;
import fun.madeby.db.specific_server.DBSpecificServer;
import fun.madeby.util.DebugInfo;
import fun.madeby.util.DebugRowInfo;
import fun.madeby.util.JSONRep;
import fun.madeby.util.LoggerSetUp;
import io.javalin.http.Handler;
import org.eclipse.jetty.util.ajax.JSONPojoConvertor;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * Created by Gra_m on 2022 07 19
 */

public final class DBController {
	private static DB database;
	private static Logger LOGGER;

	static {
		try {
			LOGGER = LoggerSetUp.setUpLogger("DBController");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static {
		try {
			database = new DBSpecificServer("restTest.db");
		} catch (IOException e) {
			throw new RuntimeException("@DBController/static{dbcreate} could not create DBSpecificServer(\"restTest.db\")");
		}
	}



	public static void controllerLogDbClose(String message, boolean isStopping) throws IOException {
		if(!isStopping)
		LOGGER.severe(message);
		else {
			LOGGER.severe(message);
			database.close();
		}


	}

	//this did not work
	/*public static Handler exit = ctx -> {
	// exit causes shutdownHooks to be activated halt() doesnot.
		try {
			ctx.json(true);
		} finally {
			systemExit();
		}
	};

	private static void systemExit() {
		System.exit(0);
	}*/


	// localhost:7001/listall
	public static Handler fetchAllRecords = ctx -> {
		/*Long totalRecordNumber = database.getTotalRecordAmount();
		ArrayList<String> allRecordList = new ArrayList<>(Math.toIntExact(totalRecordNumber));
		LongStream.range(0, totalRecordNumber).forEach(i-> {
				allRecordList.add(database.read(i).toJSON());
		});*/

		ArrayList<DebugInfo> debugResultList = (ArrayList<DebugInfo>) database.getRowsWithDebugInfo();
		ArrayList<String> allRecordList = new ArrayList<>(debugResultList.size());
		for(DebugInfo di: debugResultList) {
			DebugRowInfo dri =  (DebugRowInfo) di;
			if(!dri.isDeleted() && !dri.isTemporary())
				allRecordList.add(((JSONRep)dri.object).toJSON());
		}


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

	// localhost:7001/searchlevenshtein?name=test
	public static Handler searchLevenshtein = ctx -> {
		String  name = ctx.queryParam("name");

		if (name == null) {
			ctx.json("{\"Error\": \"Parameter is missing\"}");
			return;
		}

		Collection<DBRecord> levenshteinReturn = database.searchWithLevenshtein(name, 1);
		LinkedList<String> result = new LinkedList<>();
		levenshteinReturn.forEach(i -> {
			result.add(((JSONRep)i).toJSON());
		});

		ctx.json(result);
	};


}
