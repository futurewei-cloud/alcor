package com.futurewei.alcor.dataplane.logging;

import java.util.logging.Level;

public interface ILogger {
    void entering(String sourceClass, String sourceMethod);

    void exiting(String sourceClass, String sourceMethod);

    void log(Level level, String msg);

    void log(Level level, String msg, Object s);

    void log(Level level, String msg, Throwable e);
}
