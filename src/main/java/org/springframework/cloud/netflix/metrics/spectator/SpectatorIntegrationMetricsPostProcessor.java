/*
 * Copyright 2015 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.springframework.cloud.netflix.metrics.spectator;

import java.util.function.ToDoubleFunction;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.integration.support.context.NamedComponent;
import org.springframework.integration.support.management.MessageChannelMetrics;
import org.springframework.integration.support.management.MessageHandlerMetrics;
import org.springframework.integration.support.management.QueueChannelManagement;
import org.springframework.util.ReflectionUtils;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Registry;

/**
 * @author Taylor Wicksell
 */
public class SpectatorIntegrationMetricsPostProcessor implements BeanPostProcessor {

	@Autowired
	Registry registry;

	@Value("${netflix.spectator.integration.metricName:springintegration}")
	String metricName;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof MessageHandlerMetrics && bean instanceof NamedComponent) {
			gatherMetrics(bean, MessageHandlerMetrics.class);
		}
		if (bean instanceof QueueChannelManagement && bean instanceof NamedComponent) {
			gatherMetrics(bean, QueueChannelManagement.class);
			return bean;
		}
		if (bean instanceof MessageChannelMetrics && bean instanceof NamedComponent) {
			gatherMetrics(bean, MessageChannelMetrics.class);
			return bean;
		}
		return bean;
	}

	private void gatherMetrics(final Object bean, Class clazz) {
		ReflectionUtils
				.doWithMethods(
						clazz,
						m -> {
							if (m.getName().startsWith("get") && m.getParameterCount() == 0
									&& m.getReturnType().isPrimitive()) {
								Id metricId = registry.createId(metricName, "metric", m.getName().substring(3), "name",
										((NamedComponent) bean).getComponentName(), "type",
										((NamedComponent) bean).getComponentType());
								registerGauge(metricId, bean, s -> {
									try {
										double val = Double.valueOf(m.invoke(s).toString());
										return val;
									} catch (Exception e) {
										throw new RuntimeException("Problem exporting Spring Integration metrics", e);
									}
								});
							}
						});
	}

	private <T> void registerGauge(Id id, T bean, ToDoubleFunction<T> value) {
		registry.gauge(id, bean, value);
	}
}
