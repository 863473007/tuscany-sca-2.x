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

package org.apache.tuscany.sca.policy.security;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.assembly.xml.Constants;
import org.apache.tuscany.sca.policy.Policy;

/**
 * Models the 'allow' authorization policy assertion
 * 
 * @version $Rev$ $Date$
 */
public class RunAsPolicyAssertion implements Policy {
    private String role = null;
    private boolean unResolved = false;
    
    public static final QName NAME = new QName(Constants.SCA10_NS, "runAs");
    
   
    public QName getSchemaName() {
        return NAME;
    }
    
    public boolean isUnresolved() {
        return unResolved;
    }

    public void setUnresolved(boolean unresolved) {
        this.unResolved = unresolved;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
