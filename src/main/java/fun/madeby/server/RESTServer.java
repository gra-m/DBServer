package fun.madeby.server;

import io.javalin.Javalin;
import io.javalin.core.event.EventListener;
import io.javalin.core.event.JavalinEvent;

/**
 * Created by Gra_m on 2022 07 19
 */


public final class RESTServer {

	private final Javalin app;

	public RESTServer() {
		try (Javalin app = Javalin.create()) {
			this.app = app;
		}
	}



	void startServer() {
		app.start(7001);
		app.get("/listall", DBController.fetchAllRecords);
		app.post("/add", DBController.addCarOwner);
		app.get("/searchlevenshtein", DBController.searchLevenshtein);
	}





	public static void main(String[] args) {
		RESTServer restServer = new RESTServer();
		restServer.startServer();
	}
}
