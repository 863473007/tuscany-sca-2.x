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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.contribution.processor.ContributionReadException;
import org.apache.tuscany.sca.contribution.processor.ContributionResolveException;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.URLArtifactProcessor;
import org.apache.tuscany.sca.contribution.processor.ValidatingXMLInputFactory;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.policy.PolicySet;
import org.apache.tuscany.sca.policy.util.PolicyComputationUtils;

/**
 * A composite processor.
 * 
 * @version $Rev$ $Date$
 */
public class CompositeDocumentProcessor extends BaseAssemblyProcessor implements URLArtifactProcessor<Composite> {
    private XMLInputFactory inputFactory;
    private DocumentBuilderFactory documentBuilderFactory;
    private Collection<PolicySet> domainPolicySets = null;
    private Monitor monitor;

    /**
     * Constructs a composite document processor
     * @param modelFactories
     * @param staxProcessor
     * @param monitor
     */
    public CompositeDocumentProcessor(FactoryExtensionPoint modelFactories,
                                      StAXArtifactProcessor<?> staxProcessor,
                                      Monitor monitor) {
        super(modelFactories, staxProcessor, monitor);
        this.inputFactory = modelFactories.getFactory(ValidatingXMLInputFactory.class);
        this.documentBuilderFactory = modelFactories.getFactory(DocumentBuilderFactory.class);
        this.monitor = monitor;
    }
    
    /**
     * Reads the contents of a Composite document and returns a Composite object
     * @param contributionURL - the URL of the contribution containing the Composite - can be null
     * @param uri - the URI of the composite document
     * @param url - the URL of the composite document
     * @return a Composite object built from the supplied Composite document
     */
    public Composite read(URL contributionURL, URI uri, URL url) throws ContributionReadException {
        InputStream scdlStream = null;
        try {
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            scdlStream = connection.getInputStream();
        } catch (IOException e) {
            ContributionReadException ce = new ContributionReadException(e);
            error("ContributionReadException", url, ce);
            throw ce;
        }
        return read(uri, scdlStream);
    }

    public Composite read(URI uri, InputStream scdlStream) throws ContributionReadException {
        try {       
            
            Composite composite = null;
            
            // Tag the monitor with the name of the composite artifact
            if( monitor != null ) {
            	monitor.setArtifactName(uri.toString());
            } //end if
            
            byte[] transformedArtifactContent;
            try {
                if ( domainPolicySets != null ) {
                    transformedArtifactContent =
                        PolicyComputationUtils.addApplicablePolicySets(scdlStream, domainPolicySets, documentBuilderFactory);
                    scdlStream = new ByteArrayInputStream(transformedArtifactContent);
                } 
            } catch ( IOException e ) {
            	ContributionReadException ce = new ContributionReadException(e);
            	error("ContributionReadException", scdlStream, ce);
            	throw ce;
            } catch ( Exception e ) {
            	ContributionReadException ce = new ContributionReadException(e);
            	error("ContributionReadException", scdlStream, ce);
                //throw ce;
            }
            
            XMLStreamReader reader = inputFactory.createXMLStreamReader(scdlStream);
            
            reader.nextTag();
            
            // Read the composite model
            composite = (Composite)extensionProcessor.read(reader);
            if (composite != null) {
                composite.setURI(uri.toString());
            }

            return composite;
            
        } catch (XMLStreamException e) {
        	ContributionReadException ce = new ContributionReadException(e);
        	error("ContributionReadException", inputFactory, ce);
            throw ce;
        } finally {
            try {
                if (scdlStream != null) {
                    scdlStream.close();
                    scdlStream = null;
                }
            } catch (IOException ioe) {
                //ignore
            }
        }
    }
    
    public void resolve(Composite composite, ModelResolver resolver) throws ContributionResolveException {
        if (composite != null)
    	    extensionProcessor.resolve(composite, resolver);
    }

    public String getArtifactType() {
        return ".composite";
    }
    
    public Class<Composite> getModelType() {
        return Composite.class;
    }
    
} // end class
