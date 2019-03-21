/*
 * Copyright 2008-2017 the original author or authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit test for {@link SimplePluginRegistry}.
 *
 * @author Oliver Gierke
 */
public class SimplePluginRegistryUnitTest {

	SamplePlugin plugin;

	SimplePluginRegistry<SamplePlugin, String> registry;

	public @Rule ExpectedException o_O = ExpectedException.none();

	/**
	 * Initializes a {@code PluginRegistry} and equips it with an {@code EmailNotificationProvider}.
	 */
	@Before
	public void setUp() {

		plugin = new SamplePluginImplementation();
		registry = SimplePluginRegistry.empty();
	}

	/**
	 * Asserts that the registry contains the plugin it was initialized with.
	 *
	 * @throws Exception
	 */
	@Test
	public void assertRegistryInitialized() throws Exception {

		registry = SimplePluginRegistry.of(plugin);

		assertThat(registry.countPlugins(), is(1));
		assertTrue(registry.contains(plugin));
	}

	/**
	 * Asserts asking for a plugin with the {@code PluginMetadata} provided by the {@link EmailNotificationProvider}.
	 */
	@Test
	public void assertFindsEmailNotificationProvider() {

		registry = SimplePluginRegistry.of(plugin);

		String delimiter = "FOO";

		List<SamplePlugin> plugins = registry.getPluginsFor(delimiter);
		assertThat(plugins, is(notNullValue()));
		assertThat(plugins.size(), is(1));

		SamplePlugin provider = plugins.get(0);
		assertThat(provider, is(instanceOf(SamplePluginImplementation.class)));
	}

	/**
	 * Expects the given exception to be thrown if no {@link Plugin} found.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionIfNoPluginFound() {

		registry.getPluginFor("BAR", () -> new IllegalArgumentException());
	}

	/**
	 * Expects the given exception to be thrown if no {@link Plugin}s found.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionIfNoPluginsFound() {

		registry.getPluginsFor("BAR", () -> new IllegalArgumentException());
	}

	/**
	 * Expect the defualt plugin to be returned if none found.
	 */
	@Test
	public void returnsDefaultIfNoneFound() {

		SamplePlugin defaultPlugin = new SamplePluginImplementation();

		assertThat(registry.getPluginOrDefaultFor("BAR", defaultPlugin), is(defaultPlugin));
	}

	/**
	 * Expect the given default plugins to be returned if none found.
	 */
	@Test
	public void returnsDefaultsIfNoneFound() {

		List<? extends SamplePlugin> defaultPlugins = Arrays.asList(new SamplePluginImplementation());

		List<SamplePlugin> result = registry.getPluginsFor("BAR", defaultPlugins);
		assertTrue(result.containsAll(defaultPlugins));
	}

	@Test
	public void handlesAddingNullPluginsCorrecty() throws Exception {

		List<SamplePlugin> plugins = new ArrayList<SamplePlugin>();
		plugins.add(null);

		registry = SimplePluginRegistry.of(plugins);

		assertThat(registry.countPlugins(), is(0));
	}

	/**
	 * @see #19
	 */
	@Test(expected = IllegalStateException.class)
	public void throwsExceptionFromSupplier() throws Exception {

		registry = SimplePluginRegistry.empty();

		registry.getPluginFor("FOO", () -> new IllegalStateException());
	}

	/**
	 * @see #41
	 */
	public void throwsExceptionIfRequiredPluginIsNotFound() {

		registry = SimplePluginRegistry.empty();

		o_O.expect(IllegalArgumentException.class);

		registry.getRequiredPluginFor("FOO");
	}

	/**
	 * @see #41
	 */
	public void throwsExceptionWithMessafeIfRequiredPluginIsNotFound() {

		registry = SimplePluginRegistry.of(Collections.emptyList());

		o_O.expect(IllegalArgumentException.class);
		o_O.expectMessage("message");

		registry.getRequiredPluginFor("FOO", () -> "message");
	}
}
