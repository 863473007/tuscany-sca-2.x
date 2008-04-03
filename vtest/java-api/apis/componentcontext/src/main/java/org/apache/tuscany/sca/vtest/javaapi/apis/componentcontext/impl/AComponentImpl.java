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

package org.apache.tuscany.sca.vtest.javaapi.apis.componentcontext.impl;

import org.apache.tuscany.sca.vtest.javaapi.apis.componentcontext.AComponent;
import org.apache.tuscany.sca.vtest.javaapi.apis.componentcontext.BComponent;
import org.osoa.sca.ComponentContext;
import org.osoa.sca.ServiceReference;
import org.osoa.sca.annotations.Context;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

@Service(AComponent.class)
public class AComponentImpl implements AComponent {

    protected ComponentContext componentContext;
    
    @Reference
    protected BComponent bReference;

    public String getName() {
        return "ComponentA";
    }

    @Context
    public void setComponentContext(ComponentContext context) {
        this.componentContext = context;
    }

    public String getContextURI() {
        return componentContext.getURI();
    }

    public String getServiceBName() {
        return componentContext.getService(BComponent.class, "bReference").getName();        
    }

    public String getServiceReferenceBName() {
        ServiceReference<BComponent> bSR = componentContext.getServiceReference(BComponent.class, "bReference");
        return bSR.getService().getName();
    }

}
