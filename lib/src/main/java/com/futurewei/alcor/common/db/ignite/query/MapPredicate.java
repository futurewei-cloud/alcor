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

package com.futurewei.alcor.common.db.ignite.query;

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
