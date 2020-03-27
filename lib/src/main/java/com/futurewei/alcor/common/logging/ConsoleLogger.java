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
package com.futurewei.alcor.common.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

public class ConsoleLogger extends Logger {
    private static ConsoleHandler consoleHandler;

    public ConsoleLogger(Level level) throws Exception {
        try {
            consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(level);
            consoleHandler.setFormatter(logFormatter);
            logger.addHandler(consoleHandler);
            logger.setUseParentHandlers(false);
            logger.setLevel(level);
        } catch (IllegalArgumentException e) {
            throw (new IllegalArgumentException("application.yml: invalid log level"));
        } catch (Exception e) {
            throw (new Exception(e.getMessage()));
        }
    }
}
