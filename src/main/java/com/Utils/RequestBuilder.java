package com.Utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Scanner;

public class RequestBuilder {

    private String importedClass;
    private String groupId;
    private String artifactId;
    private HttpURLConnection connection;
    private URL requestURL;
    private URLBuilder urlBuilder;

    public RequestBuilder(){
    }

    public void setImportedClass(String importedClass) {
        this.importedClass = importedClass;
    }

    public void setImportedClassGroupIdArtifactId(String importedClass, String groupId, String artifactId){
        this.importedClass = "fc:" + importedClass;
        this.groupId = "g:" + groupId;
        this.artifactId = "a:" + artifactId;
    }

    public void createConnection() {
        try{
            this.urlBuilder = new URLBuilder(this.importedClass + "%20AND%20" + this.groupId + "%20AND%20" + this.artifactId);
            this.requestURL = urlBuilder.getRequestURL();
            this.connection = (HttpURLConnection) this.requestURL.openConnection();
            this.connection.setRequestMethod("GET");
            this.connection.setConnectTimeout(1000);
            this.connection.setReadTimeout(5000);
        } catch (Exception e){
            System.out.println(this.importedClass);
            e.printStackTrace();
        }
    }

    public JSONArray getDependenciesOfClass() throws IOException, ParseException {

        long timestamp = System.currentTimeMillis();
        this.connection.connect();
        try{
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
                    System.out.println("Could not request for: " + this.importedClass + "connection timeout");
                    e.printStackTrace();
                    return new JSONArray();
                }


                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(inline);
                JSONObject response = (JSONObject) jsonObject.get("response");


//            System.out.println("Time to process request: " + (System.currentTimeMillis() - timestamp)/1000);
                return (JSONArray) response.get("docs");

            }
        } catch (SocketTimeoutException e){
            return new JSONArray();
        }

    }
}
