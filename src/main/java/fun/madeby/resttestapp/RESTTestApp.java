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
import java.util.logging.Logger;

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

	public static void main(String[] args) {
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

	private void performTest() {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("name", "test");

		try{
			//send request
			URL url = new URL("http://localhost:7001/searchlevenshtein?" + new BuiltParameter().getParametersAsString(parameters));
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setConnectTimeout(1000);
			con.setDoOutput(false);

			//read & log response
			int responseCode = con.getResponseCode();
			LOGGER.info("con.getResponseCode() =" + responseCode);
			if (responseCode == 200){
				String responseResult = readResponseStream(con.getInputStream());
				LOGGER.info("readResponseStream(con.getInputStream() returned: \n" + responseResult);
			}

			con.disconnect();

		}catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.severe("@performTest url.openConnection()");
			e.printStackTrace();
		}
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
