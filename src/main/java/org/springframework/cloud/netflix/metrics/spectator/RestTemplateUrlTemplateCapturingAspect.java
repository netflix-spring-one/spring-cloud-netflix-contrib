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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.web.client.RestOperations;

/**
 * Captures the still-templated URI because currently the ClientHttpRequestInterceptor only gives us the means to
 * retrieve the substituted URI.
 * 
 * Requires @EnableAspectJAutoProxy(proxyTargetClass = true) or spring.aop.proxy-target-class=true to wrap
 * RestTemplate. Otherwise it will only take effect when injecting {@link RestOperations}
 *
 * @author Taylor Wicksell
 * @author Jon Schneider
 */
@Aspect
public class RestTemplateUrlTemplateCapturingAspect {
	@Around("execution(* org.springframework.web.client.RestTemplate.*(String, ..))")
	void captureUrlTemplate(ProceedingJoinPoint joinPoint) throws Throwable {
		try {
			String urlTemplate = (String) joinPoint.getArgs()[0];
			RestTemplateUrlTemplateHolder.setRestTemplateUrlTemplate(urlTemplate);
			joinPoint.proceed();
		} finally {
			RestTemplateUrlTemplateHolder.clear();
		}
	}
}
