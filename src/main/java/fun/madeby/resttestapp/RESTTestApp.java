package fun.madeby.resttestapp;

import fun.madeby.util.LoggerSetUp;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import static java.lang.Thread.currentThread;

/**
 * Created by Gra_m on 2022 07 21
 */

public class RESTTestApp {
	private static Logger LOGGER;

	static{
		try {
			LOGGER = LoggerSetUp.setUpLogger("RESTTestApp");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws InterruptedException {
		RESTTestApp app = new RESTTestApp();
		while(true) {
			app.performTest();
			try{
				Thread.sleep(50);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void performTest() throws InterruptedException {
		
		Thread searchTest = new Thread(() -> {
		while(true)	 {
			performSearchTest();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		});
		
		Thread returnAllRecordsTest = new Thread(() -> {
			while(true) {
				performReturnAllRecordsTest();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		Thread addRecordTest = new Thread(() -> {
			while(true) {
				performAddRecordTest();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		//searchTest.start();
		//returnAllRecordsTest.start();
		addRecordTest.start();

	}

	private void sendRequest(String path, Map<String, String> parameters) {
		StringBuilder sb = new StringBuilder();
			sb.append("http://localhost:7001/").append(path);

		if (parameters != null)
			sb.append("?").append(new BuiltParameter().getParametersAsString(parameters));

		try{
			URL url = new URL(sb.toString());
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setConnectTimeout(1000);
			con.setDoOutput(false);

			//read & log response
			int responseCode = con.getResponseCode();
			LOGGER.info("con.getResponseCode() on currentThread.getName() =  " + responseCode +  " " + currentThread().getName());
			if (responseCode == 200){
				String responseResult = readResponseStream(con.getInputStream());
				LOGGER.info("readResponseStream(con.getInputStream() returned: \n" + responseResult);
			}

			con.disconnect();

		}catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.severe("@sendRequest url.openConnection()");
			e.printStackTrace();
		}

	}
	private void performAddRecordTest() {
		Map<String, String> parameters = new HashMap<>();
		int i = new Random().nextInt(0, 100);
		int x = new Random().nextInt(0, 4000);
		parameters.put("name", "test" + i + "_" + x);
		parameters.put("age", "345");
		parameters.put("address", "1234, Acacia av");
		parameters.put("carplate", "PYP-729Y");
		parameters.put("description", "This is a very interesting description describing how this description was written and why I ..");

		LOGGER.info("@performAddRecordTest about to add ->" + parameters.get("name"));
		sendRequest("add", parameters);
	}

	private void performReturnAllRecordsTest() {
		sendRequest("listall", null);

	}

	private void performSearchTest() {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("name", "test");

		sendRequest("searchlevenshtein", parameters);

	}

	private String readResponseStream(InputStream inputStream) {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder sb = new StringBuilder(500);
		String line;

		try{
		while((line = bufferedReader.readLine()) != null) {
				sb.append(line);
		}
			} catch (IOException e) {
			e.printStackTrace();
			}

		return sb.toString();
	}

	private class BuiltParameter {

		String getParametersAsString(Map<String, String> parameters) {
			StringBuilder sb = new StringBuilder(500);

			for(Map.Entry<String, String> entry: parameters.entrySet()) {
				sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
						.append("=")
						.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
						.append("&");
			}
		return new String(sb);
		}
	}
}
