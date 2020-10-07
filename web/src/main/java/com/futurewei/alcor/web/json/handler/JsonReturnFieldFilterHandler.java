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

package com.futurewei.alcor.web.json.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.web.json.annotation.FieldFilter;
import com.futurewei.alcor.web.json.filter.JacksonJsonFilter;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * in spring mvc, it use {@link HandlerMethodReturnValueHandler} to handler controller return value
 * in this handler, we return a entity json str which has few required fields
 *
 */
public class JsonReturnFieldFilterHandler implements HandlerMethodReturnValueHandler {

    private static final String FIELD_PARAM_NAME = "fields";

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return returnType.getMethodAnnotation(FieldFilter.class) != null;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        // set finial handler process it
        mavContainer.setRequestHandled(true);

        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        assert request != null;
        String[] fields = request.getParameterValues(FIELD_PARAM_NAME);

        assert response != null;
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // get method annotation and get entity type from annotation
        FieldFilter fieldFilter = returnType.getMethodAnnotation(FieldFilter.class);
        assert fieldFilter != null;
        String returnJsonValue = serializerReturnValue(returnValue, fields, fieldFilter.type());
        response.getWriter().write(returnJsonValue);

    }

    private String serializerReturnValue(Object returnValue, String[] fields,
                                         Class<?> clasz) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        // if no fields param in request params, ignore it
        JacksonJsonFilter jacksonJsonFilter = new JacksonJsonFilter(fields);
        mapper.setFilterProvider(jacksonJsonFilter);
        mapper.addMixIn(clasz, JacksonJsonFilter.class);
        return mapper.writeValueAsString(returnValue);
    }
}
