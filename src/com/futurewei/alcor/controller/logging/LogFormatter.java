package com.futurewei.alcor.controller.logging;

import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class LogFormatter extends SimpleFormatter {
    public LogFormatter() {
        super();
    }

    @Override
    public String format(LogRecord lr) {
        String strSourceClassName = lr.getSourceClassName();
        if (strSourceClassName == null)
            strSourceClassName = " ";
        return String.format("[%1$tF %1$tT] [%2$-7s] %4$s %6$s %7$s%n",
                new Date(lr.getMillis()),
                lr.getLevel(),
                (lr.getLoggerName() == null) ? " " : lr.getLoggerName(),
                (strSourceClassName == null) ? " " : strSourceClassName.substring(strSourceClassName.lastIndexOf(".") + 1),
                (lr.getSourceMethodName() == null) ? " " : lr.getSourceMethodName(),
                lr.getMessage(),
                (lr.getThrown() == null) ? " " : lr.getThrown().getMessage()
        );
    }
}
