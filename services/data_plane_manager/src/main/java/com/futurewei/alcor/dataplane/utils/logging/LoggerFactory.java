/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
// sudo docker stop $(sudo docker ps -a -q)
// sudo docker rm $(sudo docker ps -a -q --filter "status=exited")
// sudo docker rmi $(sudo docker images -q)
// sudo docker exec -it alcor.dataplane bash

package com.futurewei.alcor.dataplane.utils.logging;

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
