/*
 * Copyright 2008-2012 the original author or authors.
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.comparator.InvertibleComparator;

/**
 * {@link PluginRegistry} implementation that be made aware of a certain ordering of {@link Plugin}s. By default it
 * orders {@link Plugin}s by regarding {@link org.springframework.core.Ordered} interface or
 * {@link org.springframework.core.annotation.Order} annotation. To alter ordering behaviour use one of the factory
 * methods accepting a {@link Comparator} as parameter.
 * 
 * @author Oliver Gierke
 */
public class OrderAwarePluginRegistry<T extends Plugin<S>, S> extends SimplePluginRegistry<T, S> {

	/**
	 * Comparator regarding {@link org.springframework.core.Ordered} interface or
	 * {@link org.springframework.core.annotation.Order} annotation.
	 */
	private static final Comparator<Object> DEFAULT_COMPARATOR = new AnnotationAwareOrderComparator();

	/**
	 * Comparator reverting the {@value #DEFAULT_COMPARATOR}.
	 */
	private static final Comparator<Object> DEFAULT_REVERSE_COMPARATOR = new InvertibleComparator<Object>(
			DEFAULT_COMPARATOR, false);

	private Comparator<? super T> comparator;

	/**
	 * Creates a new {@link OrderAwarePluginRegistry} with the given {@link Plugin}s and {@link Comparator}.
	 * 
	 * @param plugins the {@link Plugin}s to be contained in the registry or {@literal null} if the registry shall be
	 *          empty initally.
	 * @param comparator the {@link Comparator} to be used for ordering the {@link Plugin}s or {@literal null} if the
	 *          {@code #DEFAULT_COMPARATOR} shall be used.
	 */
	@SuppressWarnings("unchecked")
	protected OrderAwarePluginRegistry(List<? extends T> plugins, Comparator<? super T> comparator) {

		super(plugins);
		this.comparator = (Comparator<? super T>) (comparator == null ? DEFAULT_COMPARATOR : comparator);
	}

	/**
	 * Creates a new {@link OrderAwarePluginRegistry} using the {@code #DEFAULT_COMPARATOR}.
	 * 
	 * @param <T>
	 * @param <S>
	 * @return
	 */
	public static <S, T extends Plugin<S>> OrderAwarePluginRegistry<T, S> create() {
		return create(Collections.<T> emptyList());
	}

	/**
	 * Creates a new {@link OrderAwarePluginRegistry} using the given {@link Comparator} for ordering contained
	 * {@link Plugin}s.
	 * 
	 * @param <T>
	 * @param <S>
	 * @return
	 */
	public static <S, T extends Plugin<S>> OrderAwarePluginRegistry<T, S> create(Comparator<? super T> comparator) {
		return create(Collections.<T> emptyList(), comparator);
	}

	/**
	 * Creates a new {@link OrderAwarePluginRegistry} with the given plugins.
	 * 
	 * @param <S>
	 * @param <T>
	 * @param plugins
	 * @return
	 */
	public static <S, T extends Plugin<S>> OrderAwarePluginRegistry<T, S> create(List<? extends T> plugins) {
		return create(plugins, DEFAULT_COMPARATOR);
	}

	/**
	 * Creates a new {@link OrderAwarePluginRegistry} with the given plugins and the order of the plugins reverted.
	 * 
	 * @param <S>
	 * @param <T>
	 * @param plugins
	 * @return
	 */
	public static <S, T extends Plugin<S>> OrderAwarePluginRegistry<T, S> createReverse(List<? extends T> plugins) {
		return create(plugins, DEFAULT_REVERSE_COMPARATOR);
	}

	/**
	 * Creates a new {@link OrderAwarePluginRegistry} with the given plugins.
	 * 
	 * @param <S>
	 * @param <T>
	 * @param plugins
	 * @return
	 */
	public static <S, T extends Plugin<S>> OrderAwarePluginRegistry<T, S> create(List<? extends T> plugins,
			Comparator<? super T> comparator) {
		return new OrderAwarePluginRegistry<T, S>(plugins, comparator);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.PluginRegistrySupport#initialize(java.util.List)
	 */
	@Override
	protected List<T> initialize(List<T> plugins) {

		List<T> result = super.initialize(plugins);
		Collections.sort(result, comparator);
		return result;
	}

	/**
	 * Returns a new {@link OrderAwarePluginRegistry} with the order of the plugins reverted.
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public OrderAwarePluginRegistry<T, S> reverse() {

		ArrayList<T> copy = new ArrayList<T>(getPlugins());
		return create(copy, new InvertibleComparator(comparator, false));
	}
}
