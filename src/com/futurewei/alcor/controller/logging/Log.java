package com.futurewei.alcor.controller.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {

    public static final Logger logger = java.util.logging.Logger.getGlobal();
    public static ConsoleHandler consoleHandler = null;

    public static void init()
    {
        try {
            if (consoleHandler == null)
                consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            logger.addHandler(consoleHandler);
            logger.setUseParentHandlers(false);
            logger.setLevel(Level.ALL);
        } catch(SecurityException e) {
            e.printStackTrace();
        }
     }

    public static void log(Level level, String msg)
    {
        if ( logger == null || consoleHandler == null)
            init();
        logger.log(level, msg);
    }

    public static void log(Level level, String msg, Object s)
    {
        if ( logger == null || consoleHandler == null)
            init();
        logger.log(level, msg, s.toString());
    }

    public static void log(Level level, String msg, Throwable e)
    {
        if ( logger == null || consoleHandler == null)
            init();
        logger.log(level, msg, e);
    }

    public static void entering(String sourceClass, String sourceMethod)
    {
        if ( logger == null || consoleHandler == null)
            init();
        logger.entering(sourceClass, sourceMethod);
    }

    public static void exiting(String sourceClass, String sourceMethod)
    {
        if ( logger == null || consoleHandler == null)
            init();
        logger.exiting(sourceClass, sourceMethod);
    }
}
