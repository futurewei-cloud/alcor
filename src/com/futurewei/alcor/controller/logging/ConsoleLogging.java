package com.futurewei.alcor.controller.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsoleLogging implements ILogging {
    private static Logger logger;
    private static ConsoleHandler consoleHandler;
    private LogFormatter logFormatter;

    public ConsoleLogging() throws Exception {
        try {
            //logger = java.util.logging.Logger.getLogger("com.futurewei.alcor.controller");
            logger = java.util.logging.Logger.getGlobal();
            consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(new Level(System.getProperty("logging.level.root"));
            logFormatter = new LogFormatter();
            consoleHandler.setFormatter(logFormatter);
            logger.addHandler(consoleHandler);
            logger.setUseParentHandlers(false);
            logger.setLevel(Level.ALL);
        } catch (Exception e) {
            throw (new Exception(e.getMessage()));
        }
    }

    @Override
    public void entering(String sourceClass, String sourceMethod) {
        logger.entering(sourceClass, sourceMethod);
    }

    @Override
    public void exiting(String sourceClass, String sourceMethod) {
        logger.exiting(sourceClass, sourceMethod);
    }

    @Override
    public void log(Level level, String msg) {
        logger.log(level, msg);
    }

    @Override
    public void log(Level level, String msg, Object s) {
        logger.log(level, msg, s);
    }

    @Override
    public void log(Level level, String msg, Throwable e) {
        logger.log(level, msg, e);
    }
}
