package fun.madeby.server;

import io.javalin.Javalin;

/**
 * Created by Gra_m on 2022 07 19
 */


public final class RESTServer {

	public static void main(String[] args) {
		Javalin app = Javalin.create().start(7001);

			app.get("/listall", DBController.fetchAllRecords);
			app.get("/add", DBController.addCarOwner);
			app.get("/searchlevenshtein", DBController.searchLevenshtein);
	}
}
