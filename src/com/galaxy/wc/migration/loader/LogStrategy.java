package com.galaxy.wc.migration.loader;

import java.io.IOException;

public interface LogStrategy {
    void log(String message);
    void close() throws IOException;
    public void write(String message);
}
