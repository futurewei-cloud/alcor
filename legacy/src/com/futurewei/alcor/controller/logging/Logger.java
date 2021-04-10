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

import java.util.logging.Level;

public class Logger implements ILogger {
    protected static java.util.logging.Logger logger;
    protected LogFormatter logFormatter;

    public Logger() {
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
