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

package com.futurewei.alcor.common.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.TokenEntity;
import com.futurewei.alcor.common.exception.QueryParamTypeNotSupportException;
import com.google.common.collect.ObjectArrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class ControllerUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerUtil.class);

    private static final String PROTECT_FIELD_NAME = "fields";
    public static final String TOKEN_INFO_HEADER = "X-Token-Info";

    private static String GenereateMacAddress(int index) {
        return "0e:73:ae:c8:" + Integer.toHexString((index + 6) / 256) + ":" + Integer.toHexString((index + 6) % 256);
    }

    private static String GenereateIpAddress(int index) {
        return "10.0." + (index + 6) / 256 + "." + (index + 6) % 256;
    }

    public static boolean isPositive(String val) {
        if (val == null || val.length() == 0) {
            return false;
        }
        int n = val.length();
        if (n == 0 || n > 3) {  // ex. 127.0.0.00001 is wrong
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (!Character.isDigit(val.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * get token user info from http request header
     * @param tokenInfo the json token form http request header "X-Token-Info"
     * @return token info
     */
    public static Optional<TokenEntity> getUserTokenInfo(String tokenInfo) {
        if(StringUtils.isEmpty(tokenInfo)){
            return Optional.empty();
        }

        try{
            TokenEntity tokenEntity = JsonUtil.readValue(tokenInfo, TokenEntity.class);
            return Optional.of(tokenEntity);
        } catch (IOException e) {
            LOG.error("handle token user roles failed ", e);
        }
        return Optional.empty();
    }

    /**
     * get token user info from http request header
     * @param tokenEntity token info object
     * @return token info
     */
    public static boolean isAdmin(TokenEntity tokenEntity) {
        String adminRole = "admin";
        List<String> roles = tokenEntity.getRoles();
        return roles != null && roles.contains(adminRole);
    }

    /**
     * handle openstack user token roles, if admin role no need add filter project id
     * if other role, we should ensure the user get resource own to self
     * @param tokenInfo the json token form http request header "X-Token-Info"
     * @param params the query params
     */
    public static void handleUserRoles(String tokenInfo, Map<String, Object[]> params) {

        if(StringUtils.isEmpty(tokenInfo)){
            return;
        }

        TokenEntity tokenEntity = null;
        try{
            tokenEntity = JsonUtil.readValue(tokenInfo, TokenEntity.class);
        } catch (IOException e) {
            LOG.error("handle token user roles failed ", e);
            return;
        }
        String adminRole = "admin";
        List<String> roles = tokenEntity.getRoles();
        if(roles == null || !roles.contains(adminRole)){
            params.put("projectId", new String[]{tokenEntity.getProjectId()});
        }
    }

    /**
     * transform url query params to Map<String, Object[]></>
     *
     * @param params the request params,
     * @param modelClass the entity class
     * @return Map<String, Object>
     */
    public static <T> Map<String, Object[]> transformUrlPathParams(Map<String, String[]> params, Class<T> modelClass)
    throws QueryParamTypeNotSupportException {
        Map<String, Object[]> queryParams = new HashMap<>(params.size());
        Field[] fields = getAllDeclaredFields(modelClass);
        Map<String, Field> fieldMap = new HashMap<>(fields.length*2);
        for(Field field: fields){
            JsonProperty jsonAnnotate = field.getAnnotation(JsonProperty.class);
            if(jsonAnnotate != null){
                fieldMap.put(jsonAnnotate.value(), field);
            }else{
                fieldMap.put(field.getName(), field);
            }
        }
        for(Map.Entry<String, String[]> entry: params.entrySet()){
            String queryName = entry.getKey();
            String[] queryValues = entry.getValue();
            if(queryValues.length <= 0 || PROTECT_FIELD_NAME.equals(queryName)){
                continue;
            }

            if(fieldMap.containsKey(queryName)){
                Field field = fieldMap.get(queryName);

                Object[] values = new Object[queryValues.length];
                for(int i=0;i<queryValues.length;i++){
                    values[i] = castStr(queryValues[i], field.getType());
                }
                queryParams.put(field.getName(), values);
            }
        }
        return queryParams;
    }

    /**
     * transform auery params to url</>
     *
     * @param params the request params,
     * @return String
     */
    public static String transformParamsToUrl(Map<String, Object[]> params) {

        if (params.isEmpty()) {
            return "";
        }

        StringBuilder filterStrBuilder = new StringBuilder();
        filterStrBuilder.append("?");
        for (String filterKey: params.keySet()) {
            Object[] filterValueSet = params.get(filterKey);
            for (Object filterValue: filterValueSet) {
                filterStrBuilder.append(filterKey);
                filterStrBuilder.append("=");
                filterStrBuilder.append(filterValue.toString());
                filterStrBuilder.append("&");
            }
        }

        // remove "&" in the tail
        return filterStrBuilder.substring(0, filterStrBuilder.length() - 1);
    }

    public static <T> Field[] getAllDeclaredFields(Class<T> tClass){
        Field[] selfFields = tClass.getDeclaredFields();
        Class<? super T> superClass = tClass.getSuperclass();
        if(superClass != null && !superClass.getName().equals(Object.class.getName())) {
            return ObjectArrays.concat(selfFields, getAllDeclaredFields(superClass), Field.class);
        }
        return selfFields;
    }

    private static <T> Object castStr(String str, Class<T> tClass) throws QueryParamTypeNotSupportException{
        String className = tClass.getName();
        if(className.equals(String.class.getName())){
            return str;
        }else if(className.equals(boolean.class.getName()) || className.equals(Boolean.class.getName())){
            return Boolean.valueOf(str);
        }else if(className.equals(Integer.class.getName()) || className.equals(int.class.getName())){
            return Integer.valueOf(str);
        }else{
            throw new QueryParamTypeNotSupportException("query param not support type: " + className);
        }
    }

}
