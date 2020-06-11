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

package com.futurewei.alcor.apigateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.futurewei.alcor.apigateway.client.KeystoneClient;
import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.entity.TokenEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(properties= {
        "keystone.enable=true",
        "keystone.project_domain_name=Default",
        "keystone.project_name=service",
        "keystone.user_domain_name=Default",
        "keystone.username=test",
        "keystone.password=test",
        "keystone.auth_type=password",
        "keystone.auth_url=http://localhost/identity"
})
public class KeystoneClientTest {

    private static final String TEST_LOCAL_TOKEN = "11aaaaaBex0xWssdfsadfDSSDFSDF";
    private static final String TEST_INVALID_LOCAL_TOKEN = "21aaaaaBex0xWssdfsadfDSSDFSDF";

    private static final String TEST_NOCACHE_TOKEN = "daaaaaBex0xWssdfsadfDSSDFSDF";

    private static final String TEST_TOKEN = "gaaaaaBex0xWssdfsadfDSSDFSDF";
    private static final String TEST_EXPIRED_TOKEN = "baaaaaBex0xWssdfsadfDSSDFSDF";
    private static final String TEST_NULL_TOKEN = "caaaaaBex0xWssdfsadfDSSDFSDF";
    private static final String TEST_INVALID_TOKEN = "eaaaaaBex0xWssdfsadfDSSDFSDF";


    private static final String TEST_PROJECT_ID = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";

    private static final String TOKEN_URL = "/auth/tokens";
    private static final String BASE_URL = "http://localhost/identity";
    private static final String AUTH_URL_QUERY_PARAM = "?nocatalog";
    private static final String VALIDATE_TOKEN_HEADER = "X-Subject-Token";
    private static final String AUTH_TOKEN_HEADER = "X-Auth-Token";


    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private CacheFactory cacheFactory;

    @Autowired
    private KeystoneClient keystoneClient;

    @Value("${keystone.auth_url}")
    private String authUrl;

    @Before
    public void setUp() throws CacheException, ParseException {

        ICache cache = Mockito.mock(ICache.class);

        // build normal token
        TokenEntity tokenEntity = new TokenEntity(TEST_TOKEN, false);
        tokenEntity.setExpireAt(dateFormat.parse("2020-08-05T09:00:00.000+0000"));
        tokenEntity.setProjectId(TEST_PROJECT_ID);
        when(cache.get(TEST_TOKEN)).thenReturn(tokenEntity);

        // build no cache token
        when(cache.get(TEST_NULL_TOKEN)).thenReturn(null);

        // build expired token
        TokenEntity tokenExpiredEntity = new TokenEntity(TEST_EXPIRED_TOKEN, false);
        tokenExpiredEntity.setExpireAt(dateFormat.parse("2020-04-05T09:00:00.000+0000"));
        tokenExpiredEntity.setProjectId(TEST_PROJECT_ID);
        when(cache.get(TEST_EXPIRED_TOKEN)).thenReturn(tokenExpiredEntity);

        // build invalid token
        TokenEntity tokenInvalidEntity = new TokenEntity(TEST_EXPIRED_TOKEN, true);
        when(cache.get(TEST_INVALID_TOKEN)).thenReturn(tokenInvalidEntity);

        doNothing().when(cache).put(Mockito.eq(TEST_NOCACHE_TOKEN), any(TokenEntity.class));

        when(cacheFactory.getExpireCache(TokenEntity.class, 2, TimeUnit.HOURS)).thenReturn(cache);

        // build restTemplate
        String endpointsResponse = "" +
                "   {\"versions\": " +
                "        {\"values\": [{" +
                "             \"status\": \"stable\", " +
                "             \"updated\": \"2019-07-19T00:00:00Z\"," +
                "             \"media-types\": [{" +
                "                 \"base\": \"application/json\", " +
                "                 \"type\": \"application/vnd.openstack.identity-v3+json\"" +
                "             }]," +
                "             \"id\": \"v3.13\", " +
                "             \"links\": [{" +
                "                 \"href\": \"http://localhost/identity/v3\", " +
                "                 \"rel\": \"self\"" +
                "             }] " +
                "         }] " +
                "        }" +
                "   }";
        when(restTemplate.getForObject(authUrl, String.class)).thenReturn(endpointsResponse);
        String url = BASE_URL + "/v3" + TOKEN_URL + AUTH_URL_QUERY_PARAM;

        String tokenResponse = "{" +
                " \"token\": {" +
                "      \"is_domain\": false," +
                "      \"methods\": [\"password\"]," +
                "      \"roles\": [{" +
                "          \"id\": \"3297da9e8f0440f591c2c1b0c2ac327d\"," +
                "          \"name\": \"admin\"" +
                "      }]," +
                "      \"expires_at\": \"2100-06-01T08:16:01.000000Z\"," +
                "      \"project\": {" +
                "          \"domain\": {" +
                "              \"id\": \"default\"," +
                "              \"name\": \"Default\"" +
                "          }," +
                "          \"id\": \"" + TEST_PROJECT_ID + "\"," +
                "          \"name\": \"admin\"" +
                "      }," +
                "      \"user\": {" +
                "          \"password_expires_at\": null," +
                "          \"domain\": {" +
                "              \"id\": \"default\"," +
                "              \"name\": \"Default\"" +
                "           }," +
                "          \"id\": \"ccebc2b4ea484bc2bf309bb163efdb5e\"," +
                "          \"name\": \"admin\"" +
                "      }" +
                "}}";
        HttpHeaders headers = new HttpHeaders();
        headers.add(VALIDATE_TOKEN_HEADER, TEST_LOCAL_TOKEN);
        ResponseEntity<String> tokenResponseEntity = new ResponseEntity(tokenResponse, headers, HttpStatus.OK);
        when(restTemplate.postForEntity(Mockito.eq(url),
                any(HttpEntity.class), Mockito.eq(String.class))).thenReturn(tokenResponseEntity);

        String customerTokenResponse = "{" +
                " \"token\": {" +
                "      \"is_domain\": false," +
                "      \"methods\": [\"password\"]," +
                "      \"roles\": [{" +
                "          \"id\": \"3297da9e8f0440f591c2c1b0c2ac327d\"," +
                "          \"name\": \"admin\"" +
                "      }]," +
                "      \"expires_at\": \"2020-06-01T08:16:01.000000Z\"," +
                "      \"project\": {" +
                "          \"domain\": {" +
                "              \"id\": \"default\"," +
                "              \"name\": \"Default\"" +
                "          }," +
                "          \"id\": \"" + TEST_PROJECT_ID.replace("-", "") + "\"," +
                "          \"name\": \"demo\"" +
                "      }," +
                "      \"user\": {" +
                "          \"password_expires_at\": null," +
                "          \"domain\": {" +
                "              \"id\": \"default\"," +
                "              \"name\": \"Default\"" +
                "           }," +
                "          \"id\": \"ccebc2b4ea484bc2bf309bb163efdb5e\"," +
                "          \"name\": \"demo\"" +
                "      }" +
                "}}";

        when(restTemplate.exchange(Mockito.eq(url),
                Mockito.eq(HttpMethod.GET), any(HttpEntity.class), Mockito.eq(String.class))).then(new Answer<ResponseEntity<String>>() {
            public ResponseEntity<String> answer(InvocationOnMock invocation) throws Throwable {
               HttpEntity<String> httpEntity = (HttpEntity<String>)invocation.getArguments()[2];
               String authToken = httpEntity.getHeaders().getFirst(AUTH_TOKEN_HEADER);
               String validateAuthToken = httpEntity.getHeaders().getFirst(VALIDATE_TOKEN_HEADER);
               if (TEST_LOCAL_TOKEN.equals(authToken)){
                   if(TEST_TOKEN.equals(validateAuthToken) || TEST_NOCACHE_TOKEN.equals(validateAuthToken)){
                       return ResponseEntity.ok(customerTokenResponse);
                   }else if (TEST_INVALID_TOKEN.equals(validateAuthToken)){
                       return ResponseEntity.badRequest().build();
                   }else{
                       return ResponseEntity.notFound().build();
                   }
               }
               return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        });

        ReflectionTestUtils.setField(keystoneClient, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(keystoneClient, "cache", cache);
    }

    @Test
    public void testTransformProjectIdToUuid(){
        String uuid = ReflectionTestUtils.invokeMethod(keystoneClient, "transformProjectIdToUuid",
                "3297da9e8f0440f591c2c1b0c2ac327d");
        assertEquals("3297da9e-8f04-40f5-91c2-c1b0c2ac327d", uuid);

        Throwable t = null;
        try {
            ReflectionTestUtils.invokeMethod(keystoneClient, "transformProjectIdToUuid",
                    "3297da9e8f0440f591c2c1b0c2ac3");
        } catch (Exception e) {
            t = e;
        }

        assertNotNull(t);
        assertTrue(t instanceof IllegalArgumentException);
        assertTrue(t.getMessage().contains("Invalid UUID string:"));

        Throwable t2 = null;
        try {
            ReflectionTestUtils.invokeMethod(keystoneClient, "transformProjectIdToUuid",
                    "3297da9e8f0440f591c2c1b0c2ac327defsafdsfasdf");
        } catch (Exception e) {
            t2 = e;
        }

        assertNotNull(t2);
        assertTrue(t2 instanceof IllegalArgumentException);
        assertTrue(t2.getMessage().contains("Invalid UUID string:"));
    }

    @Test
    public void testBuildLocalTokenParams(){
        String requestParams = ReflectionTestUtils.invokeMethod(keystoneClient, "buildLocalTokenParams");
        JsonNode auth = ReflectionTestUtils.invokeMethod(keystoneClient, "json2Map", requestParams);
        assertEquals("password",
                auth.path("auth").path("identity").path("methods").elements().next().asText());
        assertEquals("test",
                auth.path("auth").path("identity").path("password").path("user").path("name").asText());
        assertEquals("test",
                auth.path("auth").path("identity").path("password").path("user").path("password").asText());
        assertEquals("service", auth.path("auth").path("scope").path("project").path("name").asText());
    }

    @Test
    public void testJson2Map(){
        String testJson = "{" +
                     "\"keyA\": \"keya\", " +
                     "\"listB\": [\"b1\", \"b2\", \"b3\"]" +
                "}";
        JsonNode testNode = ReflectionTestUtils.invokeMethod(keystoneClient, "json2Map", testJson);
        assertEquals("keya", testNode.path("keyA").asText());
        assertEquals("b1", testNode.path("listB").elements().next().asText());
    }

    @Test
    public void testRetrieveLinks(){
        String linksStr = "[" +
                "           {" +
                "             \"href\": \"http://localhost/identity/v2\", " +
                "             \"rel\": \"other\"" +
                "           }," +
                "           {" +
                "             \"href\": \"http://localhost/identity/v3\", " +
                "             \"rel\": \"self\"" +
                "           }" +
                "]";
        JsonNode linkNodes = ReflectionTestUtils.invokeMethod(keystoneClient, "json2Map", linksStr);
        ReflectionTestUtils.invokeMethod(keystoneClient, "retrieveLinks",
                linkNodes.elements());
        assertEquals("http://localhost/identity/v3", ReflectionTestUtils.getField(keystoneClient, "baseUrl"));
    }

    @Test
    public void testFindEndPoint(){
        String endpointsStr = "[{" +
                "             \"status\": \"stable\", " +
                "             \"updated\": \"2019-07-19T00:00:00Z\"," +
                "             \"media-types\": [{" +
                "                 \"base\": \"application/json\", " +
                "                 \"type\": \"application/vnd.openstack.identity-v3+json\"" +
                "             }]," +
                "             \"id\": \"v2.13\", " +
                "             \"links\": [{" +
                "                 \"href\": \"http://localhost/identity/v2\", " +
                "                 \"rel\": \"self\"" +
                "             }]}," +
                "             {\"status\": \"current\", " +
                "             \"updated\": \"2019-07-19T00:00:00Z\"," +
                "             \"media-types\": [{" +
                "                 \"base\": \"application/json\", " +
                "                 \"type\": \"application/vnd.openstack.identity-v3+json\"" +
                "             }]," +
                "             \"id\": \"v3.13\", " +
                "             \"links\": [{" +
                "                 \"href\": \"http://localhost/identity/v3\", " +
                "                 \"rel\": \"self\"" +
                "             }]}" +
                "] ";
        JsonNode auth = ReflectionTestUtils.invokeMethod(keystoneClient, "json2Map", endpointsStr);
        ReflectionTestUtils.invokeMethod(keystoneClient, "findEndPoint",
                auth.elements());
        assertEquals("http://localhost/identity/v2",
                ReflectionTestUtils.getField(keystoneClient, "baseUrl"));
    }

    @Test
    public void testAssignBaseUrlWithBestEndpoint(){
        String endpointsStr = "[{" +
                "             \"status\": \"stable\", " +
                "             \"updated\": \"2019-07-19T00:00:00Z\"," +
                "             \"media-types\": [{" +
                "                 \"base\": \"application/json\", " +
                "                 \"type\": \"application/vnd.openstack.identity-v3+json\"" +
                "             }]," +
                "             \"id\": \"v2.13\", " +
                "             \"links\": [{" +
                "                 \"href\": \"http://localhost/identity/v2\", " +
                "                 \"rel\": \"self\"" +
                "             }]}," +
                "             {\"status\": \"current\", " +
                "             \"updated\": \"2019-07-19T00:00:00Z\"," +
                "             \"media-types\": [{" +
                "                 \"base\": \"application/json\", " +
                "                 \"type\": \"application/vnd.openstack.identity-v3+json\"" +
                "             }]," +
                "             \"id\": \"v3.13\", " +
                "             \"links\": [{" +
                "                 \"href\": \"http://localhost/identity/v3\", " +
                "                 \"rel\": \"self\"" +
                "             }]}," +
                "             {\"status\": \"unknown\", " +
                "             \"updated\": \"2019-07-19T00:00:00Z\"," +
                "             \"media-types\": [{" +
                "                 \"base\": \"application/json\", " +
                "                 \"type\": \"application/vnd.openstack.identity-v3+json\"" +
                "             }]," +
                "             \"id\": \"v4.5\", " +
                "             \"links\": [{" +
                "                 \"href\": \"http://localhost/identity/v4\", " +
                "                 \"rel\": \"self\"" +
                "             }]}" +
                "] ";
        JsonNode auth = ReflectionTestUtils.invokeMethod(keystoneClient, "json2Map", endpointsStr);
        ReflectionTestUtils.invokeMethod(keystoneClient, "assignBaseUrlWithBestEndpoint",
                auth.elements());
        assertEquals("http://localhost/identity/v3",
                ReflectionTestUtils.getField(keystoneClient, "baseUrl"));
    }

    @Test
    public void checkEndpointsTest(){
        ReflectionTestUtils.invokeMethod(keystoneClient, "checkEndPoints");
        assertEquals("http://localhost/identity/v3",
                ReflectionTestUtils.getField(keystoneClient, "baseUrl"));
    }

    @Test
    public void getLocalTokenTest() throws ParseException{
        ReflectionTestUtils.invokeMethod(keystoneClient, "getLocalToken");
        assertEquals(TEST_LOCAL_TOKEN, ReflectionTestUtils.getField(keystoneClient, "localToken"));
        assertEquals(dateFormat.parse("2100-06-01T08:16:01.000+0000"),
                ReflectionTestUtils.getField(keystoneClient, "localTokenExpireDate"));
    }

    @Test
    public void verifyTokenTest(){
        String projectId = keystoneClient.verifyToken(TEST_TOKEN);
        assertEquals(TEST_PROJECT_ID, projectId);

        String emptyProjectId = keystoneClient.verifyToken(TEST_EXPIRED_TOKEN);
        assertEquals("", emptyProjectId);

        String empty2ProjectId = keystoneClient.verifyToken(TEST_INVALID_TOKEN);
        assertEquals("", empty2ProjectId);

        String noCacheProjectId = keystoneClient.verifyToken(TEST_NOCACHE_TOKEN);
        assertEquals(TEST_PROJECT_ID, noCacheProjectId);

        ReflectionTestUtils.setField(keystoneClient, "localToken", TEST_INVALID_LOCAL_TOKEN);
        String empty3ProjectId = keystoneClient.verifyToken(TEST_NOCACHE_TOKEN);
        assertEquals("", empty3ProjectId);
    }

}
