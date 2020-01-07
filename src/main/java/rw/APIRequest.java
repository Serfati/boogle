package rw;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class APIRequest {

    public static void main(String[] args) throws IOException {
        APIRequest api = new APIRequest();
        api.dataBase("https://api.datamuse.com/sug?s=isra");
    }

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

    public void dataBase(String apiURL) throws IOException {

        APIRequest request = new APIRequest();
        JSONObject details = request.post(apiURL);
        JSONArray result = details.getJSONArray("result");

        for(int i = 1; i < result.length() && i < 5; i++) {
            JSONObject data = (JSONObject) result.get(i);
            String word = data.get("word").toString();
            String score = data.get("score").toString();
            System.out.println(word+":"+score);
        }
    }
}