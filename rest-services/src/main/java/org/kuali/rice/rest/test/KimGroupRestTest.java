package org.kuali.rice.rest.test;/*
 * Copyright 2006-2015 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.directwebremoting.json.types.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.kuali.rice.rest.RiceRestConstants;

public class KimGroupRestTest extends RestTest {

    private static final String REST_URL = getRestUrl() + RiceRestConstants.API_URL + "/kimgroups";
    private JSONObject currentTestGroup;
    private final static String TEST_GROUP_ID_1 = "TEST-GROUP-ID-1";
    private final static String TEST_GROUP_ID_2 = "TEST-GROUP-ID-2";

    public JSONObject createGroup() throws Exception {

        String name = "TEST-NAME-" + System.currentTimeMillis();
        JSONObject newGroup = new JSONObject();
        newGroup.put("namespaceCode", "TEST-CODE");
        newGroup.put("name", name);
        newGroup.put("description", "TEST-DESC");
        newGroup.put("active", true);
        newGroup.put("kimTypeId", "1");
        newGroup.put("attributes", new JsonObject());

        String response = postWithToken(REST_URL, newGroup.toString(), getToken());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.startsWith("{"));
        Assert.assertTrue(!response.contains("error:"));

        JSONObject group = new JSONObject(response);
        Assert.assertNotNull(group);
        Assert.assertEquals(group.getString("name"), name);

        String groupId = group.getString("groupId");
        Assert.assertNotNull(groupId);

        return group;

    }

    public String createGroupMember(String groupId, String idSuffix, String typeCode) throws Exception {
        String testMemberId = "TEST-MEMBER-ID-" + idSuffix;
        JSONObject newMember = new JSONObject();
        newMember.put("groupId", groupId);
        newMember.put("memberId", testMemberId);
        newMember.put("typeCode", typeCode);
        newMember.put("active", true);
        newMember.put("activeFromDate", 1262322000000L);
        newMember.put("activeToDate", 2524626000000L);
        newMember.put("type", "PRINCIPAL");

        String response = postWithToken(REST_URL + "/" + groupId + "/members", newMember.toString(), getToken());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.startsWith("{"));
        Assert.assertTrue(!response.contains("error"));

        JSONObject member = new JSONObject(response);
        Assert.assertNotNull(member);
        Assert.assertEquals(member.getString("memberId"), testMemberId);
        Assert.assertEquals(member.getString("groupId"), groupId);

        return testMemberId;

    }

    @Test
    public void testUnauthorized() throws Exception {
        String response = getWithToken(REST_URL, "", null);
        Assert.assertEquals(response, "401");

        response = getWithToken(REST_URL, "", "bad_token");
        Assert.assertEquals(response, "401");
    }

    @Test
    public void testGetGroupsForPrincipal() throws Exception {
        String response = getWithToken(REST_URL, "principalId=admin", getToken());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.startsWith("["));
        Assert.assertTrue(!response.contains("error:"));

        JSONArray groups = new JSONArray(response);
        Assert.assertTrue(groups.length() > 0);

    }

    @Test
    public void testGetGroupById() throws Exception {
        String response = getWithToken(REST_URL + "/" + TEST_GROUP_ID_1, "", getToken());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.startsWith("{"));
        Assert.assertTrue(!response.contains("error:"));

        JSONObject group = new JSONObject(response);
        Assert.assertNotNull(group);
        Assert.assertEquals(group.getString("groupId"), TEST_GROUP_ID_1);

    }

    @Test
    public void testCreateUpdateGroup() throws Exception {

        JSONObject group = createGroup();
        String name = group.getString("name");
        String groupId = group.getString("groupId");

        group.put("description", "TEST-NEWDESC");

        String response = response = putWithToken(REST_URL + "/" + groupId, group.toString(), getToken());
        group = new JSONObject(response);

        Assert.assertNotNull(group);
        Assert.assertEquals(group.getString("name"), name);
        Assert.assertEquals(group.getString("description"), "TEST-NEWDESC");


    }

    @Test
    public void testGetGroupRefs() throws Exception {
        String response = getWithToken(REST_URL + "/group-refs", "principalId=" + "TEST-MEMBER-ID-1", getToken());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.startsWith("["));
        Assert.assertTrue(!response.contains("error:"));

        JSONArray groups = new JSONArray(response);
        Assert.assertTrue(groups.length() > 0);
        JSONObject link = groups.getJSONObject(0);

        String href = link.getString("href");
        Assert.assertNotNull(href);

        response = getWithToken(href, "", getToken());
        JSONObject group = new JSONObject(response);
        Assert.assertNotNull(group);
        Assert.assertNotNull(group.getString("groupId"));

    }

    @Test
    public void testGetGroupMembers() throws Exception {
        String response = getWithToken(REST_URL + "/" + TEST_GROUP_ID_1 + "/members", "", getToken());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.startsWith("["));
        Assert.assertTrue(!response.contains("error:"));


        JSONArray members = new JSONArray(response);
        Assert.assertTrue(members.length() > 0);
        Assert.assertNotNull(members.getJSONObject(0));
        Assert.assertEquals(members.getJSONObject(0).getString("groupId"), TEST_GROUP_ID_1);

    }

    @Test
    public void testGetGroupMember() throws Exception {
        String response = getWithToken(REST_URL + "/" + TEST_GROUP_ID_1 + "/members/" + "TEST-MEMBER-ID-1", "", getToken());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.startsWith("["));
        Assert.assertTrue(!response.contains("error:"));

        JSONArray members = new JSONArray(response);
        Assert.assertTrue(members.length() > 0);
        Assert.assertNotNull(members.getJSONObject(0));
        Assert.assertEquals(members.getJSONObject(0).getString("groupId"), TEST_GROUP_ID_1);
        Assert.assertEquals(members.getJSONObject(0).getString("memberId"), "TEST-MEMBER-ID-1");

    }

    @Test
    public void testGetGroupMemberPrincipalRefs() throws Exception {
        String response = getWithToken(REST_URL + "/" + TEST_GROUP_ID_1 + "/member-refs/principal", "", getToken());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.startsWith("["));
        Assert.assertTrue(!response.contains("error:"));
        Assert.assertTrue(response.contains("\"href\" :"));

        JSONArray members = new JSONArray(response);
        Assert.assertTrue(members.length() > 0);
        Assert.assertNotNull(members.getJSONObject(0));
        Assert.assertNotNull(members.getJSONObject(0).getString("href"));

    }

    @Test
    public void testGetGroupMemberGroupRefs() throws Exception {
        String response = getWithToken(REST_URL + "/" + TEST_GROUP_ID_1 + "/member-refs/group", "", getToken());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.startsWith("["));

        JSONArray members = new JSONArray(response);
        Assert.assertTrue(members.length() > 0);

    }

    @Test
    public void testAddDeletePrincipalMember() throws Exception {
        String response = putWithToken(REST_URL + "/" + TEST_GROUP_ID_2 + "/members/principal/" + "TEST-MEMBER-ID-XYZ", "", getToken());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.startsWith("{"));

        JSONObject membersLink = new JSONObject(response);
        Assert.assertNotNull(membersLink);

        response = getWithToken(membersLink.getString("href"), "", getToken());

        JSONArray members = new JSONArray(response);
        boolean added = false;
        for (int i = 0; i < members.length(); i++) {
            JSONObject member = members.getJSONObject(i);
            if (member.getString("memberId").equals("TEST-MEMBER-ID-XYZ")) {
                Assert.assertEquals(member.getString("groupId"), TEST_GROUP_ID_2);
                added = true;
            }
        }

        Assert.assertTrue(added);

        response = deleteWithToken(REST_URL + "/" + TEST_GROUP_ID_2 + "/members/" + "TEST-MEMBER-ID-XYZ", "", getToken());
        membersLink = new JSONObject(response);
        Assert.assertNotNull(membersLink);

        response = getWithToken(membersLink.getString("href"), "", getToken());

        members = new JSONArray(response);
        boolean removed = true;
        for (int i = 0; i < members.length(); i++) {
            JSONObject member = members.getJSONObject(i);
            if (member.getString("memberId").equals("TEST-MEMBER-ID-XYZ")) {
                removed = false;
                break;
            }
        }

        Assert.assertTrue(removed);

    }

    @Test
    public void testAddDeleteGroupMember() throws Exception {
        String response = putWithToken(REST_URL + "/" + TEST_GROUP_ID_2 + "/members/group/TEST-GROUP-ID-XYZ", "", getToken());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.startsWith("{"));

        JSONObject membersLink = new JSONObject(response);
        Assert.assertNotNull(membersLink);

        response = getWithToken(membersLink.getString("href"), "", getToken());

        JSONArray members = new JSONArray(response);
        boolean added = false;
        for (int i = 0; i < members.length(); i++) {
            JSONObject member = members.getJSONObject(i);
            if (member.getString("memberId").equals("TEST-GROUP-ID-XYZ")) {
                Assert.assertEquals(member.getString("groupId"), TEST_GROUP_ID_2);
                added = true;
            }
        }

        Assert.assertTrue(added);

        response = deleteWithToken(REST_URL + "/" + TEST_GROUP_ID_2 + "/members/TEST-GROUP-ID-XYZ", "", getToken());
        membersLink = new JSONObject(response);
        Assert.assertNotNull(membersLink);

        response = getWithToken(membersLink.getString("href"), "", getToken());

        members = new JSONArray(response);
        boolean removed = true;
        for (int i = 0; i < members.length(); i++) {
            JSONObject member = members.getJSONObject(i);
            if (member.getString("memberId").equals("TEST-GROUP-ID-XYZ")) {
                removed = false;
                break;
            }
        }

        Assert.assertTrue(removed);

    }


    @Test
    public void testCreateUpdateDeleteMember() throws Exception {

        String memberId = createGroupMember(TEST_GROUP_ID_1, "ABC", "P");

        //TODO update simply does not work - already tried below and activeFromDate, active, memberId
/*            member.put("typeCode", "G");
            member.put("type", "GROUP");

            String updateUrl = member.getJSONArray("links").getJSONObject(0).getString("href");

            response = putWithToken(updateUrl, member.toString(), getToken());
            member = new JSONObject(response);
            Assert.assertNotNull(member);
            Assert.assertEquals(member.getString("typeCode"), "G");
            Assert.assertEquals(member.getString("type"), "GROUP");*/

        String response = deleteWithToken(REST_URL + "/" + TEST_GROUP_ID_1
                + "/members/TEST-MEMBER-ID-ABC", "", getToken());
        JSONObject membersLink = new JSONObject(response);
        Assert.assertNotNull(membersLink);

        response = getWithToken(membersLink.getString("href"), "", getToken());

        JSONArray members = new JSONArray(response);
        boolean removed = true;
        for (int i = 0; i < members.length(); i++) {
            JSONObject memberItem = members.getJSONObject(i);
            if (memberItem.getString("memberId").equals("TEST-MEMBER-ID-ABC")) {
                removed = false;
                break;
            }
        }

        Assert.assertTrue(removed);

    }

    @Test
    public void testGroupSearch() throws Exception {

        String response = getWithToken(REST_URL + "/search", "filter=name::TEST-*", getToken());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.contains("content"));


        JSONObject jsonObject = new JSONObject(response);
        JSONArray content = jsonObject.getJSONArray("content");
        Assert.assertNotNull(content);
        Assert.assertTrue(content.length() > 0);
        JSONObject group = content.getJSONObject(0);
        Assert.assertNotNull(group);
        Assert.assertTrue(group.getString("name").startsWith("TEST-"));

    }

    @Test
    public void testGroupSearchLimit() throws Exception {

        String response = getWithToken(REST_URL + "/search", "filter=name::TEST-*&limit=2", getToken());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.contains("content"));

        JSONObject jsonObject = new JSONObject(response);
        JSONArray content = jsonObject.getJSONArray("content");
        Assert.assertNotNull(content);
        Assert.assertTrue(content.length() <= 2);
        JSONObject group = content.getJSONObject(0);
        Assert.assertNotNull(group);
        Assert.assertTrue(group.getString("name").startsWith("TEST-"));
        String firstItemId = group.getString("groupId");

        response = getWithToken(REST_URL + "/search", "filter=name::TEST-*&limit=2&startIndex=2", getToken());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.contains("content"));

        jsonObject = new JSONObject(response);
        content = jsonObject.getJSONArray("content");
        Assert.assertNotNull(content);
        Assert.assertTrue(content.length() <= 2);
        group = content.getJSONObject(0);
        Assert.assertNotNull(group);
        Assert.assertTrue(group.getString("name").startsWith("TEST-"));

        Assert.assertNotEquals(firstItemId, group.getString("groupId"));

    }

    @Test
    public void testMemberSearch() throws Exception {

        String response = getWithToken(REST_URL + "/search/members", "filter=memberId::TEST-MEMBER-*|groupId::TEST-*", getToken());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.contains("content"));

        JSONObject jsonObject = new JSONObject(response);
        JSONArray content = jsonObject.getJSONArray("content");
        Assert.assertNotNull(content);
        Assert.assertTrue(content.length() > 0);
        JSONObject member = content.getJSONObject(0);
        Assert.assertNotNull(member);
        Assert.assertTrue(member.getString("memberId").startsWith("TEST-MEMBER-"));
        Assert.assertTrue(member.getString("groupId").startsWith("TEST-"));

    }

    @Test
    public void testMemberSearchLimit() throws Exception {

        String response = getWithToken(REST_URL + "/search/members", "filter=memberId::TEST-*&limit=2", getToken());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.contains("content"));

        JSONObject jsonObject = new JSONObject(response);
        JSONArray content = jsonObject.getJSONArray("content");
        Assert.assertNotNull(content);
        Assert.assertTrue(content.length() <= 2);
        JSONObject member = content.getJSONObject(0);
        Assert.assertNotNull(member);
        Assert.assertTrue(member.getString("memberId").startsWith("TEST-"));
        String firstItemId = member.getString("groupId") + member.getString("memberId");

        response = getWithToken(REST_URL + "/search/members", "filter=memberId::TEST-*&limit=2&startIndex=2", getToken());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.contains("content"));

        jsonObject = new JSONObject(response);
        content = jsonObject.getJSONArray("content");
        Assert.assertNotNull(content);
        Assert.assertTrue(content.length() <= 2);
        member = content.getJSONObject(0);
        Assert.assertNotNull(member);
        Assert.assertTrue(member.getString("memberId").startsWith("TEST-"));

        Assert.assertNotEquals(firstItemId, member.getString("groupId") + member.getString("memberId"));
    }

    @Test
    public void testDeleteAllMembers() throws Exception {

        JSONObject deleteAllMembersGroup = createGroup();
        String deleteAllMembersGroupId = deleteAllMembersGroup.getString("groupId");
        createGroupMember(deleteAllMembersGroupId, "1", "P");
        createGroupMember(deleteAllMembersGroupId, "2", "G");

        String response = deleteWithToken(REST_URL + "/" + deleteAllMembersGroupId
                + "/members", "", getToken());
        JSONObject membersLink = new JSONObject(response);

        Assert.assertNotNull(membersLink);
        response = getWithToken(membersLink.getString("href"), "", getToken());

        JSONArray members = new JSONArray(response);
        Assert.assertTrue(members.length() == 0);

    }
}
