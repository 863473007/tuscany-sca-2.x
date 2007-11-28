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
package org.apache.tuscany.sca.policy;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Represents a policy set. See the Policy Framework specification for a
 * description of this element.
 */
public interface PolicySet {

    /**
     * Returns the intent name.
     * 
     * @return the intent name
     */
    QName getName();

    /**
     * Sets the intent name
     * 
     * @param name the intent name
     */
    void setName(QName name);

    /**
     * Returns the list of operations that this policy set applies to.
     * 
     * @return
     */
    //List<Operation> getOperations();

    /**
     * Returns the list of
     * 
     * @return
     */
    List<PolicySet> getReferencedPolicySets();

    /**
     * Returns the list of provided intents
     * 
     * @return
     */
    List<Intent> getProvidedIntents();


    /**
     * Returns the list of concrete policies, either WS-Policy policy
     * attachments, policy references, or policies expressed in another policy
     * language.
     * 
     * @return the list of concrete policies
     */
    List<Object> getPolicies();

    /**
     * Returns true if the model element is unresolved.
     * 
     * @return true if the model element is unresolved.
     */
    boolean isUnresolved();

    /**
     * Sets whether the model element is unresolved.
     * 
     * @param unresolved whether the model element is unresolved
     */
    void setUnresolved(boolean unresolved);
    
    /**
     * Returns the xpath expression that is to be used to evaluate
     * if this PolicySet applies to specific attachment point
     * 
     * @return the xpath expression
     */
    String getAppliesTo();
    
    /**
     * Sets the xpath expression that is to be used to evaluate
     * if this PolicySet applies to specific attachment point
     * 
     * @return 
     */
    void setAppliesTo(String xpath);
    
    
    /**
     * Returns the policies / policy attachments provided thro intent maps
     * 
     * @return
     */
    Map<Intent, List<Object>>getMappedPolicies();
}
