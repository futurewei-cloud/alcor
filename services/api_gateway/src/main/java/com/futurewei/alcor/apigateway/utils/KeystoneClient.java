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

package com.futurewei.alcor.apigateway.utils;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class KeystoneClient {

    private static final Logger LOG = LoggerFactory.getLogger(KeystoneClient.class);

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private static final String TOKEN_URL = "/auth/tokens";
    private static final String VALIDATE_TOKEN_HEADER = "X-Subject-Token";
    private static final String AUTH_TOKEN_HEADER = "X-Auth-Token";

    private static String baseUrl = "";
    private static volatile String localToken = "";
    private static volatile Date expireDate;

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
        this.cache = cacheFactory.getCache(TokenEntity.class);
    }

    @PostConstruct
    public void setUp() throws IOException{
        checkEndPoints();
        getLocalToken();
    }

    public void checkEndPoints() throws IOException{
        if(!"".equals(baseUrl)){
            return;
        }else if (authUrl.contains("v3") || authUrl.contains("v2")){
            baseUrl = authUrl;
            return;
        }

        String response = restTemplate.getForObject(authUrl, String.class);

        JsonNode versions = json2Map(response);

        //in v3 apis resp have versions key
        if(versions.has("versions")){
            JsonNode endpoints = versions.path("versions");
            //maybe there are no values key
            if(endpoints.has("values")){
                endpoints = endpoints.path("values");
            }
            Iterator<JsonNode> endpointsIt = endpoints.elements();
            findBestEndPoint(endpointsIt);

            if("".equals(baseUrl)){
                findEndPoint(endpointsIt);
            }
        }
    }

    public void getLocalToken() throws IOException{
        //if have token and not expire use it
        if(!"".equals(localToken) && (expireDate == null || expireDate.after(new Date()))){
            return;
        }

        checkEndPoints();

        synchronized(this) {
            if(!"".equals(localToken) && (expireDate == null || expireDate.after(new Date()))){
                return;
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(buildLocalTokenParams(), headers);
            HttpEntity<String> response = restTemplate.postForEntity(baseUrl + TOKEN_URL + "?nocatalog", entity, String.class);

            localToken = response.getHeaders().getFirst(VALIDATE_TOKEN_HEADER);
            JsonNode result = json2Map(response.getBody());
            JsonNode token = result.path("token");
            String expireDateStr = token.path("expires_at").asText();
            if (!"null".equals(expireDateStr)) {
                expireDateStr = expireDateStr.replace("000Z", "+0000");
                try {
                    expireDate = dateFormat.parse(expireDateStr);
                } catch (ParseException e) {
                    LOG.error("Get Alcor Token failed, {}", e.getMessage());
                    localToken = "";
                }
            }
        }

    }

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
            ResponseEntity<String> response = restTemplate.exchange(baseUrl + TOKEN_URL + "?nocatalog",
                    HttpMethod.GET, entity, String.class);

            // check headers
            if(response.getStatusCode().equals(HttpStatus.OK)){


                String resultStr = response.getBody();
                JsonNode result = json2Map(resultStr);
                JsonNode tokenNode = result.path("token");

                TokenEntity te = new TokenEntity(token,false);
                JsonNode user = tokenNode.path("user");
                te.setUser(user.path("name").asText(""));
                te.setUserId(user.path("id").asText(""));

                String expireDateStr = tokenNode.path("expires_at").asText();
                expireDateStr = expireDateStr.replace("000Z", "+0000");
                try {
                    te.setExpireAt(dateFormat.parse(expireDateStr));
                } catch (ParseException e) {
                    LOG.error("Parse Token expire date to date error, {}", e.getMessage());
                }

                if(tokenNode.has("roles")){
                    JsonNode roles = tokenNode.path("roles");
                    Iterator<JsonNode> rolesIt = roles.elements();
                    List<String> roleNames = new ArrayList<>();
                    rolesIt.forEachRemaining(role -> roleNames.add(role.path("name").asText("")));
                    te.setRoles(roleNames);
                }

                if(tokenNode.has("project")){
                    JsonNode project = tokenNode.path("project");
                    String projectId = project.path("id").asText();

                    if(project.has("domain")){
                        JsonNode domain = project.path("domain");
                        te.setDomainId(domain.path("id").asText(""));
                        te.setDomainName(domain.path("name").asText(""));
                    }

                    projectId = transformProjectIdToUUID(projectId);
                    te.setProjectId(projectId);
                    te.setProjectName(project.path("name").asText(""));
                    cache.put(token, te);
                    return projectId;
                }
            }else{
                cache.put(token, new TokenEntity(token,true));
            }
        } catch (IOException | CacheException e) {
            LOG.error("verify token failed, {}", e.getMessage());
        }
        return "";
    }

    private String transformProjectIdToUUID(String projectId){
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
        identity.set("methods", methods);

        ObjectNode passwordNode = mapper.createObjectNode();
        ObjectNode user = mapper.createObjectNode();
        user.put("name", username);
        user.put("password", password);

        ObjectNode domain = mapper.createObjectNode();
        domain.put("name", userDomainName);
        user.set("domain", domain);

        passwordNode.set("user", user);
        identity.set("password", passwordNode);

        auth.set("identity", identity);

        ObjectNode scope = mapper.createObjectNode();
        ObjectNode project = mapper.createObjectNode();
        project.put("name", projectName);
        project.set("domain", domain);
        scope.set("project", project);

        auth.set("scope", scope);
        ObjectNode root = mapper.createObjectNode();
        root.set("auth", auth);
        return mapper.writeValueAsString(root);
    }

    private void findBestEndPoint(Iterator<JsonNode> endpoints){
        SortedMap<String, Iterator<JsonNode>> sortedMap = new TreeMap<>();
        while(endpoints.hasNext()){
            JsonNode endpoint = endpoints.next();

            //check id
            if(!endpoint.has("id")){
                continue;
            }
            JsonNode id = endpoint.path("id");

            //check status is in stable current supported
            JsonNode status = endpoint.path("status");
            String statusStr = status.asText("unknown");
            if(status.isMissingNode() || !"stable, current, supported".contains(statusStr)){
                continue;
            }

            //get ref is self link
            if(!endpoint.has("links")){
                continue;
            }
            JsonNode links = endpoint.path("links");
            Iterator<JsonNode> linkIt = links.elements();
            sortedMap.put(id.asText(), linkIt);
        }

        //find latest api version
        Iterator<JsonNode> linksIt = sortedMap.get(sortedMap.lastKey());
        retrieveLinks(linksIt);
    }

    private void findEndPoint(Iterator<JsonNode> endpoints){
        while(endpoints.hasNext()){
            JsonNode endpoint = endpoints.next();
            // get ref is self link
            if(!endpoint.has("links")){
                continue;
            }
            JsonNode links = endpoint.path("links");

            Iterator<JsonNode> linksIt = links.elements();
            retrieveLinks(linksIt);
        }
    }

    private void retrieveLinks(Iterator<JsonNode> linksIt){
        while(linksIt.hasNext()){
            JsonNode link = linksIt.next();
            if(link.has("rel") && link.path("rel").asText("").equals("self")){
                if(!link.has("href")){
                    continue;
                }
                JsonNode href = link.path("href");

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
