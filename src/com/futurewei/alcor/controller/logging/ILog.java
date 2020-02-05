package com.futurewei.alcor.controller.logging;

import java.util.logging.Level;

public interface ILog {
    void entering(String sourceClass, String sourceMethod);

    void exiting(String sourceClass, String sourceMethod);

    void log(Level level, String msg);

    void log(Level level, String msg, Object s);

    void log(Level level, String msg, Throwable e);
}
