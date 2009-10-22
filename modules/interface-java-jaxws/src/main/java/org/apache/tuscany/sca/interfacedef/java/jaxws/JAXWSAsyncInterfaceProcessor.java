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

package org.apache.tuscany.sca.interfacedef.java.jaxws;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.introspect.JavaInterfaceVisitor;

public class JAXWSAsyncInterfaceProcessor implements JavaInterfaceVisitor {
    private static String ASYNC = "Async";

    public JAXWSAsyncInterfaceProcessor(ExtensionPointRegistry registry) {
        
    }
    
    public void visitInterface(JavaInterface javaInterface) throws InvalidInterfaceException {
        List<Operation> validOperations = new ArrayList<Operation>();
        List<Operation> asyncOperations = new ArrayList<Operation>();
        
        validOperations.addAll(javaInterface.getOperations());
        for(Operation o : javaInterface.getOperations()) {
            if (! o.getName().endsWith(ASYNC)) {
                Operation op = o;
                
                for(Operation asyncOp : getAsyncOperations(javaInterface.getOperations(), o.getName()) ) {
                    if (isJAXWSAsyncPoolingOperation(op, asyncOp) ||
                        isJAXWSAsyncCallbackOperation(op, asyncOp)) {
                        validOperations.remove(asyncOp);
                        asyncOperations.add(asyncOp);
                    } 
                }
            } 
        }
        
        javaInterface.getOperations().clear();
        javaInterface.getOperations().addAll(validOperations);
        
        javaInterface.getAttributes().put("JAXWS-ASYNC-OPERATIONS", asyncOperations);
    }
    
    /**
     * The additional client-side asynchronous polling and callback methods defined by JAX-WS are recognized in a Java interface as follows:
     * For each method M in the interface, if another method P in the interface has
     * 
     * a) a method name that is M's method name with the characters "Async" appended, and
     * b) the same parameter signature as M, and
     * c)a return type of Response<R> where R is the return type of M
     * 
     * @param operation
     * @param asyncOperation
     * @return
     */
    private static boolean isJAXWSAsyncPoolingOperation(Operation operation, Operation asyncOperation) {
        //a method name that is M's method name with the characters "Async" appended
        if (operation.getName().endsWith(ASYNC)) {
            return false;
        }

        if (! asyncOperation.getName().endsWith(ASYNC)) {
            return false;
        }
        
        if(! asyncOperation.getName().equals(operation.getName() + ASYNC)) {
            return false;
        }
        
        //the same parameter signature as M
        List<DataType> operationInputType = operation.getInputType().getLogical();
        List<DataType> asyncOperationInputType = asyncOperation.getInputType().getLogical();
        int size = operationInputType.size();
        for (int i = 0; i < size; i++) {
            if (!isCompatible(operationInputType.get(i), asyncOperationInputType.get(i))) {
                return false;
            }
        }
        
        //a return type of Response<R> where R is the return type of M
        DataType<?> operationOutputType = operation.getOutputType();
        DataType<?> asyncOperationOutputType = asyncOperation.getOutputType();
        
        if (operationOutputType != null && asyncOperationOutputType != null) {
            ParameterizedType asyncReturnType =  (ParameterizedType) asyncOperationOutputType.getGenericType();
            Class<?> asyncReturnTypeClass = (Class<?>)asyncReturnType.getRawType();
            if(asyncReturnTypeClass.getName().equals("javax.xml.ws.Response")) {
                //now check the actual type of the Response<R> with R
                Class<?> returnType =  operationOutputType.getPhysical();
                Class<?> asyncActualReturnTypeClass = (Class<?>) asyncReturnType.getActualTypeArguments()[0];
            
                if(returnType == asyncActualReturnTypeClass ||
                    returnType.isPrimitive() && primitiveAssignable(returnType,asyncActualReturnTypeClass)) {
                    //valid
                } else { 
                   return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * For each method M in the interface, if another method C in the interface has
     * a) a method name that is M's method name with the characters "Async" appended, and
     * b) a parameter signature that is M's parameter signature with an additional 
     *    final parameter of type AsyncHandler<R> where R is the return type of M, and
     * c) a return type of Future<?>
     * 
     * then C is a JAX-WS callback method that isn't part of the SCA interface contract.
     * 
     * @param operation
     * @param asyncOperation
     * @return
     */
    private static boolean isJAXWSAsyncCallbackOperation(Operation operation, Operation asyncOperation) {
        //a method name that is M's method name with the characters "Async" appended
        if (operation.getName().endsWith(ASYNC)) {
            return false;
        }

        if (! asyncOperation.getName().endsWith(ASYNC)) {
            return false;
        }
        
        if(! asyncOperation.getName().equals(operation.getName() + ASYNC)) {
            return false;
        }
        
        //a parameter signature that is M's parameter signature 
        //with an additional final parameter of type AsyncHandler<R> where R is the return type of M, and
        List<DataType> operationInputType = operation.getInputType().getLogical();
        List<DataType> asyncOperationInputType = asyncOperation.getInputType().getLogical();
        int size = operationInputType.size();
        for (int i = 0; i < size; i++) {
            if (!isCompatible(operationInputType.get(i), asyncOperationInputType.get(i))) {
                return false;
            }
        }
        
        if(asyncOperationInputType.size() == size + 1) {
            ParameterizedType asyncLastParameterType =  (ParameterizedType) asyncOperationInputType.get(size + 1).getGenericType();
            Class<?> asyncLastParameterTypeClass = (Class<?>)asyncLastParameterType.getRawType();
            if(asyncLastParameterTypeClass.getName().equals("javax.xml.ws.AsyncHandler")) {
              //now check the actual type of the AsyncHandler<R> with R
                Class<?> returnType =  operation.getOutputType().getPhysical();
                Class<?> asyncActualLastParameterTypeClass = (Class<?>) asyncLastParameterType.getActualTypeArguments()[0];
            
                if(returnType == asyncActualLastParameterTypeClass ||
                    returnType.isPrimitive() && primitiveAssignable(returnType,asyncActualLastParameterTypeClass)) {
                    //valid
                } else {
                    return false;
                }
            }            
        }
        
        //a return type of Response<R> where R is the return type of M
        DataType<?> operationOutputType = operation.getOutputType();
        DataType<?> asyncOperationOutputType = asyncOperation.getOutputType();
        
        if (operationOutputType != null && asyncOperationOutputType != null) {
            ParameterizedType asyncReturnType =  (ParameterizedType) asyncOperationOutputType.getGenericType();
            Class<?> asyncReturnTypeClass = (Class<?>)asyncReturnType.getRawType();
            if(asyncReturnTypeClass.getName().equals("javax.xml.ws.Response")) {
                //now check the actual type of the Response<R> with R
                Class<?> returnType =  operationOutputType.getPhysical();
                Class<?> asyncActualReturnTypeClass = (Class<?>) asyncReturnType.getActualTypeArguments()[0];
            
                if(returnType == asyncActualReturnTypeClass ||
                    returnType.isPrimitive() && primitiveAssignable(returnType,asyncActualReturnTypeClass)) {
                    //valid
                } else {
                    return false;
                }
            }
        }
        
        return true;
    }    
    
    /**
     * Get operation by name
     * 
     * @param operations
     * @param operationName
     * @return
     */
    private static List<Operation> getAsyncOperations(List<Operation> operations, String operationName) {
        List<Operation> returnOperations = new ArrayList<Operation>();

        for(Operation o : operations) {
            if(o.getName().equals(operationName + ASYNC)) {
                returnOperations.add(o);
            }
        }
        
        return returnOperations;
    }
    

    /**
     * Check if two operation parameters are compatible
     * 
     * @param source
     * @param target
     * @return
     */
    private static boolean isCompatible(DataType<?> source, DataType<?> target) {
        if (source == target) {
            return true;
        }

        return target.getPhysical().isAssignableFrom(source.getPhysical());
    }

    /**
     * Compares a two types, assuming one is a primitive, to determine if the
     * other is its object counterpart
     */
    private static boolean primitiveAssignable(Class<?> memberType, Class<?> param) {
        if (memberType == Integer.class) {
            return param == Integer.TYPE;
        } else if (memberType == Double.class) {
            return param == Double.TYPE;
        } else if (memberType == Float.class) {
            return param == Float.TYPE;
        } else if (memberType == Short.class) {
            return param == Short.TYPE;
        } else if (memberType == Character.class) {
            return param == Character.TYPE;
        } else if (memberType == Boolean.class) {
            return param == Boolean.TYPE;
        } else if (memberType == Byte.class) {
            return param == Byte.TYPE;
        } else if (param == Integer.class) {
            return memberType == Integer.TYPE;
        } else if (param == Double.class) {
            return memberType == Double.TYPE;
        } else if (param == Float.class) {
            return memberType == Float.TYPE;
        } else if (param == Short.class) {
            return memberType == Short.TYPE;
        } else if (param == Character.class) {
            return memberType == Character.TYPE;
        } else if (param == Boolean.class) {
            return memberType == Boolean.TYPE;
        } else if (param == Byte.class) {
            return memberType == Byte.TYPE;
        } else {
            return false;
        }
    }
}