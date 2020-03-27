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

public interface ILogger {
    void entering(String sourceClass, String sourceMethod);

    void exiting(String sourceClass, String sourceMethod);

    void log(Level level, String msg);

    void log(Level level, String msg, Object s);

    void log(Level level, String msg, Throwable e);
}
