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

package org.apache.tuscany.sca.assembly.xml;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.apache.tuscany.sca.assembly.xml.Constants.ELEMENT;
import static org.apache.tuscany.sca.assembly.xml.Constants.MANY;
import static org.apache.tuscany.sca.assembly.xml.Constants.MULTIPLICITY;
import static org.apache.tuscany.sca.assembly.xml.Constants.MUST_SUPPLY;
import static org.apache.tuscany.sca.assembly.xml.Constants.NAME;
import static org.apache.tuscany.sca.assembly.xml.Constants.ONE_N;
import static org.apache.tuscany.sca.assembly.xml.Constants.ONE_ONE;
import static org.apache.tuscany.sca.assembly.xml.Constants.PROPERTY_QNAME;
import static org.apache.tuscany.sca.assembly.xml.Constants.SCA11_NS;
import static org.apache.tuscany.sca.assembly.xml.Constants.TARGET;
import static org.apache.tuscany.sca.assembly.xml.Constants.TYPE;
import static org.apache.tuscany.sca.assembly.xml.Constants.VALUE;
import static org.apache.tuscany.sca.assembly.xml.Constants.VALUE_QNAME;
import static org.apache.tuscany.sca.assembly.xml.Constants.ZERO_N;
import static org.apache.tuscany.sca.assembly.xml.Constants.ZERO_ONE;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMSource;

import org.apache.tuscany.sca.assembly.AbstractContract;
import org.apache.tuscany.sca.assembly.AbstractProperty;
import org.apache.tuscany.sca.assembly.AbstractReference;
import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Base;
import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.ComponentType;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.ConstrainingType;
import org.apache.tuscany.sca.assembly.Contract;
import org.apache.tuscany.sca.assembly.Extensible;
import org.apache.tuscany.sca.assembly.Extension;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.assembly.Multiplicity;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.contribution.processor.BaseStAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.ContributionWriteException;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.StAXAttributeProcessor;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;
import org.apache.tuscany.sca.policy.Intent;
import org.apache.tuscany.sca.policy.PolicyFactory;
import org.apache.tuscany.sca.policy.PolicySet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A base class with utility methods for the other artifact processors in this module. 
 * 
 * @version $Rev$ $Date$
 */
abstract class BaseAssemblyProcessor extends BaseStAXArtifactProcessor {

    protected AssemblyFactory assemblyFactory;
    protected PolicyFactory policyFactory;
    protected StAXArtifactProcessor<Object> extensionProcessor;
    protected PolicySubjectProcessor policyProcessor;
    private DocumentBuilderFactory documentBuilderFactory;
    private Monitor monitor;

    /**
     * Constructs a new BaseArtifactProcessor.
     * @param assemblyFactory
     * @param policyFactory
     */
    @SuppressWarnings("unchecked")
    protected BaseAssemblyProcessor(AssemblyFactory assemblyFactory,
                                    PolicyFactory policyFactory,
                                    DocumentBuilderFactory documentBuilderFactory,
                                    StAXArtifactProcessor extensionProcessor,
                                    Monitor monitor) {
        this.assemblyFactory = assemblyFactory;
        this.policyFactory = policyFactory;
        this.documentBuilderFactory = documentBuilderFactory;
        this.extensionProcessor = (StAXArtifactProcessor<Object>)extensionProcessor;
        this.policyProcessor = new PolicySubjectProcessor(policyFactory);
        this.monitor = monitor;
    }

    /**
     * @param modelFactories
     * @param staxProcessor
     * @param monitor
     */
    protected BaseAssemblyProcessor(FactoryExtensionPoint modelFactories,
                                    StAXArtifactProcessor staxProcessor,
                                    Monitor monitor) {
        this.assemblyFactory = modelFactories.getFactory(AssemblyFactory.class);
        this.policyFactory = modelFactories.getFactory(PolicyFactory.class);
        this.documentBuilderFactory = modelFactories.getFactory(DocumentBuilderFactory.class);
        this.extensionProcessor = (StAXArtifactProcessor<Object>)staxProcessor;
        this.policyProcessor = new PolicySubjectProcessor(policyFactory);
        this.monitor = monitor;
    }

    /**
     * Marshals warnings into the monitor
     * 
     * @param message
     * @param model
     * @param messageParameters
     */
    protected void warning(String message, Object model, String... messageParameters) {
        if (monitor != null) {
            Problem problem =
                monitor.createProblem(this.getClass().getName(),
                                      "assembly-xml-validation-messages",
                                      Severity.WARNING,
                                      model,
                                      message,
                                      (Object[])messageParameters);
            monitor.problem(problem);
        }
    }

    /**
     * Marshals errors into the monitor
     * 
     * @param problems
     * @param message
     * @param model
     */
    protected void error(String message, Object model, Object... messageParameters) {
        if (monitor != null) {
            Problem problem =
                monitor.createProblem(this.getClass().getName(),
                                      "assembly-xml-validation-messages",
                                      Severity.ERROR,
                                      model,
                                      message,
                                      (Object[])messageParameters);
            monitor.problem(problem);
        }
    }

    /**
     * Marshals exceptions into the monitor
     * 
     * @param problems
     * @param message
     * @param model
     */
    protected void error(String message, Object model, Exception ex) {
        if (monitor != null) {
            Problem problem =
                monitor.createProblem(this.getClass().getName(),
                                      "assembly-xml-validation-messages",
                                      Severity.ERROR,
                                      model,
                                      message,
                                      ex);
            monitor.problem(problem);
        }
    }

    /**
     * Start an element.
     * @param writer
     * @param name
     * @param attrs
     * @throws XMLStreamException
     */
    protected void writeStart(XMLStreamWriter writer, String name, XAttr... attrs) throws XMLStreamException {
        writeStart(writer, SCA11_NS, name, attrs);
    }

    /**
     * Start a document.
     * @param writer
     * @throws XMLStreamException
     */
    protected void writeStartDocument(XMLStreamWriter writer, String name, XAttr... attrs) throws XMLStreamException {
        writer.writeStartDocument();
        writer.setDefaultNamespace(SCA11_NS);
        writeStart(writer, SCA11_NS, name, attrs);
        writer.writeDefaultNamespace(SCA11_NS);
    }

    /**
     * Read list of reference targets
     * @param reference
     * @param reader
     */
    protected void readTargets(Reference reference, XMLStreamReader reader) {
        String value = reader.getAttributeValue(null, TARGET);
        ComponentService target = null;
        if (value != null) {
            for (StringTokenizer tokens = new StringTokenizer(value); tokens.hasMoreTokens();) {
                target = assemblyFactory.createComponentService();
                target.setUnresolved(true);
                target.setName(tokens.nextToken());
                reference.getTargets().add(target);
            }
        }
    }

    /**
     * Write a list of targets into an attribute
     * @param reference
     * @return
     */
    protected XAttr writeTargets(Reference reference) {
        List<String> targets = new ArrayList<String>();
        for (Service target : reference.getTargets()) {
            targets.add(target.getName());
        }
        return new XAttr(TARGET, targets);
    }

    /**
     * Read a multiplicity attribute.
     * @param reference
     * @param reader
     */
    protected void readMultiplicity(AbstractReference reference, XMLStreamReader reader) {
        String value = reader.getAttributeValue(null, MULTIPLICITY);
        if (ZERO_ONE.equals(value)) {
            reference.setMultiplicity(Multiplicity.ZERO_ONE);
        } else if (ONE_N.equals(value)) {
            reference.setMultiplicity(Multiplicity.ONE_N);
        } else if (ZERO_N.equals(value)) {
            reference.setMultiplicity(Multiplicity.ZERO_N);
        } else if (ONE_ONE.equals(value)) {
            reference.setMultiplicity(Multiplicity.ONE_ONE);
        }
    }

    protected XAttr writeMultiplicity(AbstractReference reference) {
        Multiplicity multiplicity = reference.getMultiplicity();
        if (multiplicity != null) {
            String value = null;
            if (Multiplicity.ZERO_ONE.equals(multiplicity)) {
                value = ZERO_ONE;
            } else if (Multiplicity.ONE_N.equals(multiplicity)) {
                value = ONE_N;
            } else if (Multiplicity.ZERO_N.equals(multiplicity)) {
                value = ZERO_N;
            } else if (Multiplicity.ONE_ONE.equals(multiplicity)) {
                value = ONE_ONE;
                return null;
            }
            return new XAttr(MULTIPLICITY, value);
        }
        return null;
    }

    /**
     * Returns the value of a constrainingType attribute.
     * @param reader
     * @return
     */
    protected ConstrainingType readConstrainingType(XMLStreamReader reader) {
        QName constrainingTypeName = getQName(reader, Constants.CONSTRAINING_TYPE);
        if (constrainingTypeName != null) {
            ConstrainingType constrainingType = assemblyFactory.createConstrainingType();
            constrainingType.setName(constrainingTypeName);
            constrainingType.setUnresolved(true);
            return constrainingType;
        } else {
            return null;
        }
    }

    /**
     * Reads an abstract property element.
     * @param property
     * @param reader
     * @throws XMLStreamException
     * @throws ContributionReadException
     */
    protected void readAbstractProperty(AbstractProperty property, XMLStreamReader reader) throws XMLStreamException,
        ContributionReadException {

        property.setName(getString(reader, NAME));
        property.setMany(getBoolean(reader, MANY));
        property.setMustSupply(getBoolean(reader, MUST_SUPPLY));
        property.setXSDElement(getQName(reader, ELEMENT));
        property.setXSDType(getQName(reader, TYPE));

    }

    /**
     * Resolve an implementation.
     * @param implementation
     * @param resolver
     * @return
     * @throws ContributionResolveException
     */
    protected Implementation resolveImplementation(Implementation implementation, ModelResolver resolver)
        throws ContributionResolveException {
        if (implementation != null) {
            if (implementation.isUnresolved()) {
                implementation = resolver.resolveModel(Implementation.class, implementation);

                // Lazily resolve implementations
                if (implementation.isUnresolved()) {
                    extensionProcessor.resolve(implementation, resolver);
                    if (!implementation.isUnresolved()) {
                        resolver.addModel(implementation);
                    }
                }
            }
        }
        return implementation;
    }

    /**
     * Resolve interface, callback interface and bindings on a list of contracts.
     * @param contracts the list of contracts
     * @param resolver the resolver to use to resolve models
     */
    protected <C extends Contract> void resolveContracts(List<C> contracts, ModelResolver resolver)
        throws ContributionResolveException {
        resolveContracts(null, contracts, resolver);
    }

    /**
     * Resolve interface, callback interface and bindings on a list of contracts.
     * @param parent element for the contracts
     * @param contracts the list of contracts
     * @param resolver the resolver to use to resolve models
     */
    protected <C extends Contract> void resolveContracts(Base parent, List<C> contracts, ModelResolver resolver)
        throws ContributionResolveException {

        String parentName =
            (parent instanceof Composite) ? ((Composite)parent).getName().toString() : (parent instanceof Component)
                ? ((Component)parent).getName() : "UNKNOWN";

        for (Contract contract : contracts) {
            // Resolve the interface contract
            InterfaceContract interfaceContract = contract.getInterfaceContract();
            if (interfaceContract != null) {
                extensionProcessor.resolve(interfaceContract, resolver);
            }

            // Resolve bindings
            for (int i = 0, n = contract.getBindings().size(); i < n; i++) {
                Binding binding = contract.getBindings().get(i);
                extensionProcessor.resolve(binding, resolver);

            }

            // Resolve callback bindings
            if (contract.getCallback() != null) {

                for (int i = 0, n = contract.getCallback().getBindings().size(); i < n; i++) {
                    Binding binding = contract.getCallback().getBindings().get(i);
                    extensionProcessor.resolve(binding, resolver);
                }
            }
        }
    }

    /**
     * Resolve interface and callback interface on a list of abstract contracts.
     * @param contracts the list of contracts
     * @param resolver the resolver to use to resolve models
     */
    protected <C extends AbstractContract> void resolveAbstractContracts(List<C> contracts, ModelResolver resolver)
        throws ContributionResolveException {
        for (AbstractContract contract : contracts) {

            // Resolve the interface contract
            InterfaceContract interfaceContract = contract.getInterfaceContract();
            if (interfaceContract != null) {
                extensionProcessor.resolve(interfaceContract, resolver);
            }
        }
    }

    /**
     * Returns a constrainingType attribute.
     * @param componentType
     * @return
     */
    protected XAttr writeConstrainingType(ComponentType componentType) {
        ConstrainingType constrainingType = componentType.getConstrainingType();
        if (constrainingType != null)
            return new XAttr(Constants.CONSTRAINING_TYPE, constrainingType.getName());
        else
            return null;
    }

    protected List<Extension> readPropertyValue(XMLStreamReader reader) throws XMLStreamException,
        ContributionReadException {
        List<Extension> values = new ArrayList<Extension>();
        QName name = reader.getName(); // Should be sca:property

        // SCA 1.1 supports the @value for simple types
        String valueAttr = getString(reader, VALUE);
        if (valueAttr != null) {
            Extension ext = assemblyFactory.createExtension();
            ext.setValue(valueAttr);
            ext.setQName(VALUE_QNAME);
            ext.setAttribute(true);
            values.add(ext);
        }

        boolean isTextForProperty = true;
        StringBuffer text = new StringBuffer();

        int event = reader.getEventType();
        while (true) {
            switch (event) {
                case START_ELEMENT:
                    name = reader.getName();
                    if (PROPERTY_QNAME.equals(name)) {
                        isTextForProperty = true;
                        continue;
                    }
                    isTextForProperty = false;
                    // Read <value>
                    if (VALUE_QNAME.equals(name)) {
                        Object value = extensionProcessor.read(reader);
                        // Assume the value is the XMLStreamReader for the content
                        Extension ext = assemblyFactory.createExtension();
                        ext.setValue(value);
                        ext.setQName(name);
                        values.add(ext);
                    } else {
                        // Global elements
                        // FIXME: do we want to check if the element mataches property.element
                        Object value = extensionProcessor.read(reader);
                        Extension ext = assemblyFactory.createExtension();
                        ext.setValue(value);
                        ext.setQName(name);
                        values.add(ext);
                    }
                    break;
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.CDATA:
                    if (isTextForProperty) {
                        text.append(reader.getText());
                    }
                    break;
                case END_ELEMENT:
                    name = reader.getName();
                    if (PROPERTY_QNAME.equals(name)) {
                        return values;
                    }
                    break;
            }
        }
    }
    
    /**
     * Read a property value into a DOM document.
     * @param element
     * @param type
     * @param reader
     * @return
     * @throws XMLStreamException
     * @throws ContributionReadException
     * @throws ParserConfigurationException 
     */
    protected Document readPropertyValue(QName element, QName type, XMLStreamReader reader) throws XMLStreamException,
        ContributionReadException {
        Document document;
        try {
            if (documentBuilderFactory == null) {
                documentBuilderFactory = DocumentBuilderFactory.newInstance();
                documentBuilderFactory.setNamespaceAware(true);
            }
            document = documentBuilderFactory.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            ContributionReadException ce = new ContributionReadException(e);
            error("ContributionReadException", documentBuilderFactory, ce);
            throw ce;
        }

        // root element has no namespace and local name "value"
        Element root = document.createElementNS(null, "value");
        if (type != null) {
            org.w3c.dom.Attr xsi = document.createAttributeNS(XMLNS_ATTRIBUTE_NS_URI, "xmlns:xsi");
            xsi.setValue(W3C_XML_SCHEMA_INSTANCE_NS_URI);
            root.setAttributeNodeNS(xsi);

            String prefix = type.getPrefix();
            if (prefix == null || prefix.length() == 0) {
                prefix = "ns";
            }

            declareNamespace(root, prefix, type.getNamespaceURI());

            org.w3c.dom.Attr xsiType = document.createAttributeNS(W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi:type");
            xsiType.setValue(prefix + ":" + type.getLocalPart());
            root.setAttributeNodeNS(xsiType);
        }
        document.appendChild(root);

        loadElement(reader, root);
        return document;
    }

    /**
     * Create a DOM element
     * @param document
     * @param name
     * @return
     */
    private Element createElement(Document document, QName name) {
        String prefix = name.getPrefix();
        String qname =
            (prefix != null && prefix.length() > 0) ? prefix + ":" + name.getLocalPart() : name.getLocalPart();
        return document.createElementNS(name.getNamespaceURI(), qname);
    }

    /**
     * Declare a namespace.
     * @param element
     * @param prefix
     * @param ns
     */
    private void declareNamespace(Element element, String prefix, String ns) {
        if (ns == null) {
            ns = "";
        }
        if (prefix == null) {
            prefix = "";
        }
        String qname = null;
        if ("".equals(prefix)) {
            qname = "xmlns";
        } else {
            qname = "xmlns:" + prefix;
        }
        Node node = element;
        boolean declared = false;
        while (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
            NamedNodeMap attrs = node.getAttributes();
            if (attrs == null) {
                break;
            }
            Node attr = attrs.getNamedItem(qname);
            if (attr != null) {
                declared = ns.equals(attr.getNodeValue());
                break;
            }
            node = node.getParentNode();
        }
        if (!declared) {
            org.w3c.dom.Attr attr = element.getOwnerDocument().createAttributeNS(XMLNS_ATTRIBUTE_NS_URI, qname);
            attr.setValue(ns);
            element.setAttributeNodeNS(attr);
        }
    }

    /**
     * Load a property value specification from an StAX stream into a DOM
     * Document. Only elements, text and attributes are processed; all comments
     * and other whitespace are ignored.
     * 
     * @param reader the stream to read from
     * @param root the DOM node to load
     * @throws javax.xml.stream.XMLStreamException
     */
    private void loadElement(XMLStreamReader reader, Element root) throws XMLStreamException {
        Document document = root.getOwnerDocument();
        Node current = root;
        while (true) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    QName name = reader.getName();
                    Element child = createElement(document, name);

                    // push the new element and make it the current one
                    current.appendChild(child);
                    current = child;

                    int count = reader.getNamespaceCount();
                    for (int i = 0; i < count; i++) {
                        String prefix = reader.getNamespacePrefix(i);
                        String ns = reader.getNamespaceURI(i);
                        declareNamespace(child, prefix, ns);
                    }

                    if (!"".equals(name.getNamespaceURI())) {
                        declareNamespace(child, name.getPrefix(), name.getNamespaceURI());
                    }

                    // add the attributes for this element
                    count = reader.getAttributeCount();
                    for (int i = 0; i < count; i++) {
                        String ns = reader.getAttributeNamespace(i);
                        String prefix = reader.getAttributePrefix(i);
                        String qname = reader.getAttributeLocalName(i);
                        String value = reader.getAttributeValue(i);
                        if (prefix != null && prefix.length() != 0) {
                            qname = prefix + ":" + qname;
                        }
                        child.setAttributeNS(ns, qname, value);
                        if (ns != null) {
                            declareNamespace(child, prefix, ns);
                        }
                    }

                    break;
                case XMLStreamConstants.CDATA:
                    current.appendChild(document.createCDATASection(reader.getText()));
                    break;
                case XMLStreamConstants.CHARACTERS:
                    current.appendChild(document.createTextNode(reader.getText()));
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    // if we are back at the root then we are done
                    if (current == root) {
                        return;
                    }

                    // pop the element off the stack
                    current = current.getParentNode();
            }
        }
    }

    /**
     * Write the value of a property 
     * @param document
     * @param element
     * @param type
     * @param writer
     * @throws XMLStreamException
     */
    protected void writePropertyValue(Object propertyValue, QName element, QName type, XMLStreamWriter writer)
        throws XMLStreamException {

        if (propertyValue instanceof Document) {
            Document document = (Document)propertyValue;
            NodeList nodeList = document.getDocumentElement().getChildNodes();

            for (int item = 0; item < nodeList.getLength(); ++item) {
                Node node = nodeList.item(item);
                int nodeType = node.getNodeType();
                if (nodeType == Node.ELEMENT_NODE) {
                    XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new DOMSource(node));

                    while (reader.hasNext()) {
                        switch (reader.next()) {
                            case XMLStreamConstants.START_ELEMENT:
                                QName name = reader.getName();
                                writer.writeStartElement(name.getPrefix(), name.getLocalPart(), name.getNamespaceURI());

                                int namespaces = reader.getNamespaceCount();
                                for (int i = 0; i < namespaces; i++) {
                                    String prefix = reader.getNamespacePrefix(i);
                                    String ns = reader.getNamespaceURI(i);
                                    writer.writeNamespace(prefix, ns);
                                }

                                if (!"".equals(name.getNamespaceURI())) {
                                    writer.writeNamespace(name.getPrefix(), name.getNamespaceURI());
                                }

                                // add the attributes for this element
                                namespaces = reader.getAttributeCount();
                                for (int i = 0; i < namespaces; i++) {
                                    String ns = reader.getAttributeNamespace(i);
                                    String prefix = reader.getAttributePrefix(i);
                                    String qname = reader.getAttributeLocalName(i);
                                    String value = reader.getAttributeValue(i);

                                    writer.writeAttribute(prefix, ns, qname, value);
                                }

                                break;
                            case XMLStreamConstants.CDATA:
                                writer.writeCData(reader.getText());
                                break;
                            case XMLStreamConstants.CHARACTERS:
                                writer.writeCharacters(reader.getText());
                                break;
                            case XMLStreamConstants.END_ELEMENT:
                                writer.writeEndElement();
                                break;
                        }
                    }
                } else {
                    writer.writeCharacters(node.getTextContent());
                }
            }
        }
    }

    protected void addInheritedIntents(List<Intent> sourceList, List<Intent> targetList) {
        if (sourceList != null) {
            targetList.addAll(sourceList);
        }
    }

    protected void addInheritedPolicySets(List<PolicySet> sourceList, List<PolicySet> targetList) {
        if (sourceList != null) {
            targetList.addAll(sourceList);
        }
    }

    /**
     * 
     * @param reader
     * @param elementName
     * @param estensibleElement
     * @param extensionAttributeProcessor
     * @throws ContributionReadException
     * @throws XMLStreamException
     */
    protected void readExtendedAttributes(XMLStreamReader reader,
                                          QName elementName,
                                          Extensible estensibleElement,
                                          StAXAttributeProcessor extensionAttributeProcessor)
        throws ContributionReadException, XMLStreamException {
        for (int a = 0; a < reader.getAttributeCount(); a++) {
            QName attributeName = reader.getAttributeName(a);
            if (attributeName.getNamespaceURI() != null && attributeName.getNamespaceURI().length() > 0) {
                if (!elementName.getNamespaceURI().equals(attributeName.getNamespaceURI())) {
                    Object attributeValue = extensionAttributeProcessor.read(attributeName, reader);
                    Extension attributeExtension;
                    if (attributeValue instanceof Extension) {
                        attributeExtension = (Extension)attributeValue;
                    } else {
                        attributeExtension = assemblyFactory.createExtension();
                        attributeExtension.setQName(attributeName);
                        attributeExtension.setAttribute(true);
                        attributeExtension.setValue(attributeValue);
                    }
                    estensibleElement.getAttributeExtensions().add(attributeExtension);
                }
            }
        }
    }

    /**
     * 
     * @param attributeModel
     * @param writer
     * @param extensibleElement
     * @param extensionAttributeProcessor
     * @throws ContributionWriteException
     * @throws XMLStreamException
     */
    protected void writeExtendedAttributes(XMLStreamWriter writer,
                                           Extensible extensibleElement,
                                           StAXAttributeProcessor extensionAttributeProcessor)
        throws ContributionWriteException, XMLStreamException {
        for (Extension extension : extensibleElement.getAttributeExtensions()) {
            if (extension.isAttribute()) {
                extensionAttributeProcessor.write(extension, writer);
            }
        }
    }

    /*protected void validatePolicySets(PolicySubject policySetAttachPoint) 
                                                            throws ContributionResolveException {
        validatePolicySets(policySetAttachPoint, policySetAttachPoint.getApplicablePolicySets());
    }
     
    
    protected void validatePolicySets(PolicySubject policySetAttachPoint,
                                      List<PolicySet> applicablePolicySets) throws ContributionResolveException {
        //Since the applicablePolicySets in a policySetAttachPoint will already have the 
        //list of policysets that might ever be applicable to this attachPoint, just check
        //if the defined policysets feature in the list of applicable policysets
        ExtensionType attachPointType = policySetAttachPoint.getType();
        for ( PolicySet definedPolicySet : policySetAttachPoint.getPolicySets() ) {
            if ( !definedPolicySet.isUnresolved() ) {
                if ( !applicablePolicySets.contains(definedPolicySet)) {
                    throw new ContributionResolveException("Policy Set '" + definedPolicySet.getName()
                                                           + "' does not apply to binding type  "
                                                           + attachPointType.getName());
                }
            } else {
                throw new ContributionResolveException("Policy Set '" + definedPolicySet.getName()
                                                       + "' is not defined in this domain  ");
                                                
            
            }
        }
    }*/
}
