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

package org.apache.tuscany.binding.axis2.itests;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

public class HelloWorldMultiService implements HelloWorldOM, HelloWorldOM2 {

    public OMElement getGreetings(OMElement requestOM) {
        String name = requestOM.getFirstElement().getText();

        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMElement responseOM = omFactory.createOMElement("getGreetingsResponse", "http://helloworld-om", "helloworld");
        OMElement param = omFactory.createOMElement("getGreetingsReturn", "http://helloworld-om", "helloworld");
        responseOM.addChild(param);
        param.addChild(omFactory.createOMText("Hello " + name));
        
        return  responseOM;
    }

    public OMElement getGreetings2(OMElement requestOM) {
        String name = requestOM.getFirstElement().getText();

        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMElement responseOM = omFactory.createOMElement("getGreetingsResponse", "http://helloworld-om", "helloworld");
        OMElement param = omFactory.createOMElement("getGreetingsReturn", "http://helloworld-om", "helloworld");
        responseOM.addChild(param);
        param.addChild(omFactory.createOMText("Hello2 " + name));
        
        return  responseOM;
    }
}
