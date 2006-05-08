/**
 * 
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.tuscany.container.java.integration.binding;

import junit.framework.TestCase;

/**
 * Tests basic Java to external service interaction
 *
 * @version $Rev$ $Date$
 */
public class JavaToExternalServiceTestCase extends TestCase {

    /**
     * Tests an wire of an external service configured with the {@link org.apache.tuscany.container.java.mock.binding.foo.FooBinding}
     * from a Java component
     *
     * @throws Exception
     */
    public void testJavaToESInvoke() throws Exception {
//        RuntimeContext runtime = MockFactory.registerFooBinding(MockFactory.createJavaRuntime());
//        PolicyBuilderRegistry registry = (PolicyBuilderRegistry) ((CompositeContext) runtime.getSystemContext().getContext(
//                MockFactory.SYSTEM_CHILD)).getContext(MockFactory.POLICY_BUILDER_REGISTRY).getInstance(null);
//        MockSyncInterceptor mockInterceptor = new MockSyncInterceptor();
//        MockInterceptorBuilder interceptorBuilder = new MockInterceptorBuilder(mockInterceptor, false);
//        registry.registerTargetBuilder(interceptorBuilder);
//        runtime.getRootContext().registerModelObject(MockFactory.createCompositeComponent("test.module"));
//        CompositeContext child = (CompositeContext) runtime.getRootContext().getContext("test.module");
//        child.registerModelObject(MockFactory.createModuleWithExternalService());
//        child.publish(new ModuleStart(this));
//        HelloWorldService source = (HelloWorldService) child.getContext("source").getInstance(null);
//        Assert.assertNotNull(source);
//        Assert.assertEquals(0, mockInterceptor.getCount());
//        Assert.assertEquals("foo", source.hello("foo"));
//        Assert.assertEquals(1, mockInterceptor.getCount());
//        child.publish(new ModuleStop(this));
//        runtime.stop();
    }

}
