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
package org.apache.tuscany.sca.implementation.osgi.xml;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static org.apache.tuscany.sca.implementation.osgi.OSGiImplementation.BUNDLE_SYMBOLICNAME;
import static org.apache.tuscany.sca.implementation.osgi.OSGiImplementation.BUNDLE_VERSION;
import static org.apache.tuscany.sca.implementation.osgi.OSGiImplementation.IMPLEMENTATION_OSGI;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.ComponentType;
import org.apache.tuscany.sca.assembly.Property;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.ClassReference;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.implementation.osgi.OSGiImplementation;
import org.apache.tuscany.sca.implementation.osgi.OSGiImplementationFactory;
import org.apache.tuscany.sca.implementation.osgi.OSGiProperty;
import org.apache.tuscany.sca.implementation.osgi.runtime.OSGiImplementationActivator;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

/**
 * 
 * Process an <implementation.osgi/> element in a component definition. An instance of
 * OSGiImplementation is created.
 * Also associates the component type file with the implementation.
 *
 * @version $Rev$ $Date$
 */
public class OSGiImplementationProcessor implements StAXArtifactProcessor<OSGiImplementation> {
    private JavaInterfaceFactory javaInterfaceFactory;
    private AssemblyFactory assemblyFactory;
    private FactoryExtensionPoint modelFactories;
    private OSGiImplementationFactory osgiImplementationFactory;
    private Monitor monitor;

    public OSGiImplementationProcessor(FactoryExtensionPoint modelFactories, Monitor monitor) {
        this.monitor = monitor;
        this.modelFactories = modelFactories;
        this.assemblyFactory = modelFactories.getFactory(AssemblyFactory.class);
        this.osgiImplementationFactory = modelFactories.getFactory(OSGiImplementationFactory.class);
        this.javaInterfaceFactory = modelFactories.getFactory(JavaInterfaceFactory.class);
    }

    /**
     * Report a exception.
     * 
     * @param problems
     * @param message
     * @param model
     */
    private void error(String message, Object model, Exception ex) {
        if (monitor != null) {
            Problem problem =
                monitor.createProblem(this.getClass().getName(),
                                      "impl-osgi-validation-messages",
                                      Severity.ERROR,
                                      model,
                                      message,
                                      ex);
            monitor.problem(problem);
        }
    }

    /**
     * Report a error.
     * 
     * @param problems
     * @param message
     * @param model
     */
    private void error(String message, Object model, Object... messageParameters) {
        if (monitor != null) {
            Problem problem =
                monitor.createProblem(this.getClass().getName(),
                                      "impl-osgi-validation-messages",
                                      Severity.ERROR,
                                      model,
                                      message,
                                      (Object[])messageParameters);
            monitor.problem(problem);
        }
    }

    public QName getArtifactType() {
        return IMPLEMENTATION_OSGI;
    }

    public Class<OSGiImplementation> getModelType() {
        return OSGiImplementation.class;
    }

    private String[] tokenize(String str) {
        StringTokenizer tokenizer = new StringTokenizer(str);
        String[] tokens = new String[tokenizer.countTokens()];
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokenizer.nextToken();
        }

        return tokens;
    }

    public OSGiImplementation read(XMLStreamReader reader) throws ContributionReadException, XMLStreamException {
        assert IMPLEMENTATION_OSGI.equals(reader.getName());

        String bundleSymbolicName = reader.getAttributeValue(null, BUNDLE_SYMBOLICNAME);
        String bundleVersion = reader.getAttributeValue(null, BUNDLE_VERSION);

        List<OSGiProperty> refProperties = new ArrayList<OSGiProperty>();
        List<OSGiProperty> serviceProperties = new ArrayList<OSGiProperty>();
        List<OSGiProperty> refCallbackProperties = new ArrayList<OSGiProperty>();
        List<OSGiProperty> serviceCallbackProperties = new ArrayList<OSGiProperty>();

        // Skip to the end of <implementation.osgi>
        while (reader.hasNext()) {
            int next = reader.next();
            if (next == END_ELEMENT && IMPLEMENTATION_OSGI.equals(reader.getName())) {
                break;
            }
        }

        OSGiImplementation implementation = osgiImplementationFactory.createOSGiImplementation();
        implementation.setBundleSymbolicName(bundleSymbolicName);
        implementation.setBundleVersion(bundleVersion);

        implementation.setUnresolved(true);

        return implementation;

    }

    public void resolve(OSGiImplementation impl, ModelResolver resolver) throws ContributionResolveException {

        if (impl == null || !impl.isUnresolved())
            return;

        impl.setUnresolved(false);

        BundleContext bundleContext = OSGiImplementationActivator.getBundleContext();
        Bundle bundle = null;
        for (Bundle b : bundleContext.getBundles()) {
            String sn = b.getSymbolicName();
            String ver = (String)b.getHeaders().get(BUNDLE_VERSION);
            if (!impl.getBundleSymbolicName().equals(sn)) {
                continue;
            }
            Version v1 = Version.parseVersion(ver);
            Version v2 = Version.parseVersion(impl.getBundleVersion());
            if (v1.equals(v2)) {
                bundle = b;
                break;
            }
        }
        if (bundle != null) {
            impl.setBundle(bundle);
        } else {
            error("CouldNotLocateOSGiBundle", impl, impl.getBundleSymbolicName());
            //throw new ContributionResolveException("Could not locate OSGi bundle " + 
            //impl.getBundleSymbolicName());
            return;
        }

        ComponentType componentType = assemblyFactory.createComponentType();
        componentType.setURI("OSGI-INF/sca/bundle.componentType");
        componentType.setUnresolved(true);
        componentType = resolver.resolveModel(ComponentType.class, componentType);
        if (componentType.isUnresolved()) {
            error("MissingComponentTypeFile", impl, componentType.getURI());
            //throw new ContributionResolveException("missing .componentType side file " + ctURI);
            return;
        }

        List<Service> services = componentType.getServices();
        for (Service service : services) {
            Interface interfaze = service.getInterfaceContract().getInterface();
            if (interfaze instanceof JavaInterface) {
                JavaInterface javaInterface = (JavaInterface)interfaze;
                if (javaInterface.getJavaClass() == null) {
                    javaInterface.setJavaClass(getJavaClass(resolver, javaInterface.getName()));
                }
                Class<?> callback = null;
                if (service.getInterfaceContract().getCallbackInterface() instanceof JavaInterface) {
                    JavaInterface callbackInterface =
                        (JavaInterface)service.getInterfaceContract().getCallbackInterface();
                    if (callbackInterface.getJavaClass() == null) {
                        callbackInterface.setJavaClass(getJavaClass(resolver, callbackInterface.getName()));
                    }
                    callback = callbackInterface.getJavaClass();
                }

                impl.getServices().add(service);
            }
        }

        List<Reference> references = componentType.getReferences();
        for (Reference reference : references) {
            Interface interfaze = reference.getInterfaceContract().getInterface();
            if (interfaze instanceof JavaInterface) {
                JavaInterface javaInterface = (JavaInterface)interfaze;
                if (javaInterface.getJavaClass() == null) {
                    javaInterface.setJavaClass(getJavaClass(resolver, javaInterface.getName()));
                }
                impl.getReferences().add(reference);
            } else
                impl.getReferences().add(reference);
        }

        List<Property> properties = componentType.getProperties();
        for (Property property : properties) {
            impl.getProperties().add(property);
        }
        impl.setConstrainingType(componentType.getConstrainingType());

    }

    private Class getJavaClass(ModelResolver resolver, String className) {
        ClassReference ref = new ClassReference(className);
        ref = resolver.resolveModel(ClassReference.class, ref);
        return ref.getJavaClass();
    }

    public void write(OSGiImplementation model, XMLStreamWriter outputSource) throws ContributionWriteException,
        XMLStreamException {

        //FIXME Implement this method
    }

    private QName getQNameValue(XMLStreamReader reader, String value) {
        if (value != null) {
            int index = value.indexOf(':');
            String prefix = index == -1 ? "" : value.substring(0, index);
            String localName = index == -1 ? value : value.substring(index + 1);
            String ns = reader.getNamespaceContext().getNamespaceURI(prefix);
            if (ns == null) {
                ns = "";
            }
            return new QName(ns, localName, prefix);
        } else {
            return null;
        }
    }

}
