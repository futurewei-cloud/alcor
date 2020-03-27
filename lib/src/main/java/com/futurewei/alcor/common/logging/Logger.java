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
