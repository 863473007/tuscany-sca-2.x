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
package org.apache.tuscany.sca.binding.ws.axis2.provider;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.tuscany.sca.assembly.Endpoint;
import org.apache.tuscany.sca.binding.ws.WebServiceBinding;
import org.apache.tuscany.sca.interfacedef.util.FaultException;
import org.apache.tuscany.sca.invocation.DataExchangeSemantics;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.runtime.RuntimeEndpointReference;


/**
 * Axis2BindingInvoker creates an Axis2 OperationClient to pass down the 
 * binding chain
 *
 * @version $Rev$ $Date$
 */
public class Axis2ReferenceBindingInvoker implements Invoker, DataExchangeSemantics {
    public static final QName QNAME_WSA_FROM =
        new QName(AddressingConstants.Final.WSA_NAMESPACE, 
                  AddressingConstants.WSA_FROM,
                  AddressingConstants.WSA_DEFAULT_PREFIX);
    
    private RuntimeEndpointReference endpointReference;
    private ServiceClient serviceClient;
    private QName wsdlOperationName;
    private Options options;
    private SOAPFactory soapFactory;    
    private WebServiceBinding wsBinding;
    
    public Axis2ReferenceBindingInvoker(RuntimeEndpointReference endpointReference, 
                               ServiceClient serviceClient,
                               QName wsdlOperationName,
                               Options options,
                               SOAPFactory soapFactory,
                               WebServiceBinding wsBinding) {
        this.endpointReference = endpointReference;
        this.serviceClient = serviceClient;
        this.wsdlOperationName = wsdlOperationName;
        this.options = options;
        this.soapFactory = soapFactory;
        this.wsBinding = wsBinding;
    }
   
    public Message invoke(Message msg) {
        try {
            final OperationClient operationClient = createOperationClient(msg);
            msg.setBindingContext(operationClient);
            msg = endpointReference.getBindingInvocationChain().getHeadInvoker().invoke(msg);
             
        } catch (AxisFault e) {
            if (e.getDetail() != null ) {
                FaultException f = new FaultException(e.getMessage(), e.getDetail(), e);
                f.setFaultName(e.getDetail().getQName());
                msg.setFaultBody(f);
            } else {
                msg.setFaultBody(e);
            }
        } catch (Throwable e) {
            msg.setFaultBody(e);
        }       

        return msg;
    }

    @SuppressWarnings("deprecation")
    protected OperationClient createOperationClient(Message msg) throws AxisFault {
        SOAPEnvelope env = soapFactory.getDefaultEnvelope();
        Object[] args = (Object[])msg.getBody();
        if (args != null && args.length > 0) {
            SOAPBody body = env.getBody();
            for (Object bc : args) {
                if (bc instanceof OMElement) {
                    body.addChild((OMElement)bc);
                } else {
                    throw new IllegalArgumentException( "Can't handle mixed payloads between OMElements and other types.");
                }
            }
        }
        final MessageContext requestMC = new MessageContext();
        requestMC.setEnvelope(env);

        // Axis2 operationClients can not be shared so create a new one for each request
        final OperationClient operationClient = serviceClient.createClient(wsdlOperationName);
        operationClient.setOptions(options);

        Endpoint callbackEndpoint = msg.getFrom().getCallbackEndpoint();

        // add WS-Addressing header
        //FIXME: is there any way to use the Axis2 addressing support for this?
        if (callbackEndpoint != null) {
            EndpointReference fromEPR = new EndpointReference(callbackEndpoint.getURI());
            SOAPEnvelope sev = requestMC.getEnvelope();
            SOAPHeader sh = sev.getHeader();
            OMElement epr =
                EndpointReferenceHelper.toOM(sev.getOMFactory(),
                                             fromEPR,
                                             QNAME_WSA_FROM,
                                             AddressingConstants.Final.WSA_NAMESPACE);
            sh.addChild(epr);
            requestMC.setFrom(fromEPR);
        }

        // if target endpoint was not specified when this invoker was created, 
        // use dynamically specified target endpoint passed in on this call
        if (options.getTo() == null) {
            Endpoint ep = msg.getTo();
            if (ep != null) {
                requestMC.setTo(new EndpointReference(ep.getBinding().getURI()));
            } else {
                throw new RuntimeException("Unable to determine destination endpoint");
            }
        } else {
            requestMC.setTo(new EndpointReference(options.getTo().getAddress())); 
        }
        
        // Allow privileged access to read properties. Requires PropertiesPermission read in
        // security policy.
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws AxisFault {
                    operationClient.addMessageContext(requestMC);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw (AxisFault)e.getException();
        }
        return operationClient;
    }
    
    public boolean allowsPassByReference() {
        return true;
    }
}