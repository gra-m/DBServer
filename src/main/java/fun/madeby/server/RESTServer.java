package fun.madeby.server;

import io.javalin.Javalin;

/**
 * Created by Gra_m on 2022 07 19
 */


public final class RESTServer {

	private static final int PORT = 7001;
	private final Javalin app;

	public RESTServer() {
		try (Javalin app = Javalin.create()) {
			this.app = app;

			Runtime.getRuntime().addShutdownHook(new Thread(app::stop));

			app.events(event -> {
				event.serverStarted(() -> {
					DBController.controllerLogDbClose("@eventServer has started", false);
				});
				event.serverStopping(() -> {
					DBController.controllerLogDbClose("@eventServer is stopping", true);
				});
				event.serverStopped(() -> {
					DBController.controllerLogDbClose("@event Server has stopped, database close being sent", false);
				});
			});
			//Lambda
			/*Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				app.stop();
			}));*/
		}
	}



	void startServer() {
		app.start(PORT);
		app.get("/listall", DBController.fetchAllRecords);
		app.post("/add", DBController.addCarOwner);
		app.get("/searchlevenshtein", DBController.searchLevenshtein);
		//app.get("/exit", DBController.exit);
	}



	public static void main(String[] args) {
		RESTServer restServer = new RESTServer();
		restServer.startServer();
	}
}
