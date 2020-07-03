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

package com.futurewei.alcor.web.json;

import com.futurewei.alcor.web.json.handler.JsonReturnFieldFilterHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link JsonReturnFieldFilterHandler} need a fixed position in a list handlers
 * So need change the order of list hanlders.
 * we set {@link JsonReturnFieldFilterHandler} before the {@link RequestResponseBodyMethodProcessor}
 *
 */
@Configuration
public class JsonHandlerConfiguration implements InitializingBean {

    @Autowired
    private RequestMappingHandlerAdapter adapter;

    @Override
    public void afterPropertiesSet(){
        List<HandlerMethodReturnValueHandler> returnValueHandlers = adapter.getReturnValueHandlers();
        List<HandlerMethodReturnValueHandler> newHandlers = new ArrayList<>();
        if(null != returnValueHandlers) {
            newHandlers.addAll(returnValueHandlers);
            int pos = getHandlerPosition(returnValueHandlers);
            if(pos != -1){
                newHandlers.add(pos, new JsonReturnFieldFilterHandler());
                adapter.setReturnValueHandlers(newHandlers);
                return;
            }
        }
        newHandlers.add(new JsonReturnFieldFilterHandler());
        adapter.setReturnValueHandlers(newHandlers);
    }

    private int getHandlerPosition(List<HandlerMethodReturnValueHandler> returnValueHandlers){
        for(int i=0;i<returnValueHandlers.size();i++){
            if(returnValueHandlers.get(i).getClass().isAssignableFrom(RequestResponseBodyMethodProcessor.class)){
                return i;
            }
        }
        return -1;
    }
}
