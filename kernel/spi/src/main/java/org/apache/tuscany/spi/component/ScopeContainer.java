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
package org.apache.tuscany.spi.component;

import org.apache.tuscany.spi.Lifecycle;
import org.apache.tuscany.spi.event.RuntimeEventListener;
import org.apache.tuscany.spi.model.Scope;


/**
 * Manages the lifecycle and visibility of instances associated with a an {@link AtomicComponent}.
 *
 * @version $Rev$ $Date$
 */
public interface ScopeContainer extends Lifecycle, RuntimeEventListener {

    /**
     * Returns the scope value representing the scope context
     */
    Scope getScope();

    /**
     * Registers a component with the scope component
     */
    void register(AtomicComponent component);

    /**
     * Returns an instance wrapper associated with the current scope context, creating one if necessary
     *
     * @param component the component
     * @throws TargetResolutionException
     */
    <T> InstanceWrapper<T> getWrapper(AtomicComponent component) throws TargetResolutionException;

    /**
     * Returns an implementation instance associated with the current scope context.
     * If no instance is found, a {@link TargetNotFoundException} is thrown.
     *
     * @throws TargetResolutionException
     */
    <T> InstanceWrapper<T> getAssociatedWrapper(AtomicComponent component) throws TargetResolutionException;

    /**
     * Return a wrapper after use (for example, after invoking the instance).
     *  
     * @param component the component
     */
    <T> void returnWrapper(AtomicComponent component, InstanceWrapper<T> wrapper) throws TargetDestructionException;

    /**
     * Returns an implementation instance associated with the current request context, creating one if necessary
     *
     * @throws TargetResolutionException
     */
    @Deprecated
    Object getInstance(AtomicComponent component) throws TargetResolutionException;

    /**
     * Returns an implementation instance associated with the current context. If no instance is found, a {@link
     * TargetNotFoundException} is thrown
     *
     * @throws TargetResolutionException
     */
    @Deprecated
    Object getAssociatedInstance(AtomicComponent component) throws TargetResolutionException;

    /**
     * Persists a new component implementation instance, equivalent to an insert or append operation
     *
     * @param component  the owning component
     * @param id         the id associated with the instance
     * @param instance   the instance to persist
     * @param expiration the expiration in milliseconds
     * @throws PersistenceException
     */
    void persistNew(AtomicComponent component, String id, Object instance, long expiration) throws PersistenceException;

    /**
     * Persists a component implementation instance, equivalent to an update operation
     *
     * @param component  the owning component
     * @param id         the id associated with the instance
     * @param instance   the instance to persist
     * @param expiration the expiration in milliseconds
     * @throws PersistenceException
     */
    void persist(AtomicComponent component, String id, Object instance, long expiration) throws PersistenceException;

    /**
     * Removes a component implementation instance associated with the current context from persistent storage
     *
     * @param component the owning component
     * @throws PersistenceException
     */
    void remove(AtomicComponent component) throws PersistenceException;
}
