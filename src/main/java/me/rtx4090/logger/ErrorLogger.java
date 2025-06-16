package me.rtx4090.logger;

import java.nio.file.Path;

public class ErrorLogger {
    Path logFilePath;

    public ErrorLogger(Path logFilePath) {
        this.logFilePath = logFilePath;
    }
}
