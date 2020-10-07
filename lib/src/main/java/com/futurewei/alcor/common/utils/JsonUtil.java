/*
 *
 * Copyright 2019 The Alcor Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * /
 */

package com.futurewei.alcor.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static <T> T readValue(String jsonStr, Class<T> tClass) throws IOException {
        return OBJECT_MAPPER.readValue(jsonStr, tClass);
    }

    public static <T> T readValue(File jsonFile, Class<T> tClass) throws IOException {
        return OBJECT_MAPPER.readValue(jsonFile, tClass);
    }

    public static <T> T readValue(InputStream is, Class<T> tClass) throws IOException {
        return OBJECT_MAPPER.readValue(is, tClass);
    }

    public static String writeValueAsString(Object obj) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }

    public ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

}
