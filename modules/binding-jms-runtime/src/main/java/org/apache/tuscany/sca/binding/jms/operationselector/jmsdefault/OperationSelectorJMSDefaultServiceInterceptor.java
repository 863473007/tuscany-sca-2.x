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
package org.apache.tuscany.sca.binding.jms.operationselector.jmsdefault;

import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.tuscany.sca.binding.jms.context.JMSBindingContext;
import org.apache.tuscany.sca.binding.jms.impl.JMSBinding;
import org.apache.tuscany.sca.binding.jms.impl.JMSBindingConstants;
import org.apache.tuscany.sca.binding.jms.impl.JMSBindingException;
import org.apache.tuscany.sca.binding.jms.provider.JMSMessageProcessor;
import org.apache.tuscany.sca.binding.jms.provider.JMSMessageProcessorUtil;
import org.apache.tuscany.sca.binding.jms.provider.JMSResourceFactory;
import org.apache.tuscany.sca.core.assembly.EndpointReferenceImpl;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.runtime.ReferenceParameters;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.apache.tuscany.sca.runtime.RuntimeWire;

/**
 * Policy handler to handle PolicySet related to Logging with the QName
 * {http://tuscany.apache.org/xmlns/sca/1.0/impl/java}LoggingPolicy
 *
 * @version $Rev$ $Date$
 */
public class OperationSelectorJMSDefaultServiceInterceptor implements Interceptor {
    
    private static final String ON_MESSAGE_METHOD_NAME = "onMessage";
    
    private Invoker next;
    private RuntimeWire runtimeWire;
    private JMSResourceFactory jmsResourceFactory;
    private JMSBinding jmsBinding;
    private JMSMessageProcessor requestMessageProcessor;
    private JMSMessageProcessor responseMessageProcessor;
    private RuntimeComponentService service;
    private List<Operation> serviceOperations;
    

    public OperationSelectorJMSDefaultServiceInterceptor(JMSBinding jmsBinding, JMSResourceFactory jmsResourceFactory, RuntimeWire runtimeWire) {
        super();
        this.jmsBinding = jmsBinding;
        this.runtimeWire = runtimeWire;
        this.jmsResourceFactory = jmsResourceFactory;
        this.requestMessageProcessor = JMSMessageProcessorUtil.getRequestMessageProcessor(jmsBinding);
        this.responseMessageProcessor = JMSMessageProcessorUtil.getResponseMessageProcessor(jmsBinding);
        this.service = (RuntimeComponentService)runtimeWire.getTarget().getContract();
        this.serviceOperations = service.getInterfaceContract().getInterface().getOperations();
    }
    
    public Message invoke(Message msg) {
        return next.invoke(invokeRequest(msg));
    }    
    
    public Message invokeRequest(Message msg) { 
        try {
            // get the jms context
            JMSBindingContext context = (JMSBindingContext)msg.getHeaders().get(JMSBindingConstants.MSG_CTXT_POSITION);
            javax.jms.Message jmsMsg = context.getJmsMsg();
            
            String operationName = requestMessageProcessor.getOperationName(jmsMsg);
            Operation operation = getTargetOperation(operationName);
            msg.setOperation(operation);
            
            ReferenceParameters parameters = msg.getFrom().getReferenceParameters();
            
            if (service.getInterfaceContract().getCallbackInterface() != null) {
                
                String callbackdestName = jmsMsg.getStringProperty(JMSBindingConstants.CALLBACK_Q_PROPERTY);
                if (callbackdestName == null && msg.getOperation().isNonBlocking()) {
                    // if the request has a replyTo but this service operation is oneway but the service uses callbacks
                    // then use the replyTo as the callback destination
                    Destination replyTo = jmsMsg.getJMSReplyTo();
                    if (replyTo != null) {
                        callbackdestName = (replyTo instanceof Queue) ? ((Queue)replyTo).getQueueName() : ((Topic)replyTo).getTopicName();
                    }
                }
    
                if (callbackdestName != null) {
                    // append "jms:" to make it an absolute uri so the invoker can determine it came in on the request
                    // as otherwise the invoker should use the uri from the service callback binding
                    parameters.setCallbackReference(new EndpointReferenceImpl("jms:" + callbackdestName));
                }
    
                String callbackID = jmsMsg.getStringProperty(JMSBindingConstants.CALLBACK_ID_PROPERTY);
                if (callbackID != null) {
                    parameters.setCallbackID(callbackID);
                }
            }        
            
            return msg;
        } catch (JMSException e) {
            throw new JMSBindingException(e);
        }
    }  
    
    protected Operation getTargetOperation(String operationName) {
        Operation operation = null;

        if (serviceOperations.size() == 1) {

            // SCA JMS Binding Specification - Rule 1.5.1 line 203
            operation = serviceOperations.get(0);

        } else if (operationName != null) {

            // SCA JMS Binding Specification - Rule 1.5.1 line 205
            for (Operation op : serviceOperations) {
                if (op.getName().equals(operationName)) {
                    operation = op;
                    break;
                }
            }

        } else {

            // SCA JMS Binding Specification - Rule 1.5.1 line 207
            for (Operation op : serviceOperations) {
                if (op.getName().equals(ON_MESSAGE_METHOD_NAME)) {
                    operation = op;
                    break;
                }
            }
        }

        if (operation == null) {
            throw new JMSBindingException("Can't find operation " + (operationName != null ? operationName : ON_MESSAGE_METHOD_NAME));
        }

        return operation;
    }
    
    public Invoker getNext() {
        return next;
    }

    public void setNext(Invoker next) {
        this.next = next;
    }    
   
}
