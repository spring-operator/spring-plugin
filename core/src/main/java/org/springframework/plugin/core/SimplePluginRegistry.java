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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

/**
 * Basic implementation of {@link PluginRegistry}. Simply holds all given plugins in a list dropping {@literal null}
 * values silently on adding.
 *
 * @author Oliver Gierke
 */
public class SimplePluginRegistry<T extends Plugin<S>, S> extends PluginRegistrySupport<T, S> {

	/**
	 * Creates a new {@code SimplePluginRegistry}. Will create an empty registry if {@literal null} is provided.
	 *
	 * @param plugins must not be {@literal null}.
	 */
	protected SimplePluginRegistry(List<? extends T> plugins) {
		super(plugins);
	}

	/**
	 * Creates a new {@link SimplePluginRegistry}.
	 *
	 * @return
	 */
	public static <S, T extends Plugin<S>> SimplePluginRegistry<T, S> empty() {
		return of(Collections.emptyList());
	}

	/**
	 * Creates a new {@link SimplePluginRegistry} with the given {@link Plugin} s.
	 *
	 * @return
	 */
	@SafeVarargs
	public static <S, T extends Plugin<S>> SimplePluginRegistry<T, S> of(T... plugins) {
		return of(Arrays.asList(plugins));
	}

	/**
	 * Creates a new {@link SimplePluginRegistry} with the given {@link Plugin} s.
	 *
	 * @return
	 */
	public static <S, T extends Plugin<S>> SimplePluginRegistry<T, S> of(List<? extends T> plugins) {
		return new SimplePluginRegistry<>(plugins);
	}

	/**
	 * Creates a new {@link SimplePluginRegistry}.
	 *
	 * @return
	 * @deprecated use {@link #empty()} instead.
	 */
	@Deprecated
	public static <S, T extends Plugin<S>> SimplePluginRegistry<T, S> create() {
		return of(Collections.<T> emptyList());
	}

	/**
	 * Creates a new {@link SimplePluginRegistry} with the given {@link Plugin} s.
	 *
	 * @return
	 * @deprecated use {@link #of(List)} instead.
	 */
	@Deprecated
	public static <S, T extends Plugin<S>> SimplePluginRegistry<T, S> create(List<? extends T> plugins) {
		return new SimplePluginRegistry<>(plugins);
	}

	/* (non-Javadoc)
	 * @see org.springframework.plugin.core.PluginRegistrySupport#getPlugins()
	 */
	@Override
	public List<T> getPlugins() {
		return Collections.unmodifiableList(super.getPlugins());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.PluginRegistry#getPluginFor(java.lang.Object)
	 */
	@Override
	public Optional<T> getPluginFor(S delimiter) {

		Assert.notNull(delimiter, "Delimiter must not be null!");

		return super.getPlugins().stream()//
				.filter(it -> it.supports(delimiter))//
				.findFirst();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.PluginRegistry#getRequiredPluginFor(java.lang.Object)
	 */
	@Override
	public T getRequiredPluginFor(S delimiter) {

		Assert.notNull(delimiter, "Delimiter must not be null!");

		return getRequiredPluginFor(delimiter,
				() -> String.format("No plugin found for delimiter %s! Registered plugins: %s.", delimiter, getPlugins()));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.PluginRegistry#getRequiredPluginFor(java.lang.Object, java.util.function.Supplier)
	 */
	@Override
	public T getRequiredPluginFor(S delimiter, Supplier<String> message) throws IllegalArgumentException {

		Assert.notNull(delimiter, "Delimiter must not be null!");
		Assert.notNull(message, "Message must not be null!");

		return getPluginFor(delimiter, () -> new IllegalArgumentException(message.get()));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.PluginRegistry#getPluginsFor(java.lang.Object)
	 */
	@Override
	public List<T> getPluginsFor(S delimiter) {

		Assert.notNull(delimiter, "Delimiter must not be null!");

		return super.getPlugins().stream()//
				.filter(it -> it.supports(delimiter))//
				.collect(Collectors.toList());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.PluginRegistry#getPluginFor(java.lang.Object, org.springframework.plugin.core.PluginRegistry.Supplier)
	 */
	@Override
	public <E extends Exception> T getPluginFor(S delimiter, Supplier<E> ex) throws E {

		Assert.notNull(delimiter, "Delimiter must not be null!");
		Assert.notNull(ex, "Exception supplier must not be null!");

		return getPluginFor(delimiter).orElseThrow(ex);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.PluginRegistry#getPluginsFor(java.lang.Object, org.springframework.plugin.core.PluginRegistry.ExceptionProvider)
	 */
	@Override
	public <E extends Exception> List<T> getPluginsFor(S delimiter, Supplier<E> ex) throws E {

		Assert.notNull(delimiter, "Delimiter must not be null!");
		Assert.notNull(ex, "Exception supplier must not be null!");

		List<T> result = getPluginsFor(delimiter);

		if (result.isEmpty()) {
			throw ex.get();
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.PluginRegistry#getPluginOrDefaultFor(java.lang.Object, org.springframework.plugin.core.Plugin)
	 */
	@Override
	public T getPluginOrDefaultFor(S delimiter, T plugin) {
		return getPluginOrDefaultFor(delimiter, () -> plugin);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.PluginRegistry#getPluginOrDefaultFor(java.lang.Object, java.util.function.Supplier)
	 */
	@Override
	public T getPluginOrDefaultFor(S delimiter, Supplier<T> defaultSupplier) {

		Assert.notNull(delimiter, "Delimiter must not be null!");
		Assert.notNull(defaultSupplier, "Default supplier must not be null!");

		return getPluginFor(delimiter).orElseGet(defaultSupplier);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.PluginRegistry#getPluginsFor(java.lang.Object, java.util.List)
	 */
	@Override
	public List<T> getPluginsFor(S delimiter, List<? extends T> plugins) {

		Assert.notNull(delimiter, "Delimiter must not be null!");
		Assert.notNull(plugins, "Plugins must not be null!");

		List<T> candidates = getPluginsFor(delimiter);

		return candidates.isEmpty() ? new ArrayList<T>(plugins) : candidates;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.PluginRegistry#countPlugins()
	 */
	@Override
	public int countPlugins() {
		return super.getPlugins().size();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.PluginRegistry#contains(org.springframework.plugin.core.Plugin)
	 */
	@Override
	public boolean contains(T plugin) {
		return super.getPlugins().contains(plugin);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.PluginRegistry#hasPluginFor(java.lang.Object)
	 */
	@Override
	public boolean hasPluginFor(S delimiter) {
		return getPluginFor(delimiter).isPresent();
	}
}
