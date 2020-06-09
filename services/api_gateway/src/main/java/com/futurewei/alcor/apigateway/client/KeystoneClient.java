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

package com.futurewei.alcor.apigateway.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.entity.TokenEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class KeystoneClient {

    private static final Logger LOG = LoggerFactory.getLogger(KeystoneClient.class);

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private static final String TOKEN_URL = "/auth/tokens";
    private static final String AUTH_URL_V3 = "v3";
    private static final String AUTH_URL_V2 = "v2";
    private static final String AUTH_URL_QUERY_PARAM = "?nocatalog";
    private static final String VALIDATE_TOKEN_HEADER = "X-Subject-Token";
    private static final String AUTH_TOKEN_HEADER = "X-Auth-Token";
    private static final long CACHE_EXPIRE_HOUR = 2;

    private static final String JSON_PROJECT_KEY = "project";
    private static final String JSON_TOKEN_KEY = "token";
    private static final String JSON_DOMAIN_KEY = "domain";
    private static final String JSON_LINKS_KEY = "links";
    private static final String JSON_ID_KEY = "id";
    private static final String JSON_REL_KEY = "rel";
    private static final String JSON_HREF_KEY = "href";
    private static final String JSON_IDENTITY_KEY = "identity";
    private static final String JSON_PASSWORD_KEY = "password";
    private static final String JSON_VERSIONS_KEY = "versions";
    private static final String JSON_VALUES_KEY = "values";
    private static final String JSON_EXPIRES_AT_KEY = "expires_at";
    private static final String JSON_NAME_KEY = "name";
    private static final String JSON_SCOPE_KEY = "scope";
    private static final String JSON_AUTH_KEY = "auth";
    private static final String JSON_USER_KEY = "user";
    private static final String JSON_ROLES_KEY = "roles";
    private static final String JSON_METHODS_KEY = "methods";
    private static final String JSON_STATUS_KEY = "status";
    private static final String JSON_ENDPOINTS_AVAILABLE_STATUS = "stable, current, supported";


    private String baseUrl = "";
    private volatile String localToken = "";
    private volatile Date localTokenExpireDate;

    @Value("${keystone.project_domain_name}")
    private String projectDomainName;

    @Value("${keystone.project_name}")
    private String projectName;

    @Value("${keystone.user_domain_name}")
    private String userDomainName;

    @Value("${keystone.username}")
    private String username;

    @Value("${keystone.password}")
    private String password;

    @Value("${keystone.auth_type}")
    private String authType;

    @Value("${keystone.auth_url}")
    private String authUrl;

    private RestTemplate restTemplate;
    private ICache<String, TokenEntity> cache;

    public KeystoneClient(CacheFactory cacheFactory){
        this.restTemplate = new RestTemplate();
        this.cache = cacheFactory.getExpireCache(TokenEntity.class, CACHE_EXPIRE_HOUR, TimeUnit.HOURS);
    }

    @PostConstruct
    public void setUp() {
        try {
            checkEndPoints();
            getLocalToken();
        } catch (IOException | NullPointerException e) {
            LOG.error("Keystone client init failed: {}", e.getMessage());
        }
    }

    private void checkEndPoints() throws IOException{
        if(!"".equals(baseUrl)){
            return;
        }else if (authUrl.contains(AUTH_URL_V3) || authUrl.contains(AUTH_URL_V2)){
            baseUrl = authUrl;
            return;
        }

        String response = restTemplate.getForObject(authUrl, String.class);

        JsonNode versions = json2Map(response);

        //in v3 apis resp have versions key
        if(versions.has(JSON_VERSIONS_KEY)){
            JsonNode endpoints = versions.path(JSON_VERSIONS_KEY);
            //maybe there are no values key
            if(endpoints.has(JSON_VALUES_KEY)){
                endpoints = endpoints.path(JSON_VALUES_KEY);
            }
            Iterator<JsonNode> endpointsIt = endpoints.elements();
            assignBaseUrlWithBestEndpoint(endpointsIt);

            if("".equals(baseUrl)){
                findEndPoint(endpointsIt);
            }
        }
    }

    private void getLocalToken() throws IOException{
        //if have token and not expire use it
        if(!"".equals(localToken) && (localTokenExpireDate == null || localTokenExpireDate.after(new Date()))){
            return;
        }

        checkEndPoints();

        synchronized(this) {
            if(!"".equals(localToken) && (localTokenExpireDate == null || localTokenExpireDate.after(new Date()))){
                return;
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(buildLocalTokenParams(), headers);
            HttpEntity<String> response = restTemplate.postForEntity(baseUrl + TOKEN_URL + AUTH_URL_QUERY_PARAM, entity, String.class);

            localToken = response.getHeaders().getFirst(VALIDATE_TOKEN_HEADER);
            JsonNode result = json2Map(response.getBody());
            JsonNode token = result.path(JSON_TOKEN_KEY);
            String expireDateStr = token.path(JSON_EXPIRES_AT_KEY).asText();
            if (!"null".equals(expireDateStr)) {
                expireDateStr = expireDateStr.replace("000Z", "+0000");
                try {
                    localTokenExpireDate = dateFormat.parse(expireDateStr);
                } catch (ParseException e) {
                    LOG.error("Get Alcor Token failed, {}", e.getMessage());
                    localToken = "";
                }
            }
        }

    }

    /**
     * verify client request token, and cache the token info which get from keystone
     *
     * @param token the customer request token in 'X-Auth-Token'
     * @return a token project id from keystone
     *
     * @see <a href="https://docs.openstack.org/api-ref/identity/v3/index.html?expanded=password-authentication-with-scoped-authorization-detail#identity-api-operations">Keystone api operations</a>
     */
    public String verifyToken(String token){
        try {

            TokenEntity tokenEntity = cache.get(token);
            if(tokenEntity != null){
                return tokenEntity.isExpired() ? "" : tokenEntity.getProjectId();
            }

            checkEndPoints();
            getLocalToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set(VALIDATE_TOKEN_HEADER, token);
            headers.set(AUTH_TOKEN_HEADER, localToken);

            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            ResponseEntity<String> response = restTemplate.exchange(baseUrl + TOKEN_URL + AUTH_URL_QUERY_PARAM,
                    HttpMethod.GET, entity, String.class);

            // check headers
            if(response.getStatusCode().equals(HttpStatus.OK)){


                String resultStr = response.getBody();
                JsonNode result = json2Map(resultStr);
                JsonNode tokenNode = result.path(JSON_TOKEN_KEY);

                TokenEntity te = new TokenEntity(token,false);
                JsonNode user = tokenNode.path(JSON_USER_KEY);
                te.setUser(user.path(JSON_NAME_KEY).asText(""));
                te.setUserId(user.path(JSON_ID_KEY).asText(""));

                String expireDateStr = tokenNode.path(JSON_EXPIRES_AT_KEY).asText();
                expireDateStr = expireDateStr.replace("000Z", "+0000");
                Date expireDate = dateFormat.parse(expireDateStr);
                te.setExpireAt(expireDate);

                if(tokenNode.has(JSON_ROLES_KEY)){
                    JsonNode roles = tokenNode.path(JSON_ROLES_KEY);
                    Iterator<JsonNode> rolesIt = roles.elements();
                    List<String> roleNames = new ArrayList<>();
                    rolesIt.forEachRemaining(role -> roleNames.add(role.path(JSON_NAME_KEY).asText("")));
                    te.setRoles(roleNames);
                }

                if(tokenNode.has(JSON_PROJECT_KEY)){
                    JsonNode project = tokenNode.path(JSON_PROJECT_KEY);
                    String projectId = project.path(JSON_ID_KEY).asText();

                    if(project.has(JSON_DOMAIN_KEY)){
                        JsonNode domain = project.path(JSON_DOMAIN_KEY);
                        te.setDomainId(domain.path(JSON_ID_KEY).asText(""));
                        te.setDomainName(domain.path(JSON_NAME_KEY).asText(""));
                    }

                    projectId = transformProjectIdToUuid(projectId);
                    te.setProjectId(projectId);
                    te.setProjectName(project.path(JSON_NAME_KEY).asText(""));
                    cache.put(token, te);
                    return projectId;
                }
            }else{
                cache.put(token, new TokenEntity(token,true));
            }
        } catch (IOException | CacheException | ParseException e) {
            LOG.error("verify token failed, {}", e.getMessage());
        }
        return "";
    }

    private String transformProjectIdToUuid(String projectId){
        int uuidLength = 32;
        if (projectId.length() != uuidLength){
            throw new IllegalArgumentException("Invalid UUID string: "+ projectId);
        }
        return projectId.substring(0, 8) + "-" + projectId.substring(8, 12) +
                "-" + projectId.substring(12, 16) + "-" +
                projectId.substring(16, 20) + "-" +
                projectId.substring(20);
    }

    private String buildLocalTokenParams() throws JsonProcessingException {
        //build params
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode auth = mapper.createObjectNode();

        ObjectNode identity = mapper.createObjectNode();
        ArrayNode methods = mapper.createArrayNode();
        methods.add(authType);
        identity.set(JSON_METHODS_KEY, methods);

        ObjectNode passwordNode = mapper.createObjectNode();
        ObjectNode user = mapper.createObjectNode();
        user.put(JSON_NAME_KEY, username);
        user.put(JSON_PASSWORD_KEY, password);

        ObjectNode domain = mapper.createObjectNode();
        domain.put(JSON_NAME_KEY, userDomainName);
        user.set(JSON_DOMAIN_KEY, domain);

        passwordNode.set(JSON_USER_KEY, user);
        identity.set(JSON_PASSWORD_KEY, passwordNode);

        auth.set(JSON_IDENTITY_KEY, identity);

        ObjectNode scope = mapper.createObjectNode();
        ObjectNode project = mapper.createObjectNode();
        project.put(JSON_NAME_KEY, projectName);
        project.set(JSON_DOMAIN_KEY, domain);
        scope.set(JSON_PROJECT_KEY, project);

        auth.set(JSON_SCOPE_KEY, scope);
        ObjectNode root = mapper.createObjectNode();
        root.set(JSON_AUTH_KEY, auth);
        return mapper.writeValueAsString(root);
    }


    /**
     * find a best endpoint for client to use verifing tokens
     * A endpoint id is the API version,
     * first get the all status is stable, current, supported endpoints
     * sort them by id (version), get the last version endpoint
     * retrieve this endpoint link that "ref" value is "self"
     * @param endpoints
     */
    private void assignBaseUrlWithBestEndpoint(Iterator<JsonNode> endpoints){
        SortedMap<String, Iterator<JsonNode>> sortedMap = new TreeMap<>();
        while(endpoints.hasNext()){
            JsonNode endpoint = endpoints.next();

            //check id
            if(!endpoint.has(JSON_ID_KEY) || !endpoint.has(JSON_STATUS_KEY) || !endpoint.has(JSON_LINKS_KEY)){
                continue;
            }

            JsonNode id = endpoint.path(JSON_ID_KEY);
            JsonNode status = endpoint.path(JSON_STATUS_KEY);
            String statusStr = status.asText("unknown");
            if(JSON_ENDPOINTS_AVAILABLE_STATUS.contains(statusStr)){
                //get ref is self link
                JsonNode links = endpoint.path(JSON_LINKS_KEY);
                Iterator<JsonNode> linkIt = links.elements();
                sortedMap.put(id.asText(), linkIt);
            }
        }

        //find latest api version
        Iterator<JsonNode> linksIt = sortedMap.get(sortedMap.lastKey());
        retrieveLinks(linksIt);
    }

    /**
     * return a available endpoint when no best endpoint
     * @param endpoints
     */
    private void findEndPoint(Iterator<JsonNode> endpoints){
        while(endpoints.hasNext()){
            JsonNode endpoint = endpoints.next();
            // get ref is self link
            if(!endpoint.has(JSON_LINKS_KEY)){
                continue;
            }
            JsonNode links = endpoint.path(JSON_LINKS_KEY);

            Iterator<JsonNode> linksIt = links.elements();
            retrieveLinks(linksIt);
            if(!"".equals(baseUrl)){
                return;
            }
        }
    }

    private void retrieveLinks(Iterator<JsonNode> linksIt){
        while(linksIt.hasNext()){
            JsonNode link = linksIt.next();
            if(link.has(JSON_REL_KEY) && "self".equals(link.path(JSON_REL_KEY).asText(""))){
                if(!link.has(JSON_HREF_KEY)){
                    continue;
                }
                JsonNode href = link.path(JSON_HREF_KEY);

                baseUrl = href.asText();
                return;
            }
        }
    }

    private JsonNode json2Map(String jsonStr) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(jsonStr);
    }

}
