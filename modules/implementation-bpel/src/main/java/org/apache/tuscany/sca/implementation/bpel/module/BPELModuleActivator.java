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

package org.apache.tuscany.sca.implementation.bpel.module;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.ModuleActivator;
import org.apache.tuscany.sca.implementation.bpel.BPELImplementationFactory;
import org.apache.tuscany.sca.implementation.bpel.DefaultBPELImplementationFactory;
import org.apache.tuscany.sca.implementation.bpel.impl.BPELArtifactProcessor;
import org.apache.tuscany.sca.implementation.bpel.ode.EmbeddedODEServer;
import org.apache.tuscany.sca.implementation.bpel.provider.BPELImplementationProviderFactory;
import org.apache.tuscany.sca.interfacedef.wsdl.DefaultWSDLFactory;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLFactory;
import org.apache.tuscany.sca.provider.ProviderFactoryExtensionPoint;

/**
 * Implements a module activator for the BPEL implementation extension module.
 * The module activator is responsible for contributing the BPEL implementation
 * extensions and plugging them in the extension points defined by the Tuscany
 * runtime.
 * 
 * @version $Rev$ $Date$
 */
public class BPELModuleActivator implements ModuleActivator {

    private EmbeddedODEServer odeServer;

    public Object[] getExtensionPoints() {
        // This module extension does not contribute any new extension point
        return null;
    }

    public void start(ExtensionPointRegistry registry) {

        // Create the CRUD implementation factory
        ModelFactoryExtensionPoint factories = registry.getExtensionPoint(ModelFactoryExtensionPoint.class);
        AssemblyFactory assemblyFactory = factories.getFactory(AssemblyFactory.class);

        WSDLFactory wsdlFactory = new DefaultWSDLFactory();

        BPELImplementationFactory bpelFactory =
                new DefaultBPELImplementationFactory(assemblyFactory, wsdlFactory);

        // Add the CRUD implementation extension to the StAXArtifactProcessor
        // extension point
        StAXArtifactProcessorExtensionPoint processors = registry.getExtensionPoint(StAXArtifactProcessorExtensionPoint.class);
        BPELArtifactProcessor implementationArtifactProcessor = new BPELArtifactProcessor(bpelFactory);
        processors.addArtifactProcessor(implementationArtifactProcessor);

        // Instantiating the ODE server to pass it to the providers
        odeServer = new EmbeddedODEServer();

        // Add the CRUD provider factory to the ProviderFactory extension point
        ProviderFactoryExtensionPoint providerFactories = registry.getExtensionPoint(ProviderFactoryExtensionPoint.class);
        providerFactories.addProviderFactory(new BPELImplementationProviderFactory(odeServer));
    }

    public void stop(ExtensionPointRegistry registry) {
    }
}
