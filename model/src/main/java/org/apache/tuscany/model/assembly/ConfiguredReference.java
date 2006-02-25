/**
 *
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tuscany.model.assembly;

import java.util.List;

/**
 * A configured reference associated with a particular usage.
 */
public interface ConfiguredReference extends ConfiguredPort {

    /**
     * Returns the {@link Reference} that is being configured.
     * @return the {@link Reference} that is being configured
     */
    Reference getReference();

    /**
     * Sets the {@link Reference} that is being configured.
     * @param reference the {@link Reference} that is being configured
     */
    void setReference(Reference reference);

    /**
     * Returns the list of configured services that are wired to this configured reference.
     * @return the list of configured services that are wired to this configured reference
     */
    List<ConfiguredService> getTargetConfiguredServices();

}
