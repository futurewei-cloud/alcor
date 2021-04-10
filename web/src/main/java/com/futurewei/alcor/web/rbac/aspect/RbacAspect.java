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

package com.futurewei.alcor.web.rbac.aspect;

import ch.qos.logback.core.subst.Token;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.TokenEntity;
import com.futurewei.alcor.common.exception.PolicyNotAuthorizedException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.rbac.RbacManger;
import com.futurewei.alcor.common.utils.ControllerUtil;
import com.futurewei.alcor.web.exception.NotAuthorizedException;
import com.futurewei.alcor.web.exception.NotFoundException;
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
import java.util.*;

import static com.futurewei.alcor.common.constants.CommonConstants.*;

/**
 *  An aspect pointcut for rbac control. It handle all have {@link com.futurewei.alcor.web.rbac.aspect.Rbac @Rbac}
 *  annotation methods.
 */
@Aspect
@Component
public class RbacAspect {
    private static final Logger LOG = LoggerFactory.getLogger(RbacAspect.class);

    private static final String POST_METHOD_NAME = "POST";
    private static final String PUT_METHDO_NAME = "PUT";
    private static final String DELETE_METHDO_NAME = "DELETE";
    private static final String GET_METHDO_NAME = "GET";

    @Autowired
    private OwnerCheckerSupplier ownerCheckerSupplier;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private RbacManger rbacManger;

    @Pointcut("@annotation(com.futurewei.alcor.web.rbac.aspect.Rbac)")
    public void annotationPointCut(){}

    /**
     * check request header token info and check token roles for access method.
     *
     * @param pjp real execute method
     * @return object real method return
     * @throws Throwable if check roles permissions failed throw exception
     */
    @Around("annotationPointCut()")
    public Object checkRbac(ProceedingJoinPoint pjp) throws Throwable {

        String methodType = request.getMethod().toUpperCase();
        String tokenInfo = request.getHeader(TOKEN_INFO_HEADER);
        MethodSignature ms = (MethodSignature)pjp.getSignature();
        Rbac rbac = ms.getMethod().getAnnotation(Rbac.class);
        String resourceName = rbac.resource();

        Optional<TokenEntity> tokenEntityOptional = ControllerUtil.getUserTokenInfo(tokenInfo);
        if (tokenEntityOptional.isPresent()) {
            // process token roles
            TokenEntity tokenEntity = tokenEntityOptional.get();

            try {
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
            } catch (PolicyNotAuthorizedException e) {
                throw new NotAuthorizedException(e.getMessage());
            } catch (ResourceNotFoundException e) {
                throw new NotFoundException(e.getMessage());
            }
//        } else if (GET_METHDO_NAME.equals(methodType)){
//            String uri = request.getRequestURI();
//            String projectId =  uri.split("/")[2];
//            TokenEntity tokenEntity = new TokenEntity();
//            tokenEntity.setProjectId(projectId);
//            processAdminQuery(resourceName, tokenEntity);
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
        } catch (Exception e) {
            LOG.warn("process http get method return object failed, {}", e.getMessage());
        }
    }

    private void processAdminQuery(String resourceName, TokenEntity tokenEntity) {
        Map<String, String[]> queryParams = new HashMap<>(request.getParameterMap());
        if (!rbacManger.isAdmin(resourceName, tokenEntity)) {
            queryParams.put(QUERY_PROJECT_NAME, new String[]{tokenEntity.getProjectId()});
        }
        request.setAttribute(QUERY_ATTR_HEADER, queryParams);
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
                            String[] getFields) throws Exception {
        rbacManger.checkGet(resourceName, tokenEntity, getFields, ownerCheckerSupplier.getOwnerChecker());
    }

    private void processDelete(String resourceName, TokenEntity tokenEntity) throws Exception {
        rbacManger.checkDelete(resourceName, tokenEntity, ownerCheckerSupplier.getOwnerChecker());
    }

    private void processUpdate(String resourceName, TokenEntity tokenEntity,
                               List<String> bodyFields) throws Exception {
        rbacManger.checkUpdate(resourceName, tokenEntity, bodyFields, ownerCheckerSupplier.getOwnerChecker());
    }

    private void processCreate(String resourceName, TokenEntity tokenEntity,
                               List<String> bodyFields) throws Exception {
        rbacManger.checkCreate(resourceName, tokenEntity, bodyFields, ownerCheckerSupplier.getOwnerChecker());
    }

}
