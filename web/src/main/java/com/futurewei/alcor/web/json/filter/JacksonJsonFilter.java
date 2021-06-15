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

package com.futurewei.alcor.web.json.filter;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.futurewei.alcor.common.utils.CommonUtil.isBaseClass;

/**
 * a filter class for filter fields from object to json str
 *
 */
@JsonFilter("JacksonFilter")
public class JacksonJsonFilter extends FilterProvider {

    private List<String> fieldNames;


    public JacksonJsonFilter(String[] fieldNames) {
        if(fieldNames != null) {
            this.fieldNames = Arrays.asList(fieldNames);
        }else{
            this.fieldNames = Collections.emptyList();
        }
    }

    @Override
    public PropertyFilter findPropertyFilter(Object filterId, Object valueToFilter) {
        return new SimpleBeanPropertyFilter(){
            @Override
            public void serializeAsField(Object pojo, JsonGenerator jgen,
                                         SerializerProvider provider, PropertyWriter writer) throws Exception {

                if (!apply(writer.getName())){
                    if(!jgen.canOmitFields()){
                        writer.serializeAsOmittedField(pojo, jgen, provider);
                    }
                    return;
                }

                if(writer.getMember().getValue(pojo) != null){
                    writer.serializeAsField(pojo, jgen, provider);
                }else{
                    JavaType type = writer.getType();
                    if(isBaseClass(type.getRawClass())){
                        //writer.serializeAsField(pojo, jgen, provider);
                        jgen.writeFieldName(writer.getName());
                        jgen.writeString("");
                    }else if(type.isCollectionLikeType()){
                        jgen.writeFieldName(writer.getName());
                        jgen.writeStartArray();
                        jgen.writeEndArray();
                    }else{
                        jgen.writeFieldName(writer.getName());
                        jgen.writeStartObject();
                        jgen.writeEndObject();
                    }
                }
            }
        };
    }

    @Override
    public BeanPropertyFilter findFilter(Object filterId) {
        throw new UnsupportedOperationException("Access to deprecated filters not supported");
    }

    public boolean apply(String name){
        if(fieldNames.isEmpty()){
            return true;
        }
        return fieldNames.contains(name);
    }
}
