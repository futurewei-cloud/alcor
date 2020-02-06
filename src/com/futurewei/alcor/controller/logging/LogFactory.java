package com.futurewei.alcor.controller.logging;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.logging.Level;

public class LogFactory {
    public static Log alcorLog = null;
    private static Properties properties;

    public LogFactory() {
        properties = new Properties();
    }

    public static Log getLog() {
        if (alcorLog == null)
            createLog();
        return alcorLog;
    }

    public static Log createLog() {
        try {
            readLogProperties();
            Level logLevel = Level.parse(properties.getProperty("logging.level.root"));
            switch (properties.getProperty("logging.type")) {
                case "console":
                    alcorLog = createConsoleLog(logLevel);
                    break;
                case "file":
                    String strPath = System.getProperty("user.dir");
                    String strDir = properties.getProperty("logging.file.path");
                    alcorLog = createFileLog(logLevel, strDir);
                    break;
                default:
                    alcorLog = createConsoleLog(logLevel);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return alcorLog;
    }

    private static ConsoleLog createConsoleLog(Level logLevel) {
        ConsoleLog consoleLog = null;
        try {
            consoleLog = new ConsoleLog(logLevel);
        } catch (Exception e) {
            System.out.println("Fail: Console Logging System Creation");
        }
        return consoleLog;
    }

    private static FileLog createFileLog(Level logLevel, String strDir) {
        FileLog fileLog = null;
        try {
            fileLog = new FileLog(logLevel, strDir);
        } catch (Exception e) {
            System.out.println("Fail: File Logging System Creation" + strDir);
        }
        return fileLog;
    }

    private static void readLogProperties() throws Exception {
        String strPath = System.getProperty("user.dir");
        String strFileName = strPath + "/resources/application.properties";

        properties = new Properties();

        try {
            FileInputStream inputStream = new FileInputStream(strFileName);
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new FileNotFoundException("property file" + strFileName);
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
