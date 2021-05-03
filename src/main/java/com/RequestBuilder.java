package com;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class RequestBuilder {

    String importedClass;
    HttpURLConnection connection;
    URL requestURL;
    URLBuilder urlBuilder;

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
            e.printStackTrace();
        }
    }

    public JSONArray getDependenciesOfClass() throws IOException, ParseException {

        this.connection.connect();

        int responseCode = connection.getResponseCode();

        if (responseCode != 200) {
            return null;
        } else {

            String inline = "";
            Scanner scanner = new Scanner(this.requestURL.openStream());

            while (scanner.hasNext()) {
                inline += scanner.nextLine();
            }

            scanner.close();

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(inline);
            JSONObject response = (JSONObject) jsonObject.get("response");

//            writeToTxtFile(importedClass, inline);
            return (JSONArray) response.get("docs");


        }
    }

    private void writeToTxtFile(String className, String json) throws IOException {

        FileWriter writer = new FileWriter("debugging/" + className + ".txt");

        writer.append(json);
        writer.close();

    }
}
