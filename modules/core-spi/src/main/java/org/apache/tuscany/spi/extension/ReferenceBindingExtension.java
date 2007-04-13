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
package org.apache.tuscany.spi.extension;

import java.net.URI;

import org.apache.tuscany.interfacedef.InterfaceContract;
import org.apache.tuscany.spi.component.AbstractSCAObject;
import org.apache.tuscany.spi.component.ReferenceBinding;
import org.apache.tuscany.spi.wire.Wire;

/**
 * The default implementation of an SCA reference
 *
 * @version $Rev$ $Date$
 * @Deprecated
 */
public abstract class ReferenceBindingExtension extends AbstractSCAObject implements ReferenceBinding {
    protected Wire wire;
    protected InterfaceContract bindingServiceContract;
    protected URI targetUri;

    public ReferenceBindingExtension(URI name, URI targetUri) {
        super(name);
        this.targetUri = targetUri;
    }

    public InterfaceContract getBindingInterfaceContract() {
        return bindingServiceContract;
    }

    public Wire getWire() {
        return wire;
    }

    public void setWire(Wire wire) {
        this.wire = wire;
    }


    public URI getTargetUri() {
        return targetUri;
    }
}
