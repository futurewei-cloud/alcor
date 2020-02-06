package com.futurewei.alcor.controller.logging;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.logging.Level;

public class LogFactory {
    public static Log alcorLog = new Log();
    private static Properties properties = new Properties();
    private static boolean bLogCreated = false;

    public LogFactory() {
    }

    public static Log getLog() {
        try {
            if (bLogCreated == false) {
                createLog();
                bLogCreated = true;
            }
        } catch (Exception e) {
            alcorLog.log(Level.WARNING, "Failed: Log System Creation & Returning to Default Log", e);
        }
        return alcorLog;
    }

    public static void createLog() {
        try {
            readLogProperties();
            Level logLevel = Level.parse(properties.getProperty("logging.level.root"));
            switch (properties.getProperty("logging.type")) {
                case "console":
                    alcorLog = createConsoleLog(logLevel);
                    break;
                case "file":
                    //String strPath = System.getProperty("user.dir");
                    String strDir = properties.getProperty("logging.file.path");
                    alcorLog = createFileLog(logLevel, strDir);
                    break;
                default:
                    alcorLog = createConsoleLog(logLevel);
                    break;
            }
        } catch (Exception e) {
            alcorLog.log(Level.WARNING, "Failed: Log Configuration", e);
        }
    }

    private static ConsoleLog createConsoleLog(Level logLevel) {
        ConsoleLog consoleLog = null;
        try {
            consoleLog = new ConsoleLog(logLevel);
        } catch (Exception e) {
            alcorLog.log(Level.WARNING, "Fail: Console Logging System Creation", e);
        }
        return consoleLog;
    }

    private static FileLog createFileLog(Level logLevel, String strDir) {
        FileLog fileLog = null;
        try {
            fileLog = new FileLog(logLevel, strDir);
        } catch (Exception e) {
            alcorLog.log(Level.WARNING, "Fail: File Logging System Creation" + strDir, e);
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
