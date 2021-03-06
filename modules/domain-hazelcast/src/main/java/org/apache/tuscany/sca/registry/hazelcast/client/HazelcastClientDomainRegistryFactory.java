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

package org.apache.tuscany.sca.registry.hazelcast.client;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.runtime.BaseDomainRegistryFactory;
import org.apache.tuscany.sca.runtime.DomainRegistry;

/**
 * The utility responsible for finding the endpoint regstry by the scheme and creating instances for the
 * given domain
 */
public class HazelcastClientDomainRegistryFactory extends BaseDomainRegistryFactory {
    private final static String[] schemes = new String[] {"hazelcastclient", "tuscanyclient"};

    /**
     * @param extensionRegistry
     */
    public HazelcastClientDomainRegistryFactory(ExtensionPointRegistry registry) {
        super(registry);
    }

    protected DomainRegistry createEndpointRegistry(String endpointRegistryURI, String domainURI) {
        DomainRegistry domainRegistry =
            new HazelcastClientEndpointRegistry(registry, null, endpointRegistryURI, domainURI);
        return domainRegistry;
    }

    public String[] getSupportedSchemes() {
        return schemes;
    }
}
