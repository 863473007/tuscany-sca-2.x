/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.tuscany.sca.binding.jsonrpc;

import junit.framework.Assert;

import org.apache.tuscany.sca.node.Contribution;
import org.apache.tuscany.sca.node.ContributionLocationHelper;
import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.NodeFactory;
import org.apache.wink.json4j.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import echo.Echo;

/**
 * @version $Rev$ $Date$
 */
public class JSONRPCSmdTestCase {

    private static final String SERVICE_PATH = "/EchoService";

    private static final String SERVICE_URL = "http://localhost:8085/SCADomain" + SERVICE_PATH;

    private static final String SMD_URL = SERVICE_URL + "?smd";

    private static Node node;

    @BeforeClass
    public static void setUp() throws Exception {
        try {
            String contribution = ContributionLocationHelper.getContributionLocation(JSONRPCSmdTestCase.class);
            node =
                NodeFactory.newInstance()
                    .createNode("JSONRPCBinding.composite", new Contribution("test", contribution));
            node.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        node.stop();
    }

    @Test
    /**
     * This test make sure the JSON-RPC Binding can handle special characters when generating SMD
     */
    public void testJSONRPCSmdSpecialCharacters() throws Exception {
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(SMD_URL);
        WebResponse response = wc.getResource(request);

        Assert.assertEquals(200, response.getResponseCode());
        JSONObject smd = new JSONObject(response.getText());
        Assert.assertEquals(Echo.class.getMethods().length, smd.getJSONArray("methods").length());

        // System.out.println(">>>SMD:\n" + response.getText());
    }
}
