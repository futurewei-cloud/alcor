/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
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
