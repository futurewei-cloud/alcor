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

package com.futurewei.alcor.web.json;

import com.futurewei.alcor.web.json.handler.JsonReturnFieldFilterHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
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
@ComponentScan(value="com.futurewei.alcor.web.json")
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
