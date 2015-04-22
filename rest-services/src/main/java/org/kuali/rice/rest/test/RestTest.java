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

import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class RestTest extends TestCase {

    public static String getAppUrl() {
        return TestConstants.RICE_URL;
    }

    public static String getRestUrl() {
        return getAppUrl() + TestConstants.REST_PATH;
    }

    public String getToken() throws Exception {

        String clientId = TestConstants.CLIENT_ID;
        String secret = TestConstants.CLIENT_SECRET;
        String urlParameters =
                "grant_type=client_credentials&client_secret=" + secret + "&client_id=" + clientId + "&scope=access";
        String location = getRestUrl() + "/oauth/token";

        String response = request("POST", location, urlParameters, null, "application/x-www-form-urlencoded");
        Assert.assertNotNull(response);
        Assert.assertTrue(response.startsWith("{"));
        Assert.assertTrue(response.contains("access_token"));
        Assert.assertTrue(response.contains("token_type"));
        String token = null;


        JSONObject jsonObject = new JSONObject(response);
        token = jsonObject.getString("access_token");


        return token;
    }

    public String getWithToken(String location, String urlParameters, String token) throws Exception {
        return request("GET", location, urlParameters, token, null);
    }

    public String postWithToken(String location, String urlParameters, String token) throws Exception {
        return request("POST", location, urlParameters, token, "application/json");
    }

    public String putWithToken(String location, String urlParameters, String token) throws Exception {
        return request("PUT", location, urlParameters, token, "application/json");
    }

    public String deleteWithToken(String location, String urlParameters, String token) throws Exception {
        return request("DELETE", location, urlParameters, token, "application/x-www-form-urlencoded");
    }

    public String request(String requestMethod, String location, String queryContent, String token, String contentType) throws Exception {
        String responseJSON = null;
        URL url;
        HttpURLConnection connection = null;

        //Create connection


        if (requestMethod.equalsIgnoreCase("post") || requestMethod.equalsIgnoreCase("put")) {
            url = new URL(location);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod.toUpperCase());
            connection.setRequestProperty("Content-Type", contentType);

            if (StringUtils.isNotBlank(token)) {
                connection.setRequestProperty("Authorization", "bearer " + token);
            }

            connection.setRequestProperty("Content-Length", "" + Integer.toString(queryContent.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(queryContent);
            wr.flush();
            wr.close();
        } else {
            url = new URL(location + "?" + queryContent);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod.toUpperCase());
            connection.setRequestProperty("Accept-Language", "en-US");
            connection.setRequestProperty("Accept", "*/*");

            if (StringUtils.isNotBlank(token)) {
                connection.setRequestProperty("Authorization", "bearer " + token);
            }
        }

        //Successful responses start with 2
        if (!Integer.toString(connection.getResponseCode()).startsWith("2")) {
            return "" + connection.getResponseCode();
        }

        //Get Response
        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuffer response = new StringBuffer();
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();

        responseJSON = response.toString();

        return responseJSON;
    }
}
