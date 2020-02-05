package com.futurewei.alcor.controller.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

public class ConsoleLog extends Log {
    private static ConsoleHandler consoleHandler;

    public ConsoleLog(Level level) throws Exception {
        try {
            consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(level);
            consoleHandler.setFormatter(logFormatter);
            logger.addHandler(consoleHandler);
            logger.setUseParentHandlers(false);
            logger.setLevel(level);
        } catch (IllegalArgumentException e) {
            throw (new IllegalArgumentException("application.properties: invalid log level"));
        } catch (Exception e) {
            throw (new Exception(e.getMessage()));
        }
    }
}
