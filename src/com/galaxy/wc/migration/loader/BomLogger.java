package com.galaxy.wc.migration.loader;

import java.io.IOException;

public class BomLogger {
    private final LogStrategy logStrategy;

    public BomLogger(LogStrategy logStrategy) {
        this.logStrategy = logStrategy;
    }

    public static BomLogger getInstance(LogStrategy strategy) {
        return new BomLogger(strategy);
    }

    public void log(String message) {
        logStrategy.write(message);
    }

    public void close() throws IOException{
        logStrategy.close(); // Ensure log is flushed/closed
    }
}