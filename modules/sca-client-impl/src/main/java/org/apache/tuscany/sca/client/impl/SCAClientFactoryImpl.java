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

package org.apache.tuscany.sca.client.impl;

import java.net.URI;
import java.util.List;

import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.NodeFinder;
import org.oasisopen.sca.NoSuchDomainException;
import org.oasisopen.sca.NoSuchServiceException;
import org.oasisopen.sca.ServiceUnavailableException;
import org.oasisopen.sca.client.SCAClientFactory;
import org.oasisopen.sca.client.SCAClientFactoryFinder;

public class SCAClientFactoryImpl extends SCAClientFactory {

    public static void setSCAClientFactoryFinder(SCAClientFactoryFinder factoryFinder) {
        SCAClientFactory.factoryFinder = factoryFinder;
    }
    
    public SCAClientFactoryImpl(URI domainURI) throws NoSuchDomainException {
        super(domainURI);
    }   
    
    @Override
    public <T> T getService(Class<T> serviceInterface, String serviceName) throws NoSuchServiceException, NoSuchDomainException {
        URI domainURI = getDomainURI();
        if (domainURI == null) {
            domainURI = URI.create(Node.DEFAULT_DOMAIN_URI);
        }
        List<Node> nodes = NodeFinder.getNodes(domainURI);
        if (nodes == null || nodes.size() < 1) {
            throw new NoSuchDomainException(domainURI.toString());
        }

        for (Node n : nodes) {
            try {
                return n.getService(serviceInterface, serviceName);
            } catch(ServiceUnavailableException e) {
                // Ingore and continue
            }
        }

        throw new NoSuchServiceException(serviceName);
    }
}
