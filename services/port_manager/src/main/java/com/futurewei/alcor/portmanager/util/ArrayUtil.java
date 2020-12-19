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
package com.futurewei.alcor.portmanager.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArrayUtil {
    public static <T> List<T> findCommonItems(List<T> array1, List<T> array2) {
        List<T> commonItems = new ArrayList<>();
        if (array1 == null || array2 == null) {
            return commonItems;
        }

        Iterator<T> iterator1 = array1.iterator();
        while (iterator1.hasNext()) {
            T item1 = iterator1.next();
            Iterator<T> iterator2 = array2.iterator();
            while (iterator2.hasNext()) {
                T item2 = iterator2.next();
                if (item1.equals(item2)) {
                    commonItems.add(item1);
                    break;
                }
            }
        }

        return commonItems;
    }
}
