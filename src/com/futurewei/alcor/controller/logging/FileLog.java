package com.futurewei.alcor.controller.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;

public class FileLog extends Log {
    private static FileHandler fileHandler;

    public FileLog(Level level, String strDir) throws Exception {
        try {
            String strFileName = makeLogFileName();
            fileHandler = new FileHandler(strDir + strFileName, 1024, 96, true);
            fileHandler.setLevel(level);
            fileHandler.setFormatter(logFormatter);
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
            logger.setLevel(level);
        } catch (IllegalArgumentException e) {
            throw (new IllegalArgumentException("application.properties: invalid log level"));
        } catch (Exception e) {
            throw (new Exception(e.getMessage()));
        }
    }

    private String makeLogFileName() {
        String strFileName = "AlcorLog-";
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        strFileName = strFileName.concat(dateFormat.format(new Date())).concat(".log");
        return strFileName;
    }
}
