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

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.kuali.rice.rest.RiceRestConstants;

public class ActionListRestTest extends RestTest {

    private static final String REST_URL = getRestUrl() + RiceRestConstants.API_URL + "/actionlist";
    private static final String USER_SEGMENT = "/admin";

    @Test
    public void testUnauthorized() throws Exception {
        String response = getWithToken(REST_URL, "", null);
        Assert.assertEquals(response, "401");

        response = getWithToken(REST_URL, "", "bad_token");
        Assert.assertEquals(response, "401");
    }

    @Test
    public void testGetActionList() throws Exception {
        String response = getWithToken(REST_URL + USER_SEGMENT, "", getToken());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.contains("content"));

        JSONObject jsonObject = new JSONObject(response);
        JSONArray content = jsonObject.getJSONArray("content");
        Assert.assertNotNull(content);
        Assert.assertTrue(content.length() > 0);
        JSONObject actionItem = content.getJSONObject(0);
        Assert.assertNotNull(actionItem);
        Assert.assertNotNull(actionItem.getString("documentId"));
        Assert.assertEquals(actionItem.getString("principalId"), "admin");

    }
    
    @Test
     public void testFilteredDocumentSearch() throws Exception {
         String response = getWithToken(REST_URL + USER_SEGMENT, "filter=documentTitle::Rest*", getToken());
         Assert.assertNotNull(response);
         Assert.assertTrue(response.contains("content"));
 
         JSONObject jsonObject = new JSONObject(response);
         JSONArray content = jsonObject.getJSONArray("content");
         Assert.assertNotNull(content);
         JSONObject actionItem = content.getJSONObject(0);
         Assert.assertNotNull(actionItem);
         Assert.assertTrue(actionItem.getString("docTitle").startsWith("Rest"));
 
     }
 
     @Test
     public void testUnmatchedFilteredDocumentSearch() throws Exception {
         String response = getWithToken(REST_URL + USER_SEGMENT, "filter=documentTitle::BAD_TITLE*", getToken());
         Assert.assertNotNull(response);
         Assert.assertTrue(response.contains("content"));
 
         JSONObject jsonObject = new JSONObject(response);
         JSONArray content = jsonObject.getJSONArray("content");
         Assert.assertNotNull(content);
         Assert.assertTrue(content.length() == 0);
 
     }
 
     @Test
     public void testLimitedAndPagedDocumentSearch() throws Exception {
         String response = getWithToken(REST_URL + USER_SEGMENT, "limit=2&startIndex=0", getToken());
         Assert.assertNotNull(response);
         Assert.assertTrue(response.contains("content"));
 
         JSONObject jsonObject = new JSONObject(response);
         JSONArray content = jsonObject.getJSONArray("content");
         Assert.assertNotNull(content);
         Assert.assertTrue(content.length() <= 2);
         JSONObject actionItem = content.getJSONObject(0);
         String firstPageId = actionItem.getString("actionItemId");
 
         response = getWithToken(REST_URL + USER_SEGMENT, "limit=2&startIndex=2", getToken());
         jsonObject = new JSONObject(response);
         content = jsonObject.getJSONArray("content");
         Assert.assertNotNull(content);
         Assert.assertTrue(content.length() <= 2);
         actionItem = content.getJSONObject(0);
         Assert.assertNotEquals(firstPageId, actionItem.getString("actionItemId"));
 
     }
}
