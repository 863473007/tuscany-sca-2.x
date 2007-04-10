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

package org.apache.tuscany.assembly.xml.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.assembly.AssemblyFactory;
import org.apache.tuscany.assembly.ConstrainingType;
import org.apache.tuscany.assembly.impl.DefaultAssemblyFactory;
import org.apache.tuscany.policy.PolicyFactory;
import org.apache.tuscany.policy.impl.DefaultPolicyFactory;
import org.apache.tuscany.services.spi.contribution.ArtifactResolver;
import org.apache.tuscany.services.spi.contribution.ContributionReadException;
import org.apache.tuscany.services.spi.contribution.ContributionResolveException;
import org.apache.tuscany.services.spi.contribution.ContributionWireException;
import org.apache.tuscany.services.spi.contribution.ContributionWriteException;
import org.apache.tuscany.services.spi.contribution.StAXArtifactProcessor;
import org.apache.tuscany.services.spi.contribution.URLArtifactProcessor;

/**
 * A contrainingType content handler.
 * 
 * @version $Rev$ $Date$
 */
public class ConstrainingTypeDocumentProcessor extends BaseArtifactProcessor implements URLArtifactProcessor<ConstrainingType> {
    private XMLInputFactory inputFactory;

    /**
     * Construct a new constrainingType processor.
     * @param factory
     * @param policyFactory
     * @param staxProcessor
     */
    public ConstrainingTypeDocumentProcessor(AssemblyFactory factory, PolicyFactory policyFactory, StAXArtifactProcessor staxProcessor, XMLInputFactory inputFactory) {
        super(factory, policyFactory, staxProcessor);
        this.inputFactory = inputFactory;
    }

    /**
     * Construct a new constrainingType processor.
     * @param staxProcessor
     */
    public ConstrainingTypeDocumentProcessor(StAXArtifactProcessor staxProcessor) {
        this(new DefaultAssemblyFactory(), new DefaultPolicyFactory(), staxProcessor, XMLInputFactory.newInstance());
    }

    public ConstrainingType read(URL url) throws ContributionReadException {
        InputStream urlStream = null;
        try {
            urlStream = url.openStream();
            XMLStreamReader reader = inputFactory.createXMLStreamReader(urlStream);
            reader.nextTag();
            ConstrainingType constrainingType = (ConstrainingType)extensionProcessor.read(reader);
            return constrainingType;
            
        } catch (XMLStreamException e) {
            throw new ContributionReadException(e);
        } catch (IOException e) {
            throw new ContributionReadException(e);
        } finally {
            try {
                if (urlStream != null) {
                    urlStream.close();
                    urlStream = null;
                }
            } catch (IOException ioe) {
                //ignore
            }
        }
    }
    
    public void write(ConstrainingType model, URL outputSource) throws ContributionWriteException {
        // Can't write to a URL
        throw new UnsupportedOperationException();
    }
    
    public void resolve(ConstrainingType constrainingType, ArtifactResolver resolver) throws ContributionResolveException {
        extensionProcessor.resolve(constrainingType, resolver);
    }
    
    public void wire(ConstrainingType constrainingType) throws ContributionWireException {
        extensionProcessor.wire(constrainingType);
    }

    public String getArtifactType() {
        return ".constrainingType";
    }
    
    public Class<ConstrainingType> getModelType() {
        return ConstrainingType.class;
    }
}
