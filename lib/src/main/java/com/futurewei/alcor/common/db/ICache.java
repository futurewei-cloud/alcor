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

package com.futurewei.alcor.common.db;

import java.util.Map;
import java.util.Set;

public interface ICache<K, V> {
    V get(K var1) throws CacheException;

    /**
     * Get Cache value from cache db by multi params
     *
     * @param filterParams a map of params name and value
     * @return cache value
     * @throws CacheException if any exception
     */
    V get(Map<String, Object[]> filterParams) throws CacheException;

    void put(K var1, V var2) throws CacheException;

    /**
     * Atmoic put entry
     *
     * @param var1 key
     * @param var2 value
     * @return true if no exist false if existed
     * @throws CacheException
     */
    Boolean putIfAbsent(K var1, V var2) throws CacheException;

    boolean containsKey(K var1) throws CacheException;

    /**
     * Get Cache multi keys
     *
     * @param keys
     * @return
     * @throws CacheException
     */
    Map<K, V> getAll(Set<K> keys) throws CacheException;

    Map<K, V> getAll() throws CacheException;

    /**
     * Get Cache values from cache db by multi params
     *
     * @param filterParams a map of params name and value
     * @return cache value
     * @throws CacheException if any exception
     */
    <E1, E2> Map<K, V> getAll(Map<String, Object[]> filterParams) throws CacheException;

    void putAll(Map<? extends K, ? extends V> var1) throws CacheException;

    boolean remove(K var1) throws CacheException;

    /**
     * db cache size
     *
     * @return
     */
    long size();

    Transaction getTransaction();
}