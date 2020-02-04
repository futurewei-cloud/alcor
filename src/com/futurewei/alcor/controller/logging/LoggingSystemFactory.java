package com.futurewei.alcor.controller.logging;

public class LoggingSystemFactory {
    public LoggingSystemFactory() {
    }

    public ConsoleLogging createConsoleLogging() {
        ConsoleLogging consoleLogging = null;
        try {
            consoleLogging = new ConsoleLogging();
        } catch (Exception e) {
            System.out.println("Fail: Console Logging System Creation");
        }
        return consoleLogging;
    }

    public FileLogging createFileLog() {
        FileLogging fileLogging = null;
        try {
            fileLogging = new FileLogging();
        } catch (Exception e) {
            System.out.println("Fail: File Logging System Creation");
        }
        return fileLogging;
    }
}
