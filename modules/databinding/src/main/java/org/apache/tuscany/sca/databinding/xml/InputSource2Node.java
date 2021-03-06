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
package org.apache.tuscany.sca.databinding.xml;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.databinding.BaseTransformer;
import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Push DOM InputSource to Node
 *
 * @version $Rev$ $Date$
 */
public class InputSource2Node extends BaseTransformer<InputSource, Node> implements
    PullTransformer<InputSource, Node> {
    
    private final Source2ResultTransformer TRANSFORMER;
    
    public InputSource2Node(ExtensionPointRegistry registry) {
        super();
        this.TRANSFORMER = new Source2ResultTransformer(registry);
    }
    
    public Node transform(InputSource source, TransformationContext context) {
        try {
            Source streamSource = new StreamSource(source.getCharacterStream());
            DOMResult result = new DOMResult();
            TRANSFORMER.transform(streamSource, result, context);
            return result.getNode();
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

    @Override
    public Class<InputSource> getSourceType() {
        return InputSource.class;
    }

    @Override
    public Class<Node> getTargetType() {
        return Node.class;
    }

    @Override
    public int getWeight() {
        return 40;
    }

}
