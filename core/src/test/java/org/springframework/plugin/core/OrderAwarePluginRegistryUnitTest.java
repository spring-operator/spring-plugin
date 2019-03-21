/*
 * Copyright 2008-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.plugin.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.springframework.plugin.core.PluginRegistry.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit test for {@link OrderAwarePluginRegistry} that especially concentrates on testing ordering functionality.
 *
 * @author Oliver Gierke
 */
public class OrderAwarePluginRegistryUnitTest extends SimplePluginRegistryUnitTest {

	TestPlugin firstPlugin;
	TestPlugin secondPlugin;

	@Override
	@Before
	public void setUp() {

		super.setUp();

		firstPlugin = new FirstImplementation();
		secondPlugin = new SecondImplementation();
	}

	@Test
	public void honorsOrderOnAddPlugins() throws Exception {

		PluginRegistry<TestPlugin, String> registry = of(firstPlugin, secondPlugin);
		assertOrder(registry, secondPlugin, firstPlugin);
	}

	@Test
	public void createsRevertedRegistryCorrectly() throws Exception {

		OrderAwarePluginRegistry<TestPlugin, String> registry = OrderAwarePluginRegistry.of(firstPlugin, secondPlugin);
		PluginRegistry<TestPlugin, String> reverse = registry.reverse();

		assertOrder(registry, secondPlugin, firstPlugin);
		assertOrder(reverse, firstPlugin, secondPlugin);
	}

	/**
	 * @see #1
	 */
	@Test
	public void considersJdkProxiedOrderedImplementation() {

		ThirdImplementation plugin = new ThirdImplementation();
		TestPlugin thirdPlugin = (TestPlugin) new ProxyFactory(plugin).getProxy();

		OrderAwarePluginRegistry<TestPlugin, String> registry = OrderAwarePluginRegistry.of(firstPlugin, secondPlugin,
				thirdPlugin);

		assertOrder(registry, secondPlugin, thirdPlugin, firstPlugin);
		assertOrder(registry.reverse(), firstPlugin, thirdPlugin, secondPlugin);
	}

	@Test
	public void defaultSetupUsesDefaultComparator() {
		assertDefaultComparator(OrderAwarePluginRegistry.empty());
	}

	@Test
	public void defaultSetupUsesDefaultReverseComparator() {

		OrderAwarePluginRegistry<Plugin<Object>, Object> registry = OrderAwarePluginRegistry
				.ofReverse(Collections.emptyList());
		Object field = ReflectionTestUtils.getField(registry, "comparator");

		assertThat(field, is(ReflectionTestUtils.getField(registry, "DEFAULT_REVERSE_COMPARATOR")));
	}

	private static void assertOrder(PluginRegistry<TestPlugin, String> registry, TestPlugin... plugins) {

		List<TestPlugin> result = registry.getPluginsFor("delimiter");

		assertThat(plugins.length, is(result.size()));

		for (int i = 0; i < plugins.length; i++) {
			assertThat(result.get(i), is(plugins[i]));
		}

		assertThat(registry.getPluginFor("delimiter"), is(Optional.of(plugins[0])));
	}

	private static void assertDefaultComparator(OrderAwarePluginRegistry<?, ?> registry) {

		Object field = ReflectionTestUtils.getField(registry, "comparator");
		assertThat(field, is(ReflectionTestUtils.getField(registry, "DEFAULT_COMPARATOR")));
	}

	private static interface TestPlugin extends Plugin<String> {

	}

	@Order(5)
	private static class FirstImplementation implements TestPlugin {

		/*
		 * (non-Javadoc)
		 *
		 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
		 */
		public boolean supports(String delimiter) {
			return true;
		}
	}

	@Order(1)
	private static class SecondImplementation implements TestPlugin {

		/*
		 * (non-Javadoc)
		 *
		 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
		 */
		public boolean supports(String delimiter) {
			return true;
		}
	}

	private static class ThirdImplementation implements TestPlugin, Ordered {

		@Override
		public int getOrder() {
			return 3;
		}

		@Override
		public boolean supports(String delimiter) {
			return true;
		}
	}
}
