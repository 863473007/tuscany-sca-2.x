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

package org.apache.tuscany.sca.policy.transaction.runtime;

import java.util.List;

import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Phase;
import org.apache.tuscany.sca.invocation.PhasedInterceptor;
import org.apache.tuscany.sca.policy.PolicyExpression;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.transaction.TransactionPolicy;
import org.apache.tuscany.sca.provider.BasePolicyProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;

/**
 * @version $Rev$ $Date$
 */
public class TransactionImplementationPolicyProvider extends BasePolicyProvider<TransactionPolicy> {
    private TransactionManagerHelper helper;
    private List<PolicySet> policySets;
    private RuntimeComponent component;

    public TransactionImplementationPolicyProvider(TransactionManagerHelper helper, RuntimeComponent component) {
        super(TransactionPolicy.class, component);
        this.helper = helper;
        this.policySets = component.getPolicySets();
    }

    public PhasedInterceptor createInterceptor(Operation operation) {
        /* TODO - 2.x better way of doing this in 2.x */
        for (PolicySet policySet : policySets) {
            for (PolicyExpression p : policySet.getPolicies()) {
                if (p.getPolicy() instanceof TransactionPolicy) {
                    TransactionInterceptor interceptor = new TransactionInterceptor(helper, false, null, (TransactionPolicy)p.getPolicy(), getPhase());
                    return interceptor;
                }
            }
        }
     
        return null;
    }

    public String getPhase() {
        return Phase.IMPLEMENTATION_POLICY;
    }

}