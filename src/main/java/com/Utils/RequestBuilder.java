package com.Utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class RequestBuilder {

    private String importedClass;
    private HttpURLConnection connection;
    private URL requestURL;
    private URLBuilder urlBuilder;

    public RequestBuilder(){
    }

    public void setImportedClass(String importedClass) {
        this.importedClass = importedClass;
    }

    public void createConnection() {
        try{
            this.urlBuilder = new URLBuilder(this.importedClass);
            this.requestURL = urlBuilder.getRequestURL();
            this.connection = (HttpURLConnection) this.requestURL.openConnection();
            this.connection.setRequestMethod("GET");
            this.connection.setConnectTimeout(5000);
            this.connection.setReadTimeout(60*1000);
        } catch (Exception e){
            System.out.println(this.importedClass);
            e.printStackTrace();
        }
    }

    public JSONArray getDependenciesOfClass() throws IOException, ParseException {

        long timestamp = System.currentTimeMillis();
        this.connection.connect();

        int responseCode = connection.getResponseCode();

        if (responseCode != 200) {
            return null;
        } else {

            String inline = "";
            try{
                Scanner scanner = new Scanner(this.requestURL.openStream());
                while (scanner.hasNext()) {
                    inline += scanner.nextLine();
                }

                scanner.close();
            } catch (Exception e){
                System.out.println("Could not " + this.importedClass + "connection timeout");
                e.printStackTrace();
            }


            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(inline);
            JSONObject response = (JSONObject) jsonObject.get("response");


//            System.out.println("Time to process request: " + (System.currentTimeMillis() - timestamp)/1000);
            return (JSONArray) response.get("docs");

        }
    }
}
