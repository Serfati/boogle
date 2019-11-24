package Model.Engine;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class APIRequest {

    /**
     * this function addresses the url given and gets the data from the web
     *
     * @param url the url to address to
     * @return JSONObject containing all data
     * @throws IOException .
     */
    public JSONObject post(String url) throws IOException {
        URL address = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) address.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        StringBuilder sb = new StringBuilder("{\"result\":");
        Scanner scan = new Scanner(address.openStream());
        while(scan.hasNext())
            sb.append(scan.nextLine());
        scan.close();
        sb.append("}");
        return new JSONObject(sb.toString());
    }
}