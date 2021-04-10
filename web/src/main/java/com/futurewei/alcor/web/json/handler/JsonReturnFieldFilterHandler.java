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
