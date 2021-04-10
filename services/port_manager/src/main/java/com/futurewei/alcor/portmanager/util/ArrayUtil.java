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

    public static <T> List<T> findCommonItemsNew(List<T> array1, List<T> array2) {
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
                    iterator1.remove();
                    iterator2.remove();
                }
            }
        }

        return commonItems;
    }
}
