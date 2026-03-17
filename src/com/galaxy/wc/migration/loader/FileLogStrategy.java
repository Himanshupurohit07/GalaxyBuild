package com.galaxy.wc.migration.loader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class FileLogStrategy implements LogStrategy {
    private BufferedWriter writer;

    public FileLogStrategy(String logFilePath) throws IOException {
        writer = new BufferedWriter(new FileWriter(logFilePath));
    }

    public void write(String message) {
        try {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(String message){
        write(message);
    }

}