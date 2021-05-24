package com.Utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class Logger {
    private static String LOG_PATH = "C:/Users/LenovoThinkPadT450s/OneDrive - Universität Zürich UZH/Studium/Bachelor Thesis/Repository mining/SourceCodeAnalyzer/Logs.txt";

    public Logger(){

    }

    public void addLog(String methodName, String parameters, String returnValue) throws IOException {

        FileWriter fw = new FileWriter(LOG_PATH, true);

        StringBuilder logString = new StringBuilder();

        Date logTime = new Date();
        logString.append(logTime.toString()).append("\t")
                    .append(methodName).append("\t")
                    .append(parameters).append("\t")
                    .append(returnValue).append("\n");

        fw.write(logString.toString());
        fw.close();
    }
}
