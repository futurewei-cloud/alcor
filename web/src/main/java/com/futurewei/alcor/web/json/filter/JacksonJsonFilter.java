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
                        writer.serializeAsField(pojo, jgen, provider);
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
