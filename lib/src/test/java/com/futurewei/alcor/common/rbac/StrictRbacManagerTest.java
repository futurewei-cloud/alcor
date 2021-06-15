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

package com.futurewei.alcor.common.rbac;

import com.futurewei.alcor.common.cache.entity.TestVpcEntity;
import com.futurewei.alcor.common.entity.TokenEntity;
import com.futurewei.alcor.common.exception.FieldPolicyNotAuthorizedException;
import com.futurewei.alcor.common.exception.ParseObjectException;
import com.futurewei.alcor.common.exception.PolicyResourceNotFoundException;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StrictRbacManagerTest {

    private StrictRbacManager strictRbacManager;
    private static final String RESOURCE = "vpc";
    private static final String TOKEN_NAME = "test";
    private static final String ADMIN_ROLE_NAME = "admin";
    private static final String ADVSVC_ROLE_NAME = "advsvc";
    private static final String NOT_ADMIN_ROLE_NAME = "member";

    @Before
    public void init() throws IOException {
        strictRbacManager = new StrictRbacManager();
    }

    @Test
    public void checkUpdateTest() throws Exception {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(ADMIN_ROLE_NAME));
        List<String> bodyFields = Lists.newArrayList("shared", "is_default", "name");
        strictRbacManager.checkUpdate(RESOURCE, tokenEntity, bodyFields, () -> false);
    }

    @Test
    public void checkUpdate_AdminAndOwnerTest() throws Exception {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(ADMIN_ROLE_NAME));
        List<String> bodyFields = Lists.newArrayList("shared", "is_default", "name");
        strictRbacManager.checkUpdate(RESOURCE, tokenEntity, bodyFields, () -> true);
    }

    @Test
    public void checkUpdate_NotAdminFailedTest() {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(NOT_ADMIN_ROLE_NAME));
        List<String> bodyFields = Lists.newArrayList("shared", "is_default", "name");
        try {
            strictRbacManager.checkUpdate(RESOURCE, tokenEntity, bodyFields, () -> false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof PolicyResourceNotFoundException);
        }
    }

    @Test
    public void checkUpdate_OwnerTest() throws Exception {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(NOT_ADMIN_ROLE_NAME));
        List<String> bodyFields = Lists.newArrayList("port_security_enabled", "router:external", "name");
        strictRbacManager.checkUpdate(RESOURCE, tokenEntity, bodyFields, () -> true);
    }

    @Test
    public void checkUpdate_NotAdminNotOwnerTest(){
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(NOT_ADMIN_ROLE_NAME));
        List<String> bodyFields = Lists.newArrayList("name", "router:external");
        try {
            strictRbacManager.checkUpdate(RESOURCE, tokenEntity, bodyFields, () -> false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof PolicyResourceNotFoundException);
        }
    }

    @Test
    public void checkDeleteTest() throws Exception {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(ADMIN_ROLE_NAME));
        strictRbacManager.checkDelete(RESOURCE, tokenEntity, () -> true);
    }

    @Test
    public void checkDelete_OwnerTest() throws Exception {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(ADMIN_ROLE_NAME));
        strictRbacManager.checkDelete(RESOURCE, tokenEntity, () -> true);
    }

    @Test
    public void checkDelete_FailedTest() {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(NOT_ADMIN_ROLE_NAME));
        try {
            strictRbacManager.checkDelete(RESOURCE, tokenEntity, () -> false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof PolicyResourceNotFoundException);
        }
    }

    @Test
    public void checkCreateTest() throws Exception {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(ADMIN_ROLE_NAME));
        List<String> bodyFields = Lists.newArrayList("shared", "router:external", "is_default");
        strictRbacManager.checkCreate(RESOURCE, tokenEntity, bodyFields, () -> false);
    }

    @Test
    public void checkCreate_AdminAndOwnerTest() throws Exception {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(ADMIN_ROLE_NAME));
        List<String> bodyFields = Lists.newArrayList("shared", "router:external", "is_default");
        strictRbacManager.checkCreate(RESOURCE, tokenEntity, bodyFields, () -> true);
    }

    @Test
    public void checkCreate_OwnerTest() throws Exception {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(NOT_ADMIN_ROLE_NAME));
        List<String> bodyFields = Lists.newArrayList("name");
        strictRbacManager.checkCreate(RESOURCE, tokenEntity, bodyFields, () -> true);
    }

    @Test
    public void checkCreate_NotAdminAndNotOwnerTest() throws Exception {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(NOT_ADMIN_ROLE_NAME));
        List<String> bodyFields = Lists.newArrayList("name");
        strictRbacManager.checkCreate(RESOURCE, tokenEntity, bodyFields, () -> false);
    }

    @Test
    public void checkCreate_NotAdminAndNotOwnerFailedTest(){
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(NOT_ADMIN_ROLE_NAME));
        List<String> bodyFields = Lists.newArrayList("name", "shared", "is_default");
        try {
            strictRbacManager.checkCreate(RESOURCE, tokenEntity, bodyFields, () -> false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof FieldPolicyNotAuthorizedException);
        }
    }

    @Test
    public void checkCreate_NotAdminFailedTest(){
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(NOT_ADMIN_ROLE_NAME));
        List<String> bodyFields = Lists.newArrayList("name", "shared", "is_default");
        try {
            strictRbacManager.checkCreate(RESOURCE, tokenEntity, bodyFields, () -> true);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof FieldPolicyNotAuthorizedException);
        }
    }

    @Test
    public void checkGet_AdminTest() throws Exception {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(ADMIN_ROLE_NAME));
        String[] getFields = new String[]{"name", "shared", "is_default"};
        strictRbacManager.checkGet(RESOURCE, tokenEntity, getFields, () -> false);
    }

    @Test
    public void checkGet_OwnerTest() throws Exception {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(NOT_ADMIN_ROLE_NAME));
        String[] getFields = new String[]{"name"};
        strictRbacManager.checkGet(RESOURCE, tokenEntity, getFields, () -> true);
    }

    @Test
    public void checkGet_AdminAndOwnerTest() throws Exception {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(ADMIN_ROLE_NAME));
        String[] getFields = new String[]{"name"};
        strictRbacManager.checkGet(RESOURCE, tokenEntity, getFields, () -> true);
    }

    @Test
    public void checkGet_FailedTest() {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(NOT_ADMIN_ROLE_NAME));
        String[] getFields = new String[]{"name", "shared", "is_default"};
        try {
            strictRbacManager.checkGet(RESOURCE, tokenEntity, getFields, () -> true);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof FieldPolicyNotAuthorizedException);
        }
    }

    @Test
    public void processGetExcludeFieldsTest() throws ParseObjectException {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(ADMIN_ROLE_NAME));
        TestVpcEntity testVpcEntity = new TestVpcEntity("test", true, "physic");
        strictRbacManager.processGetExcludeFields(RESOURCE, tokenEntity, () -> false,
                testVpcEntity);
        Assert.assertNotNull(testVpcEntity.getProviderPhysicalNetwork());
    }

    @Test
    public void processGetExcludeFields_OwnerTest() throws ParseObjectException {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(NOT_ADMIN_ROLE_NAME));
        TestVpcEntity testVpcEntity = new TestVpcEntity("test", true, "physic");
        strictRbacManager.processGetExcludeFields(RESOURCE, tokenEntity, () -> true,
                testVpcEntity);
        Assert.assertNull(testVpcEntity.getProviderPhysicalNetwork());
    }

    @Test
    public void processListExcludeFieldsTest() throws ParseObjectException {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(ADMIN_ROLE_NAME));
        List<Object> testVpcEntities = new ArrayList<>();
        testVpcEntities.add(new TestVpcEntity("test", true, "physic1"));
        testVpcEntities.add(new TestVpcEntity("test1", false, "physic2"));
        testVpcEntities.add(new TestVpcEntity("test2", false, "physic3"));
        strictRbacManager.processListExcludeFields(RESOURCE, tokenEntity, () -> false,
                testVpcEntities);
        for (Object obj: testVpcEntities) {
            TestVpcEntity testVpcEntity = (TestVpcEntity) obj;
            Assert.assertNotNull(testVpcEntity.getProviderPhysicalNetwork());
        }
    }

    @Test
    public void processListExcludeFields_OwnerTest() throws ParseObjectException {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(NOT_ADMIN_ROLE_NAME));
        List<Object> testVpcEntities = new ArrayList<>();
        testVpcEntities.add(new TestVpcEntity("test", true, "physic1"));
        testVpcEntities.add(new TestVpcEntity("test1", false, "physic2"));
        testVpcEntities.add(new TestVpcEntity("test2", false, "physic3"));
        strictRbacManager.processListExcludeFields(RESOURCE, tokenEntity, () -> true,
                testVpcEntities);
        for (Object obj: testVpcEntities) {
            TestVpcEntity testVpcEntity = (TestVpcEntity) obj;
            Assert.assertNull(testVpcEntity.getProviderPhysicalNetwork());
        }
    }

    @Test
    public void isAdminTest() {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(ADMIN_ROLE_NAME, "testRole1", "testRole2"));
        Assert.assertTrue(strictRbacManager.isAdmin(RESOURCE, tokenEntity));
    }

    @Test
    public void isAdvsvcTest() {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(ADVSVC_ROLE_NAME, "testRole1", "testRole2"));
        Assert.assertTrue(strictRbacManager.isAdmin(RESOURCE, tokenEntity));
    }

    @Test
    public void isAdmin_FalseTest() {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.newArrayList(NOT_ADMIN_ROLE_NAME, "testRole1", "testRole2"));
        Assert.assertFalse(strictRbacManager.isAdmin(RESOURCE, tokenEntity));
    }

    @Test
    public void isAdmin_EmptyRolesTest() {
        TokenEntity tokenEntity = new TokenEntity(TOKEN_NAME, false);
        tokenEntity.setRoles(Lists.emptyList());
        Assert.assertFalse(strictRbacManager.isAdmin(RESOURCE, tokenEntity));
    }
}
