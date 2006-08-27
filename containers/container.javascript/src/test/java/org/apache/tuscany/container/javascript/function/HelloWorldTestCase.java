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
package org.apache.tuscany.container.javascript.function;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Hashtable;

import javax.xml.stream.XMLStreamReader;

import helloworld.HelloWorldService;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.tuscany.container.javascript.utils.xmlfromxsd.XMLGenerator;
import org.apache.tuscany.container.javascript.utils.xmlfromxsd.XMLGeneratorFactory;
import org.apache.tuscany.container.javascript.utils.xmlfromxsd.XMLfromXSDConfiguration;
import org.apache.tuscany.test.SCATestCase;
import org.apache.xmlbeans.XmlObject;
import org.osoa.sca.CompositeContext;
import org.osoa.sca.CurrentCompositeContext;

/**
 * This shows how to test the HelloWorld service component.
 */
public class HelloWorldTestCase extends SCATestCase {

    private HelloWorldService helloWorldService;

    private HelloWorldService introspectableService;
    
    private HelloWorldService e4xHelloWorldService;

    protected void setUp() throws Exception {
        addExtension("JavaScriptContainer", getClass().getClassLoader().getResource("META-INF/sca/default.scdl"));
        setApplicationSCDL("org/apache/tuscany/container/javascript/function/helloworld.scdl");
        super.setUp();

        CompositeContext context = CurrentCompositeContext.getContext();
        helloWorldService = context.locateService(HelloWorldService.class, "HelloWorldComponent");
        introspectableService = context.locateService(HelloWorldService.class, "IntrospectableHelloWorldComponent");
        e4xHelloWorldService = context.locateService(HelloWorldService.class, "HelloWorldComponentE4X");
    }

    public void testHelloWorld() throws Exception {
        assertEquals(helloWorldService.sayHello("petra"), "Hello petra");
    }

    public void testIntrospectedHelloWorld() throws Exception {
        assertEquals(introspectableService.sayHello("petra"), "Hello petra");
    }
    
    public void testE4XImplInvocation() throws Exception {
        String xmlInput = "<hel:getGreetings xmlns:hel=\"http://helloworld\"> " +
                            "<hel:name>TuscanyWorld</hel:name> " +
                        "</hel:getGreetings>";
        
        XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(
                                       new ByteArrayInputStream(xmlInput.getBytes()));
        StAXOMBuilder staxOMBuilder = new StAXOMBuilder(OMAbstractFactory.getOMFactory(), xmlReader);
        Object response = e4xHelloWorldService.sayE4XHello(staxOMBuilder.getDocumentElement());
        assertNotNull(response);
        assertTrue(response instanceof OMElement);
        assertEquals("e4xHello TuscanyWorld", ((OMElement)response).getFirstElement().getText());
        //System.out.println(response);
    }
    
    public void testE4XRefInvocation() throws Exception 
    {
        String initialInput = "JavaClient";
        String jsAddition = " thro e4x reference";
        String endSvcImplResponse = "Hello from Java Implementation to ";
        
        Object response = e4xHelloWorldService.sayHello(initialInput);
        assertNotNull(response);
        assertTrue(response instanceof String);
        assertEquals(endSvcImplResponse + initialInput + jsAddition, response.toString());
        //System.out.println(response);
    }
}
