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

package com.futurewei.alcor.common.db.query.impl;

import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.lang.IgniteBiPredicate;

import java.util.Map;


public class MapPredicate implements IgniteBiPredicate<String, BinaryObject> {

    private final Map<String, Object[]> params;

    public static MapPredicate getInstance(Map<String, Object[]> params){
        return new MapPredicate(params);
    }

    public MapPredicate(Map<String, Object[]> params){
        this.params = params;
    }

    @Override
    public boolean apply(String k, BinaryObject v) {
        boolean matched = true;
        for(Map.Entry<String, Object[]> entry: params.entrySet()){
            if(!v.hasField(entry.getKey())){
                continue;
            }

            boolean fieldMatch = false;
            for(Object obj: entry.getValue()){
                fieldMatch |= obj.equals(v.field(entry.getKey()));
            }

            matched &= fieldMatch;
        }
        return matched;
    }
}
