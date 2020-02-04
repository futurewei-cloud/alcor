package com.futurewei.alcor.controller.logging;

import java.util.logging.Level;

public class Log {
    private static LoggingSystemFactory factory = null;
    //private static ILogging loggingSystem = null;
    private static ConsoleLogging loggingSystem = null;
    //public static Logger logger = null;

    public Log() {
        factory = new LoggingSystemFactory();
    }

    public static void init() {
        if (factory == null)
            factory = new LoggingSystemFactory();
        loggingSystem = factory.createConsoleLogging();
    }

    public static void log(Level level, String msg) {
        if (loggingSystem == null)
            init();
        loggingSystem.log(level, msg);
    }

    public static void log(Level level, String msg, Object s) {
        if (loggingSystem == null)
            init();
        loggingSystem.log(level, msg, s.toString());
    }

    public static void log(Level level, String msg, Throwable e) {
        if (loggingSystem == null)
            init();
        loggingSystem.log(level, msg, e);
    }

    public static void entering(String sourceClass, String sourceMethod) {
        if (loggingSystem == null)
            init();
        loggingSystem.entering(sourceClass, sourceMethod);
    }

    public static void exiting(String sourceClass, String sourceMethod) {
        if (loggingSystem == null)
            init();
        loggingSystem.exiting(sourceClass, sourceMethod);
    }
}
//public class Log{
//    public static final Logger logger = java.util.logging.Logger.getGlobal();
//    public static ConsoleHandler consoleHandler = null;
//
//    public static void init()
//    {
//        try {
//            if (consoleHandler == null)
//                consoleHandler = new ConsoleHandler();
//            consoleHandler.setLevel(Level.ALL);
//            consoleHandler.setFormatter(new SimpleFormatter() {
//                @Override
//                public synchronized String format(LogRecord lr) {
//                    String strSourceClassName = lr.getSourceClassName();
//                    if (strSourceClassName == null)
//                        strSourceClassName = " ";
//                    return String.format("[%1$tF %1$tT] [%2$-7s] %4$s %6$s %7$s%n",
//                            new Date(lr.getMillis()),
//                            lr.getLevel(),
//                            (lr.getLoggerName() == null)? " " : lr.getLoggerName(),
//                            (strSourceClassName == null)? " " : strSourceClassName.substring(strSourceClassName.lastIndexOf(".")+1),
//                            (lr.getSourceMethodName() == null)? " " : lr.getSourceMethodName(),
//                            lr.getMessage(),
//                            (lr.getThrown() == null)? " " : lr.getThrown().getMessage()
//                    );
//                }
//            });
//            logger.addHandler(consoleHandler);
//            logger.setUseParentHandlers(false);
//            logger.setLevel(Level.ALL);
//        } catch(SecurityException e) {
//            e.printStackTrace();
//        }
//     }

//    public static void log(Level level, String msg)
//    {
//        if ( logger == null || consoleHandler == null)
//            init();
//        logger.log(level, msg);
//    }
//
//    public static void log(Level level, String msg, Object s)
//    {
//        if ( logger == null || consoleHandler == null)
//            init();
//        logger.log(level, msg, s.toString());
//    }
//
//    public static void log(Level level, String msg, Throwable e)
//    {
//        if ( logger == null || consoleHandler == null)
//            init();
//        logger.log(level, msg, e);
//    }
//
//    public static void entering(String sourceClass, String sourceMethod)
//    {
//        if ( logger == null || consoleHandler == null)
//            init();
//        logger.entering(sourceClass, sourceMethod);
//    }
//
//    public static void exiting(String sourceClass, String sourceMethod)
//    {
//        if ( logger == null || consoleHandler == null)
//            init();
//        logger.exiting(sourceClass, sourceMethod);
//    }
//}
