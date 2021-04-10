/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

// sudo docker stop $(sudo docker ps -a -q)
// sudo docker rm $(sudo docker ps -a -q --filter "status=exited")
// sudo docker rmi $(sudo docker images -q)
// sudo docker exec -it alcor-controller bash

package com.futurewei.alcor.controller.logging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.logging.Level;

@Component
public class LoggerFactory {
    public static Logger logger = new Logger();
    public static String loggingLevel;
    public static String loggingFilePath;
    public static String loggingType;
    private static boolean bLogConfig = false;

    public LoggerFactory() {
    }

    public static Logger getLogger() {
        try {
            if (bLogConfig == false) {
                createLogger();
                bLogConfig = true;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed: Log System Creation & Returning to Default Log", e);
        }
        return logger;
    }

    private static ConsoleLogger createConsoleLogger(Level logLevel) {
        ConsoleLogger consoleLog = null;
        try {
            consoleLog = new ConsoleLogger(logLevel);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Fail: Console Logging System Creation", e);
        }
        return consoleLog;
    }

    private static FileLogger createFileLogger(Level logLevel, String strDir) {
        FileLogger fileLogger = null;
        try {
            fileLogger = new FileLogger(logLevel, strDir);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Fail: File Logging System Creation" + strDir, e);
        }
        return fileLogger;
    }

    public static void createLogger() {
        try {
            Level logLevel = Level.parse(loggingLevel.toUpperCase());
            switch (loggingType) {
                case "console":
                    logger = createConsoleLogger(logLevel);
                    break;
                case "file":
                    logger = createFileLogger(logLevel, loggingFilePath);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed: Log Configuration");
        }
    }

    @Value("${logging.level.root:'info'}")
    public void setLoggingLevel(String strLoggingLevel) {
        loggingLevel = strLoggingLevel;
    }

    @Value("${logging.file.path:'/var/log'}")
    public void setLoggingFilepath(String strLoggingFilePath) {
        loggingFilePath = strLoggingFilePath;
    }

    @Value("${logging.type:'console'}")
    public void setLoggingType(String strLoggingType) {
        loggingType = strLoggingType;
    }
}
