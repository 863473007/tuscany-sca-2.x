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

package org.apache.tuscany.sca.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Base;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.assembly.EndpointReference;
import org.apache.tuscany.sca.assembly.Multiplicity;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.common.java.io.IOHelper;
import org.apache.tuscany.sca.context.CompositeContext;
import org.apache.tuscany.sca.contribution.Artifact;
import org.apache.tuscany.sca.contribution.Contribution;
import org.apache.tuscany.sca.contribution.ContributionMetadata;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.core.invocation.ExtensibleProxyFactory;
import org.apache.tuscany.sca.core.invocation.ProxyFactory;
import org.apache.tuscany.sca.core.invocation.ProxyFactoryExtensionPoint;
import org.apache.tuscany.sca.deployment.Deployer;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.ValidationException;
import org.apache.tuscany.sca.runtime.ActivationException;
import org.apache.tuscany.sca.runtime.CompositeActivator;
import org.apache.tuscany.sca.runtime.EndpointRegistry;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;
import org.oasisopen.sca.NoSuchServiceException;
import org.oasisopen.sca.ServiceRuntimeException;

public class NodeImpl implements Node {

    private String domainName;
    private Deployer deployer;
    private CompositeActivator compositeActivator;
    private EndpointRegistry endpointRegistry;
    private ExtensionPointRegistry extensionPointRegistry;
    private TuscanyRuntime tuscanyRuntime;
    private Map<String, InstalledContribution> locallyInstalledContributions = new HashMap<String, InstalledContribution>();
    
    private static Map<String, Node> allNodes = new HashMap<String, Node>();
    
    public NodeImpl(String domainName, Deployer deployer, CompositeActivator compositeActivator, EndpointRegistry endpointRegistry, ExtensionPointRegistry extensionPointRegistry, TuscanyRuntime tuscanyRuntime) {
        this.domainName = domainName;
        this.deployer = deployer;
        this.compositeActivator = compositeActivator;
        this.endpointRegistry = endpointRegistry;
        this.extensionPointRegistry = extensionPointRegistry;
        this.tuscanyRuntime = tuscanyRuntime;
        allNodes.put(domainName, this);
    }

    public String installContribution(String contributionURL) throws ContributionReadException, ActivationException, ValidationException {
        return installContribution(null, contributionURL, null, null, true);
    }

    public String installContribution(String uri, String contributionURL, String metaDataURL, List<String> dependentContributionURIs, boolean startDeployables) throws ContributionReadException, ActivationException, ValidationException {
        if (uri == null) {
            uri = getDefaultContributionURI(contributionURL);
        }
        // TODO: sort out deployables and exports
        org.apache.tuscany.sca.runtime.InstalledContribution ic = new org.apache.tuscany.sca.runtime.InstalledContribution();
        ic.setURI(uri);
        ic.setURL(contributionURL);
        endpointRegistry.installContribution(ic);
        if (startDeployables) {
            // TODO: sort out metadata and dependents in distributed
            localInstall(uri, contributionURL, metaDataURL, dependentContributionURIs, startDeployables);
        }
        return uri;
    }

    private void localInstall(String uri, String contributionURL, String metaDataURL, List<String> dependentContributionURIs, boolean startDeployables) throws ContributionReadException, ValidationException, ActivationException {
        Monitor monitor = deployer.createMonitor();
        Contribution contribution = deployer.loadContribution(IOHelper.createURI(uri), IOHelper.getLocationAsURL(contributionURL), monitor);
        monitor.analyzeProblems();
        if (metaDataURL != null) {
            mergeContributionMetaData(metaDataURL, contribution);
        }
        installContribution(contribution, dependentContributionURIs, startDeployables);
    }

    private void mergeContributionMetaData(String metaDataURL, Contribution contribution) throws ValidationException {
        ContributionMetadata metaData;
        Monitor monitor = deployer.createMonitor();
        try {
            metaData = deployer.loadXMLDocument(IOHelper.getLocationAsURL(metaDataURL), monitor);
        } catch (Exception e) {
            throw new ValidationException(e);
        }
        monitor.analyzeProblems();
        contribution.getDeployables().addAll(metaData.getDeployables());
        contribution.getImports().addAll(metaData.getImports());
        contribution.getExports().addAll(metaData.getExports());
    }
    
    public String installContribution(Contribution contribution, List<String> dependentContributionURIs, boolean startDeployables) throws ContributionReadException, ActivationException, ValidationException {
        InstalledContribution ic = new InstalledContribution(contribution.getURI(), contribution.getLocation(), contribution, dependentContributionURIs);
        locallyInstalledContributions.put(contribution.getURI(), ic);
        if (startDeployables) {
            for (Composite c : ic.getDefaultDeployables()) {
                startComposite(c, ic);
            }
        } else {
            contribution.getDeployables().clear();
            
            List<Contribution> dependentContributions = calculateDependentContributions(ic);

            Monitor monitor = deployer.createMonitor();
            try {
                deployer.resolve(contribution, dependentContributions, monitor);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            monitor.analyzeProblems();
        }
        return ic.getURI();
    }

    protected List<Contribution> calculateDependentContributions(InstalledContribution ic) {
        List<Contribution> dependentContributions = new ArrayList<Contribution>();
        if (ic.getDependentContributionURIs() != null) {
            // if the install specified dependent uris use just those contributions
            for (String uri : ic.getDependentContributionURIs()) {
                InstalledContribution dependee = locallyInstalledContributions.get(uri);
                if (dependee != null) {
                    dependentContributions.add(dependee.getContribution());
                }
            }
        } else {
            // otherwise use all available contributions for dependents
            for (InstalledContribution ics : locallyInstalledContributions.values()) {
                dependentContributions.add(ics.getContribution());
            }
        }
        return dependentContributions;
    }

    public String start(String contributionURI, Reader compositeXML) throws ContributionReadException, XMLStreamException, ActivationException, ValidationException {
        Monitor monitor = deployer.createMonitor();
        Composite composite = deployer.loadXMLDocument(compositeXML, monitor);
        monitor.analyzeProblems();
        return start(contributionURI, composite);
    }

    public String start(String contributionURI, Composite composite) throws ActivationException, ValidationException {
        InstalledContribution ic = locallyInstalledContributions.get(contributionURI);
        if (ic == null) {
            throw new IllegalArgumentException("contribution not installed: " + contributionURI);
        }
        String compositeArtifcatURI = deployer.attachDeploymentComposite(ic.getContribution(), composite, true);
        startComposite(composite, ic);
        return compositeArtifcatURI;
    }

    public void start(String contributionURI, String compositeURI) throws ActivationException, ValidationException, ContributionReadException {
        InstalledContribution ic = locallyInstalledContributions.get(contributionURI);
        if (ic == null) {
            
            org.apache.tuscany.sca.runtime.InstalledContribution icx = endpointRegistry.getInstalledContribution(contributionURI);
            String url = null;
            if (icx != null) {
                url  = icx.getURL();
            }
            if (url == null) {
                throw new IllegalArgumentException("Contribution not installed: " + contributionURI);
            }
            localInstall(contributionURI, url, null, null, false);
            ic = locallyInstalledContributions.get(contributionURI);
        }
        if (!ic.restart(compositeURI)) {
            for (Artifact a : ic.getContribution().getArtifacts()) {
                if (a.getURI().equals(compositeURI)) {
                    startComposite((Composite) a.getModel(), ic);
                    return;
                }
            }
            throw new IllegalArgumentException("composite not found: " + compositeURI);
        }
    }

    @Override
    public void stop(String contributionURI, String compositeURI) throws ActivationException {
        InstalledContribution ic = locallyInstalledContributions.get(contributionURI);
        if (ic == null) {
            throw new IllegalArgumentException("Contribution not installed: " + contributionURI);
        }
        ic.stop(compositeURI);
    }

    public Composite getDomainLevelComposite() {
        FactoryExtensionPoint factories = extensionPointRegistry.getExtensionPoint(FactoryExtensionPoint.class);
        AssemblyFactory assemblyFactory = factories.getFactory(AssemblyFactory.class);
        Composite domainComposite = assemblyFactory.createComposite();
        domainComposite.setName(new QName(Base.SCA11_TUSCANY_NS, "domainComposite"));
        domainComposite.setAutowire(false);
        domainComposite.setLocal(false);
        List<Composite> domainIncludes = domainComposite.getIncludes();
        Map<String, List<QName>> runningComposites = endpointRegistry.getRunningCompositeNames();
        for (String curi : runningComposites.keySet()) {
            for (QName name : runningComposites.get(curi)) {
                domainIncludes.add(endpointRegistry.getRunningComposite(curi, name));
            }
        }
        return domainComposite;
    }

    public String getDomainLevelCompositeAsString() {
        StAXArtifactProcessorExtensionPoint xmlProcessors = extensionPointRegistry.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        StAXArtifactProcessor<Composite> compositeProcessor = xmlProcessors.getProcessor(Composite.class);
        XMLOutputFactory outputFactory = extensionPointRegistry.getExtensionPoint(FactoryExtensionPoint.class).getFactory(XMLOutputFactory.class);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            XMLStreamWriter sw = outputFactory.createXMLStreamWriter(bos);
            compositeProcessor.write(getDomainLevelComposite(), sw, new ProcessorContext(extensionPointRegistry));
            sw.close();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return  bos.toString();
    }
    public Object getQNameDefinition(String contributionURI, QName definition, QName symbolSpace) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<String> removeContribution(String contributionURI) throws ActivationException {
        endpointRegistry.uninstallContribution(contributionURI);

        // TODO: should this next bit happen?
        List<String> removedContributionURIs = new ArrayList<String>();
        InstalledContribution ic = locallyInstalledContributions.get(contributionURI);
        if (ic != null) {
            removedContributionURIs.add(ic.getURI());
            for (String dependent : getDependentContributions(contributionURI)) {
                removedContributionURIs.addAll(removeContribution(dependent));
            }
            locallyInstalledContributions.remove(contributionURI);
            for (String compositeURI : ic.getStartedCompositeURIs()) {
                ic.stop(compositeURI);
            }
        }
        return removedContributionURIs;
    }

    public void updateContribution(String uri, String contributionURL) {
        // TODO Auto-generated method stub
        // is this just removeContribution/installContribution?
    }
    public void updateContribution(Contribution contribution) {
        // TODO Auto-generated method stub
    }

    public String updateDeploymentComposite(String uri, Reader compositeXML) {
        // TODO Auto-generated method stub
        // is this removeFromDomainLevelComposite/addDeploymentComposite
        return null;
    }
    public String updateDeploymentComposite(String uri, Composite composite) {
        // TODO Auto-generated method stub
        return null;
    }

    public void stop() {
        ArrayList<String> ics = new ArrayList<String>(locallyInstalledContributions.keySet());
        for (String uri : ics) {
            try {
                removeContribution(uri);
            } catch (Exception e) {
                // TODO: log
                e.printStackTrace();
            }
        }
        if (tuscanyRuntime != null) {
            tuscanyRuntime.stop();
        }
        allNodes.remove(this.domainName);
    }

    public <T> T getService(Class<T> interfaze, String serviceURI) throws NoSuchServiceException {

        List<Endpoint> endpoints = endpointRegistry.findEndpoint(serviceURI);
        if (endpoints.size() < 1) {
            throw new NoSuchServiceException(serviceURI);
        }

        String serviceName = null;
        if (serviceURI.contains("/")) {
            int i = serviceURI.indexOf("/");
            if (i < serviceURI.length() - 1) {
                serviceName = serviceURI.substring(i + 1);
            }
        }

        Endpoint ep = endpoints.get(0);
        if (((RuntimeComponent)ep.getComponent()).getComponentContext() != null) {
            return ((RuntimeComponent)ep.getComponent()).getServiceReference(interfaze, serviceName).getService();
        } else {
            return getRemoteProxy(interfaze, ep);
        }
    }

    private <T> T getRemoteProxy(Class<T> serviceInterface, Endpoint endpoint) throws NoSuchServiceException {
        FactoryExtensionPoint factories = extensionPointRegistry.getExtensionPoint(FactoryExtensionPoint.class);
        AssemblyFactory assemblyFactory = factories.getFactory(AssemblyFactory.class);
        JavaInterfaceFactory javaInterfaceFactory = factories.getFactory(JavaInterfaceFactory.class);
        ProxyFactory proxyFactory =
            new ExtensibleProxyFactory(extensionPointRegistry.getExtensionPoint(ProxyFactoryExtensionPoint.class));

        CompositeContext compositeContext =
            new CompositeContext(extensionPointRegistry, endpointRegistry, null, null, null,
                                 deployer.getSystemDefinitions());

        RuntimeEndpointReference epr;
        try {
            epr =
                createEndpointReference(javaInterfaceFactory,
                                        compositeContext,
                                        assemblyFactory,
                                        endpoint,
                                        serviceInterface);
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }

        return proxyFactory.createProxy(serviceInterface, epr);
    }

    private RuntimeEndpointReference createEndpointReference(JavaInterfaceFactory javaInterfaceFactory,
                                                             CompositeContext compositeContext,
                                                             AssemblyFactory assemblyFactory,
                                                             Endpoint endpoint,
                                                             Class<?> businessInterface)
        throws CloneNotSupportedException, InvalidInterfaceException {
        Component component = endpoint.getComponent();
        ComponentService service = endpoint.getService();
        ComponentReference componentReference = assemblyFactory.createComponentReference();
        componentReference.setName("sca.client." + service.getName());

        componentReference.setCallback(service.getCallback());
        componentReference.getTargets().add(service);
        componentReference.getPolicySets().addAll(service.getPolicySets());
        componentReference.getRequiredIntents().addAll(service.getRequiredIntents());
        componentReference.getBindings().add(endpoint.getBinding());

        InterfaceContract interfaceContract = service.getInterfaceContract();
        Service componentTypeService = service.getService();
        if (componentTypeService != null && componentTypeService.getInterfaceContract() != null) {
            interfaceContract = componentTypeService.getInterfaceContract();
        }
        interfaceContract = getInterfaceContract(javaInterfaceFactory, interfaceContract, businessInterface);
        componentReference.setInterfaceContract(interfaceContract);
        componentReference.setMultiplicity(Multiplicity.ONE_ONE);
        // component.getReferences().add(componentReference);

        // create endpoint reference
        EndpointReference endpointReference = assemblyFactory.createEndpointReference();
        endpointReference.setComponent(component);
        endpointReference.setReference(componentReference);
        endpointReference.setBinding(endpoint.getBinding());
        endpointReference.setUnresolved(false);
        endpointReference.setStatus(EndpointReference.Status.WIRED_TARGET_FOUND_AND_MATCHED);

        endpointReference.setTargetEndpoint(endpoint);

        componentReference.getEndpointReferences().add(endpointReference);
        ((RuntimeComponentReference)componentReference).setComponent((RuntimeComponent)component);
        ((RuntimeEndpointReference)endpointReference).bind(compositeContext);

        return (RuntimeEndpointReference)endpointReference;
    }

    private InterfaceContract getInterfaceContract(JavaInterfaceFactory javaInterfaceFactory,
                                                   InterfaceContract interfaceContract,
                                                   Class<?> businessInterface) throws CloneNotSupportedException,
        InvalidInterfaceException {
        if (businessInterface == null) {
            return interfaceContract;
        }
        boolean compatible = false;
        if (interfaceContract != null && interfaceContract.getInterface() != null) {
            Interface interfaze = interfaceContract.getInterface();
            if (interfaze instanceof JavaInterface) {
                Class<?> cls = ((JavaInterface)interfaze).getJavaClass();
                if (cls != null && businessInterface.isAssignableFrom(cls)) {
                    compatible = true;
                }
            }
        }

        if (!compatible) {
            // The interface is not assignable from the interface contract
            interfaceContract = javaInterfaceFactory.createJavaInterfaceContract();
            JavaInterface callInterface = javaInterfaceFactory.createJavaInterface(businessInterface);
            interfaceContract.setInterface(callInterface);
            if (callInterface.getCallbackClass() != null) {
                interfaceContract.setCallbackInterface(javaInterfaceFactory.createJavaInterface(callInterface
                    .getCallbackClass()));
            }
        }

        return interfaceContract;
    }
   
    public String getDomainName() {
        return domainName;
    }

    public List<String> getStartedCompositeURIs(String contributionURI) {
        InstalledContribution ic = locallyInstalledContributions.get(contributionURI);
        if (ic == null) {
            throw new IllegalArgumentException("no contribution found for: " + contributionURI);
        }
        return ic.getStartedCompositeURIs();
    }

    public List<String> getInstalledContributionURIs() {
        Set<String> ls = new HashSet<String>(endpointRegistry.getInstalledContributionURIs());
        ls.addAll(locallyInstalledContributions.keySet());
        return new ArrayList<String>(ls);
    }

    public Contribution getInstalledContribution(String uri) {
        if (locallyInstalledContributions.containsKey(uri)) {
            return locallyInstalledContributions.get(uri).getContribution();
        }
        throw new IllegalArgumentException("no contribution found for: " + uri);
    }

    protected String getContributionUriForArtifact(String artifactURI) {
        String contributionURI = null;
        for (String uri : locallyInstalledContributions.keySet()) {
            if (artifactURI.startsWith(uri)) {
                contributionURI = uri;
                break;
            }
        }
        if (contributionURI == null) {
            throw new IllegalArgumentException("no contribution found for: " + artifactURI);
        }
        return contributionURI;
    }

    protected void startComposite(Composite c, InstalledContribution ic) throws ActivationException, ValidationException {
        List<Contribution> dependentContributions = calculateDependentContributions(ic);
        DeployedComposite dc = new DeployedComposite(c, ic, dependentContributions, deployer, compositeActivator, endpointRegistry, extensionPointRegistry);
        ic.start(dc);
    }
    
    public Set<String> getDependentContributions(String contributionURI) {
        InstalledContribution ic = locallyInstalledContributions.get(contributionURI);
        if (ic == null) {
            throw new IllegalArgumentException("Contribution not installed: " + contributionURI);
        }
        Set<String> dependentContributionURIs = new HashSet<String>();
        for (InstalledContribution icx : locallyInstalledContributions.values()) {
            if (ic != icx) {
                List<Contribution> dependencies = icx.getContribution().getDependencies();
                if (dependencies != null && dependencies.contains(ic.getContribution())) {
                    dependentContributionURIs.addAll(getDependentContributions(icx.getURI()));
                }
            }
        }
        return dependentContributionURIs;
    }

    /**
     * Returns a default URI for a contribution based on the contribution URL
     */
    protected String getDefaultContributionURI(String contributionURL) {
        String uri = null;
        try {
            File f = new File(contributionURL);
            if ("classes".equals(f.getName()) && "target".equals(f.getParentFile().getName())) {
                uri = f.getParentFile().getParentFile().getName();                   
            } else {
                uri = f.getName();
            }
        } catch (Exception e) {
            // ignore
        }
        if (uri == null) {
            uri = contributionURL;
        }
        if (uri.endsWith(".zip") || uri.endsWith(".jar")) {
            uri = uri.substring(0, uri.length()-4);
        }
        return uri;
    }

    public EndpointRegistry getEndpointRegistry() {
        return endpointRegistry;
    }
    
    public static Node nodeExists(String domainName) {
        return allNodes.get(domainName);
    }

}
