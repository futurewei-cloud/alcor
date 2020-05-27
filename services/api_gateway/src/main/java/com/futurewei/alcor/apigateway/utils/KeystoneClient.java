package com.futurewei.alcor.apigateway.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.common.Json;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jcraft.jsch.IO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.xml.ws.Response;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Component
public class KeystoneClient {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private static final String TOKEN_URL = "/v3/auth/tokens";
    private static final String VALIDATE_TOKEN_HEADER = "X-Subject-Token";
    private static final String AUTH_TOKEN_HEADER = "X-Auth-Token";

    private static String baseUrl = "";
    private static String localToken = "";
    private static Date expireDate;

    //TODO add token verify cache

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

    private WebClient webClient;

    public KeystoneClient(){
        this.webClient = WebClient.create();
    }

    public void checkEndPoints() throws IOException{
        if(!"".equals(baseUrl)){
            return;
        }else if (authUrl.contains("v3") || authUrl.contains("v2")){
            baseUrl = authUrl;
            return;
        }

        Mono<String> resp = webClient.get()
                .uri(authUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class);

        JsonNode versions = json2Map(resp.block());

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
        Mono<ClientResponse> resp = webClient.post().uri(baseUrl + TOKEN_URL + "?nocatalog")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(buildLocalTokenParams()))
                .exchange();

        ClientResponse response = resp.block();
        localToken = response.headers().asHttpHeaders().getFirst(VALIDATE_TOKEN_HEADER);
        String resultStr = response.bodyToMono(String.class).block();
        JsonNode result = json2Map(resultStr);
        JsonNode token = result.path("token");
        String expireDateStr = token.path("expires_at").asText();
        if(!"null".equals(expireDateStr)) {
            expireDateStr = expireDateStr.replace("000Z", "+0000");
            try {
                expireDate = dateFormat.parse(expireDateStr);
            } catch (ParseException e) {
                e.printStackTrace();
                localToken = "";
            }
        }

    }

    public String verifyToken(String token) throws IOException{
        getLocalToken();
        Mono<ClientResponse> resp = webClient.get()
                .uri(baseUrl + TOKEN_URL + "?nocatalog")
                .accept(MediaType.APPLICATION_JSON)
                .header(VALIDATE_TOKEN_HEADER, token)
                .header(AUTH_TOKEN_HEADER, localToken)
                .exchange();

        ClientResponse response = resp.block();
        //TODO check response status code details

        // check headers
        if(response.statusCode().equals(HttpStatus.OK)){
            String resultStr = response.bodyToMono(String.class).block();
            JsonNode result = json2Map(resultStr);
            JsonNode tokenNode = result.path("token");
            if(tokenNode.has("project")){
                JsonNode project = tokenNode.path("project");
                String projectId = project.path("id").asText();
                return projectId;
            }
        }
        return "";
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
        identity.set("identity", identity);

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
