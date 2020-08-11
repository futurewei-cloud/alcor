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

package com.futurewei.alcor.web.rbac.aspect;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.TokenEntity;
import com.futurewei.alcor.common.exception.ParseObjectException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.rbac.RbacMangerInterface;
import com.futurewei.alcor.common.utils.ControllerUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Aspect
@Component
public class RbacAspect {
    private static final Logger LOG = LoggerFactory.getLogger(RbacAspect.class);

    private static final String QUERY_PROJECT_NAME = "project_id";
    private static final String FIELD_PARAM_NAME = "fields";
    private static final String TOKEN_INFO_HEADER = "X-Token-info";

    private static final String POST_METHOD_NAME = "POST";
    private static final String PUT_METHDO_NAME = "PUT";
    private static final String DELETE_METHDO_NAME = "DELETE";
    private static final String GET_METHDO_NAME = "GET";

    @Autowired
    private OwnerCheckerSupplier ownerCheckerSupplier;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private RbacMangerInterface rbacManger;

    @Pointcut("@annotation(com.futurewei.alcor.web.rbac.aspect.Rbac)")
    public void annotationPointCut(){}

    @Around("annotationPointCut()")
    public Object checkRbac(ProceedingJoinPoint pjp) throws Throwable {

        String methodType = request.getMethod().toUpperCase();
        String tokenInfo = request.getHeader(TOKEN_INFO_HEADER);
        MethodSignature ms = (MethodSignature)pjp.getSignature();
        Rbac rbac = ms.getMethod().getAnnotation(Rbac.class);
        String resourceName = rbac.name();

        Optional<TokenEntity> tokenEntityOptional = ControllerUtil.getUserTokenInfo(tokenInfo);
        if (tokenEntityOptional.isPresent()) {
            // process token roles
            TokenEntity tokenEntity = tokenEntityOptional.get();

            switch (methodType) {
                case POST_METHOD_NAME: {
                    List<String> fields = getObjectFields(ms, pjp.getArgs());
                    processCreate(resourceName, tokenEntity, fields);
                    break;
                }
                case PUT_METHDO_NAME: {
                    List<String> fields = getObjectFields(ms, pjp.getArgs());
                    processUpdate(resourceName, tokenEntity, fields);
                    break;
                }
                case DELETE_METHDO_NAME:
                    processDelete(resourceName, tokenEntity);
                    break;
                case GET_METHDO_NAME: {
                    String[] fields = getQueryFields();
                    processGet(resourceName, tokenEntity, fields);
                    processAdminQuery(resourceName, tokenEntity);
                    break;
                }
                default:
                    LOG.warn("rbac get unknown http method type {}", methodType);
                    break;
            }
        }

        // execute real controller method
        Object object = pjp.proceed();

        if (GET_METHDO_NAME.equals(methodType) && tokenEntityOptional.isPresent()) {
            processGetReturn(resourceName, tokenEntityOptional.get(), object);
        }

        return object;
    }

    private void processGetReturn(String resourceName, TokenEntity tokenEntity,
                                  Object obj) {
        Class<?> objClass = obj.getClass();
        Field[] fields = objClass.getDeclaredFields();
        if (fields.length != 1) {
            LOG.warn("process http get method return object failed, return class have no fields or more than one field");
            return;
        }

        try {
            Field field = fields[0];
            field.setAccessible(true);
            Object realObj = field.get(obj);
            if (realObj instanceof List) {
                rbacManger.processListExcludeFields(resourceName, tokenEntity, ownerCheckerSupplier.getOwnerChecker(),
                        (List)realObj);
            } else {
                rbacManger.processGetExcludeFields(resourceName, tokenEntity, ownerCheckerSupplier.getOwnerChecker(),
                        realObj);
            }
        } catch (IllegalAccessException | ParseObjectException e) {
            LOG.warn("process http get method return object failed, {}", e.getMessage());
        }
    }

    private void processAdminQuery(String resourceName, TokenEntity tokenEntity) {
        Map<String, String[]> queryParams = request.getParameterMap();
        if (rbacManger.isAdmin(resourceName, tokenEntity)) {
            queryParams.put(QUERY_PROJECT_NAME, new String[]{tokenEntity.getProjectId()});
        }
    }

    private String[] getQueryFields() {
        return request.getParameterValues(FIELD_PARAM_NAME);
    }

    private List<String> getObjectFields(MethodSignature ms, Object[] args) throws IOException {

        int i = getBodyClass(ms);
        if (i == -1) {
            return null;
        }

        List<String> requestParams = new ArrayList<>();
        try {
            Object arg = args[i];
            Field[] fields = ControllerUtil.getAllDeclaredFields(arg.getClass());
            if (fields.length > 1) {
                LOG.warn("process http post/put method return object failed, request body not invalid");
                return null;
            }
            Field objField = fields[0];
            objField.setAccessible(true);
            Object subObj = objField.get(arg);
            if (subObj == null) {
                return null;
            }
            Field[] subFields = ControllerUtil.getAllDeclaredFields(subObj.getClass());
            for (Field field: subFields) {
                field.setAccessible(true);
                Object subFieldObj = field.get(subObj);
                if (subFieldObj != null) {
                    if (field.getType().equals(boolean.class) && !(boolean) subFieldObj) {
                        continue;
                    }
                    JsonProperty jsonAnnotate = field.getAnnotation(JsonProperty.class);
                    String fieldName = jsonAnnotate == null ? field.getName() : jsonAnnotate.value();
                    requestParams.add(fieldName);
                }
            }
        } catch (IllegalAccessException e) {
            LOG.warn("process http post/put method return object failed, {}", e.getMessage());
        }
        return requestParams;
    }

    private int getBodyClass(MethodSignature ms) {
        Method method = ms.getMethod();
        Annotation[][] paramsAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < paramsAnnotations.length; i++) {
            for (Annotation annotation: paramsAnnotations[i]) {
                if (annotation instanceof RequestBody) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void processGet(String resourceName, TokenEntity tokenEntity,
                            String[] getFields) throws ResourceNotFoundException {
        rbacManger.checkGet(resourceName, tokenEntity, getFields, ownerCheckerSupplier.getOwnerChecker());
    }

    private void processDelete(String resourceName, TokenEntity tokenEntity) throws ResourceNotFoundException {
        rbacManger.checkDelete(resourceName, tokenEntity, ownerCheckerSupplier.getOwnerChecker());
    }

    private void processUpdate(String resourceName, TokenEntity tokenEntity,
                               List<String> bodyFields) throws ResourceNotFoundException {
        rbacManger.checkUpdate(resourceName, tokenEntity, bodyFields, ownerCheckerSupplier.getOwnerChecker());
    }

    private void processCreate(String resourceName, TokenEntity tokenEntity,
                               List<String> bodyFields) throws ResourceNotFoundException {
        rbacManger.checkCreate(resourceName, tokenEntity, bodyFields, ownerCheckerSupplier.getOwnerChecker());
    }

}
