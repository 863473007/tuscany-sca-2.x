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

package org.apache.tuscany.sca.endpoint.hazelcast;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.LifeCycleListener;
import org.apache.tuscany.sca.runtime.BaseEndpointRegistry;
import org.apache.tuscany.sca.runtime.DomainRegistryURI;
import org.apache.tuscany.sca.runtime.EndpointRegistry;
import org.apache.tuscany.sca.runtime.RuntimeEndpoint;

import com.hazelcast.config.Config;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.nio.Address;

/**
 * An EndpointRegistry using a Hazelcast
 */
public class HazelcastEndpointRegistry extends BaseEndpointRegistry implements EndpointRegistry, LifeCycleListener, EntryListener<String, Endpoint>, MembershipListener {
    private final static Logger logger = Logger.getLogger(HazelcastEndpointRegistry.class.getName());

    protected DomainRegistryURI configURI;

    private HazelcastInstance hazelcastInstance;
    protected Map<Object, Object> map;
    private Map<String, Endpoint> localEndpoints = new HashMap<String, Endpoint>();

    public HazelcastEndpointRegistry(ExtensionPointRegistry registry,
                                     Map<String, String> attributes,
                                     String domainRegistryURI,
                                     String domainURI) {
        super(registry, attributes, domainRegistryURI, domainURI);
        this.configURI = new DomainRegistryURI(domainRegistryURI);
    }

    public void start() {
        if (map != null) {
            throw new IllegalStateException("The registry has already been started");
        }
        if (configURI.toString().startsWith("tuscany:vm:")) {
            map = new HashMap<Object, Object>();
        } else {
            initHazelcastInstance();
            IMap imap = hazelcastInstance.getMap(configURI.getDomainName() + "/Endpoints");
            imap.addEntryListener(this, true);
            map = imap;
            hazelcastInstance.getCluster().addMembershipListener(this);
        }
    }

    public void stop() {
        if (hazelcastInstance != null) {
            hazelcastInstance.shutdown();
            hazelcastInstance = null;
            map = null;
        }
    }

    private void initHazelcastInstance() {
        Config config = new XmlConfigBuilder().build();

        config.setPort(configURI.getListenPort());
        //config.setPortAutoIncrement(false);

        if (configURI.getBindAddress() != null) {
            config.getNetworkConfig().getInterfaces().setEnabled(true);
            config.getNetworkConfig().getInterfaces().clear();
            config.getNetworkConfig().getInterfaces().addInterface(configURI.getBindAddress());
        }

        config.getGroupConfig().setName(configURI.getDomainName());
        config.getGroupConfig().setPassword(configURI.getPassword());

        if (configURI.isMulticastDisabled()) {
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        } else {
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(true);
            config.getNetworkConfig().getJoin().getMulticastConfig().setMulticastPort(configURI.getMulticastPort());
            config.getNetworkConfig().getJoin().getMulticastConfig().setMulticastGroup(configURI.getMulticastAddress());
        }
        
        // config.getMapConfig(configURI.getDomainName() + "/Endpoints").setBackupCount(0);

        if (configURI.getRemotes().size() > 0) {
            TcpIpConfig tcpconfig = config.getNetworkConfig().getJoin().getTcpIpConfig();
            tcpconfig.setEnabled(true);
            List<Address> lsMembers = tcpconfig.getAddresses();
            lsMembers.clear();
            for (String addr : configURI.getRemotes()) {
                String[] ipNPort = addr.split(":");
                try {
                    lsMembers.add(new Address(ipNPort[0], Integer.parseInt(ipNPort[1])));
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        
        config.getMapConfig("default").setNearCacheConfig(new NearCacheConfig(0, 0, "NONE", 0, true));
        
        this.hazelcastInstance = Hazelcast.newHazelcastInstance(config);
    }

    public void addEndpoint(Endpoint endpoint) {
        map.put(endpoint.getURI(), endpoint);
        localEndpoints.put(endpoint.getURI(), endpoint);
        logger.info("Add endpoint - " + endpoint);
    }

    public List<Endpoint> findEndpoint(String uri) {
        List<Endpoint> foundEndpoints = new ArrayList<Endpoint>();
        for (Object v : map.values()) {
            Endpoint endpoint = (Endpoint)v;
            logger.fine("Matching against - " + endpoint);
            if (matches(uri, endpoint.getURI())) {
                if (!isLocal(endpoint)) {
                    ((RuntimeEndpoint)endpoint).bind(registry, this);
                } else {
                    // get the local version of the endpoint
                    // this local version won't have been serialized
                    // won't be marked as remote and will have the 
                    // full interface contract information
                    endpoint = localEndpoints.get(endpoint.getURI());
                }
                
                foundEndpoints.add(endpoint);
                logger.fine("Found endpoint with matching service  - " + endpoint);
            }
        }
        return foundEndpoints;
    }
    

    private boolean isLocal(Endpoint endpoint) {
        return localEndpoints.containsKey(endpoint.getURI());
    }

    public Endpoint getEndpoint(String uri) {
        return (Endpoint)map.get(uri);
    }

    public List<Endpoint> getEndpoints() {
        return new ArrayList(map.values());
    }

    public void removeEndpoint(Endpoint endpoint) {
        map.remove(endpoint.getURI());
        localEndpoints.remove(endpoint.getURI());
        logger.info("Removed endpoint - " + endpoint);
    }


    public void entryAdded(EntryEvent<String, Endpoint> event) {
        entryAdded(event.getKey(), event.getValue());
    }

    public void entryEvicted(EntryEvent<String, Endpoint> event) {
        // Should not happen
    }

    public void entryRemoved(EntryEvent<String, Endpoint> event) {
        entryRemoved(event.getKey(), event.getValue());
    }

    public void entryUpdated(EntryEvent<String, Endpoint> event) {
        entryUpdated(event.getKey(), null, event.getValue());
    }

    public void entryAdded(Object key, Object value) {
        Endpoint newEp = (Endpoint)value;
        if (!isLocal(newEp)) {
            logger.info(" Remote endpoint added: " + newEp);
        } 
        endpointAdded(newEp);
    }

    public void entryRemoved(Object key, Object value) {
        Endpoint oldEp = (Endpoint)value;
        if (!isLocal(oldEp)) {
            logger.info(" Remote endpoint removed: " + value);
        }
        endpointRemoved(oldEp);
    }

    public void entryUpdated(Object key, Object oldValue, Object newValue) {
        Endpoint oldEp = (Endpoint)oldValue;
        Endpoint newEp = (Endpoint)newValue;
        if (!isLocal(newEp)) {
            logger.info(" Remote endpoint updated: " + newEp);
        }
        endpointUpdated(oldEp, newEp);
    }

    public void memberAdded(MembershipEvent event) {
    }

    public void memberRemoved(MembershipEvent event) {
    }

}
