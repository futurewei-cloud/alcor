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
package com.futurewei.alcor.controller.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;

public class FileLogger extends Logger {
    private static FileHandler fileHandler;

    public FileLogger(Level level, String strDir) throws Exception {
        try {
            String strFileName = makeLogFileName();
            fileHandler = new FileHandler(strDir + "/" + strFileName, 1024 * 1024 * 10, 96, true);
            fileHandler.setLevel(level);
            fileHandler.setFormatter(logFormatter);
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
            logger.setLevel(level);
        } catch (IllegalArgumentException e) {
            throw (new IllegalArgumentException("application.properties: invalid log level"));
        } catch (Exception e) {
            throw (new Exception(e.getMessage()));
        }
    }

    private String makeLogFileName() {
        String strFileName = "AlcorLog-";
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        strFileName = strFileName.concat(dateFormat.format(new Date())).concat(".log");
        return strFileName;
    }
}
