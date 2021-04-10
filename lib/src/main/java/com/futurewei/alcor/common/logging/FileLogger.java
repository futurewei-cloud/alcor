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

package com.futurewei.alcor.common.logging;

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
            throw (new IllegalArgumentException("application.yml: invalid log level"));
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
