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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.test.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.*;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.EnableIntegrationManagement;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.support.management.DefaultMetricsFactory;
import org.springframework.integration.support.management.MessageChannelMetrics;
import org.springframework.integration.support.management.MessageHandlerMetrics;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.test.context.ContextConfiguration;

import com.netflix.spectator.api.Registry;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Taylor Wicksell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpectatorIntegrationMetricsTestConfig.class)
public class SpectatorIntegrationMetricsTests {
    @Autowired
    TestGateway gateway;

    @Autowired
    Registry registry;

    @Autowired
    List<MessageChannelMetrics> channelMetrics;

    @Autowired
    List<MessageHandlerMetrics> handlerMetrics;

    @Test
    public void metricsAndRegistryAreCreated() {
        assertTrue(channelMetrics != null);
        assertTrue(handlerMetrics != null);
        assertTrue(registry != null);
    }

    @Test
    public void metricsAreRecordedForIntegrationMessages() throws InterruptedException {
        gateway.sendMessage("test");

        try {
            gateway.sendMessage("error");
        } catch(RuntimeException e) {
        }

        Thread.sleep(500);

        assertEquals(2, (int) registry.get(registry.createId("springintegration", "metric", "HandleCount", "name", "spectatorIntegrationMetricsTestConfig.testActivator.serviceActivator.handler", "type", "service-activator"))
                .measure().iterator().next().value());
        assertEquals(1, (int) registry.get(registry.createId("springintegration", "metric", "ErrorCount", "name", "spectatorIntegrationMetricsTestConfig.testActivator.serviceActivator.handler", "type", "service-activator"))
                .measure().iterator().next().value());
        assertEquals(1, (int) registry.get(registry.createId("springintegration", "metric", "SendCount", "name", "output", "type", "channel")).measure().iterator().next().value());
        assertEquals(0, (int) registry.get(registry.createId("springintegration", "metric", "SendErrorCount", "name", "output", "type", "channel")).measure().iterator().next().value());
    }
}

@Configuration
@EnableIntegration
@EnableIntegrationManagement(countsEnabled = "*", statsEnabled = "*", metricsFactory = "metricsFactory")
@IntegrationComponentScan(basePackageClasses = SpectatorIntegrationMetricsTestConfig.class)
@MessageEndpoint
@ImportAutoConfiguration({PropertyPlaceholderAutoConfiguration.class, SpectatorAutoConfiguration.class, SpectatorIntegrationMetricsAutoConfiguration.class})
class SpectatorIntegrationMetricsTestConfig {
    @Bean
    public DefaultMetricsFactory metricsFactory() {
        return new DefaultMetricsFactory();
    }

    @Bean
    public MessageChannel input() {
        return MessageChannels.direct().get();
    }

    @Bean
    public MessageChannel output() {
        return MessageChannels.queue(10).get();
    }

    @ServiceActivator(inputChannel = "input", outputChannel = "output")
    public String testActivator(@Payload String payload) {
        if ("error".equals(payload)) {
            throw new RuntimeException("Boom");
        } else {
            return payload.toUpperCase();
        }
    }
}

@MessagingGateway
interface TestGateway {
    @Gateway(requestChannel = "input")
    void sendMessage(@Payload String payload);
}