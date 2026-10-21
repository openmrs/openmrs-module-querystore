/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.querystore.embedding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Pins that a querystore bean holding native resources releases them when the application context
 * shuts down. {@link OnnxEmbeddingProvider} is the case that leaked: with CoreML enabled each ONNX
 * session compiles its partitions into {@code onnxruntime-*.model.mlmodel(c)} entries under the
 * temp directory, and only {@link OnnxEmbeddingProvider#close()} removes them, so every JVM that
 * loaded the model and exited without it left them behind.
 *
 * <p>OpenMRS closes the application context when a module start or stop refreshes it, not on
 * server shutdown; the close that runs then is {@code QueryStoreActivator.stopped()}, pinned in
 * {@code QueryStoreActivatorTest}.
 */
public class EmbeddingProviderShutdownTest extends BaseModuleContextSensitiveTest {

	private static final String ONNX_BEAN = "querystore.embedding.onnx";

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	@Test
	public void contextShutdown_closesTheOnnxEmbeddingProvider() {
		ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
		RecordingOnnxEmbeddingProvider provider = new RecordingOnnxEmbeddingProvider();

		// The same destruction callback the context runs on shutdown, applied to a stand-in
		// instance so the shared singleton stays usable for the rest of the suite.
		beanFactory.destroyBean(ONNX_BEAN, provider);

		assertEquals("the context's shutdown must call close() on " + ONNX_BEAN + " exactly once", 1,
				provider.closeCalls);
	}

	@Test
	public void everyQuerystoreBeanWithACloseMethodIsClosedOnContextShutdown() throws Exception {
		ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
		List<String> examined = new ArrayList<>();
		List<String> unclosed = new ArrayList<>();
		for (String name : beanFactory.getBeanDefinitionNames()) {
			BeanDefinition definition = beanFactory.getMergedBeanDefinition(name);
			String className = definition.getBeanClassName();
			if (className == null || !className.startsWith("org.openmrs.module.querystore.")) {
				continue;
			}
			Class<?> beanClass = Class.forName(className);
			if (!hasPublicNoArgClose(beanClass)) {
				continue;
			}
			examined.add(name);
			boolean declared = definition instanceof AbstractBeanDefinition
					&& "close".equals(((AbstractBeanDefinition) definition).getDestroyMethodName());
			boolean inferred = AutoCloseable.class.isAssignableFrom(beanClass)
					|| DisposableBean.class.isAssignableFrom(beanClass);
			if (!declared && !inferred) {
				unclosed.add(name + " (" + className + ")");
			}
		}
		assertTrue("expected the ONNX provider among the beans examined, examined " + examined,
				examined.contains(ONNX_BEAN));
		assertTrue("beans whose close() the context never calls on shutdown: " + unclosed, unclosed.isEmpty());
	}

	private static boolean hasPublicNoArgClose(Class<?> type) {
		try {
			Method close = type.getMethod("close");
			return !Modifier.isStatic(close.getModifiers());
		}
		catch (NoSuchMethodException e) {
			return false;
		}
	}

	private static final class RecordingOnnxEmbeddingProvider extends OnnxEmbeddingProvider {

		private int closeCalls;

		@Override
		public synchronized void close() {
			closeCalls++;
			super.close();
		}
	}
}
