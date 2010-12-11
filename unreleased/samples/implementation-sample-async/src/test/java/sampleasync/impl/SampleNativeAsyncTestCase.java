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

package sampleasync.impl;

import static org.junit.Assert.assertEquals;

import org.apache.tuscany.sca.node.Contribution;
import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.NodeFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import sampleasync.Upper;

/**
 * Test how to run an SCA contribution containing a test composite on a
 * Tuscany runtime node.
 * 
 * @version $Rev$ $Date$
 */
public class SampleNativeAsyncTestCase {
    static Node node;

    @BeforeClass
    public static void setUp() throws Exception {
        final NodeFactory nf = NodeFactory.newInstance();
        String here = SampleNativeAsyncTestCase.class.getProtectionDomain().getCodeSource().getLocation().toString();
        node = nf.createNode(new Contribution("test", here));
        node.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        node.stop();
    }

    @Test
    public void testReference() {
        System.out.println("SampleNaiveAsyncTestCase.testReference");
        Upper upper = node.getService(Upper.class, "SampleNativeAsyncReference");
        final String r = upper.upper("async"); 
        System.out.println(r);
        assertEquals("ASYNC", r);
    }
}