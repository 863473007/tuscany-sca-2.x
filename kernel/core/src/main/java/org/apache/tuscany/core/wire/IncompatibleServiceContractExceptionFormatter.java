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
package org.apache.tuscany.core.wire;

import java.io.PrintWriter;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;

import org.apache.tuscany.spi.annotation.Autowire;
import org.apache.tuscany.spi.model.Operation;
import org.apache.tuscany.spi.model.ServiceContract;
import org.apache.tuscany.spi.wire.IncompatibleServiceContractException;

import org.apache.tuscany.host.monitor.ExceptionFormatter;
import org.apache.tuscany.host.monitor.FormatterRegistry;

/**
 * Formats {@link org.apache.tuscany.spi.wire.IncompatibleServiceContractException} for JDK logging
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class IncompatibleServiceContractExceptionFormatter implements ExceptionFormatter {
    private FormatterRegistry factory;

    public IncompatibleServiceContractExceptionFormatter(@Autowire FormatterRegistry factory) {
        this.factory = factory;
        factory.register(this);
    }

    public boolean canFormat(Class<?> type) {
        return IncompatibleServiceContractException.class.isAssignableFrom(type);
    }

    @Destroy
    public void destroy() {
        factory.unregister(this);
    }

    public PrintWriter write(PrintWriter writer, Throwable exception) {
        assert exception instanceof IncompatibleServiceContractException;
        IncompatibleServiceContractException e = (IncompatibleServiceContractException) exception;
        e.appendBaseMessage(writer);
        ServiceContract<?> source = e.getSource();
        String sourceContractName = null;
        if (source != null) {
            sourceContractName = source.getInterfaceName();
        }
        Operation<?> sourceOperation = e.getSourceOperation();
        String sourceOpName = null;
        if (sourceOperation != null) {
            sourceOpName = sourceOperation.getName();
        }
        if (sourceOpName == null) {
            writer.write("\nSource Contract: " + sourceContractName);
        } else {
            writer.write("\nSource Contract: " + sourceContractName + "/" + sourceOpName);
        }
        ServiceContract<?> target = e.getTarget();
        String targetContractName = null;
        if (target != null) {
            targetContractName = target.getInterfaceName();
        }
        Operation<?> targetOperation = e.getTargetOperation();
        String targetOpName = null;
        if (targetOperation != null) {
            targetOpName = targetOperation.getName();
        }
        if (targetOpName == null) {
            writer.write("\nTarget Contract: " + targetContractName + "\n");
        } else {
            writer.write("\nTarget Contract: " + targetContractName + "/" + targetOpName + "\n");

        }
        return writer;
    }
}
