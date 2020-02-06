package com.futurewei.alcor.controller.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log implements ILog {
    protected static Logger logger;
    protected LogFormatter logFormatter;

    public Log() {
        logger = java.util.logging.Logger.getGlobal();
        logFormatter = new LogFormatter();
    }

    public void log(Level level, String msg) {
        logger.log(level, msg);
    }

    public void log(Level level, String msg, Object s) {
        logger.log(level, msg, s.toString());
    }

    public void log(Level level, String msg, Throwable e) {
        logger.log(level, msg, e);
    }

    public void entering(String sourceClass, String sourceMethod) {
        logger.entering(sourceClass, sourceMethod);
    }

    public void exiting(String sourceClass, String sourceMethod) {
        logger.exiting(sourceClass, sourceMethod);
    }
}
