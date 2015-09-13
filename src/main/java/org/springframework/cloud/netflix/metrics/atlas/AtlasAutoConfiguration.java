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

package org.springframework.cloud.netflix.metrics.atlas;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.export.Exporter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.metrics.spectator.SpectatorAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.frigga.Names;
import com.netflix.servo.publish.MetricPoller;
import com.netflix.servo.publish.atlas.AtlasMetricObserver;
import com.netflix.servo.publish.atlas.ServoAtlasConfig;
import com.netflix.servo.tag.BasicTagList;

@Configuration
@ConditionalOnClass(AtlasMetricObserver.class)
@EnableConfigurationProperties(AtlasProperties.class)
@AutoConfigureAfter(SpectatorAutoConfiguration.class)
public class AtlasAutoConfiguration {
  
    @Bean
    @ConditionalOnMissingBean(AtlasMetricObserver.class)
    public AtlasMetricObserver atlasObserver(Optional<Collection<AtlasTagProvider>> tagProviders, ServoAtlasConfig servoAtlasConfig) {
        BasicTagList tags = (BasicTagList) BasicTagList.EMPTY;
        if(tagProviders != null && tagProviders.isPresent()) {
            for (AtlasTagProvider tagProvider : tagProviders.get()) {
                for (Map.Entry<String, String> tag : tagProvider.defaultTags().entrySet()) {
                    if(tag.getValue() != null)
                        tags = tags.copy(tag.getKey(), tag.getValue());
                }
            }
        }
        return new AtlasMetricObserver(servoAtlasConfig, tags);
    }

    @Bean
    @ConditionalOnMissingBean(Exporter.class)
    public AtlasExporter exporter(AtlasMetricObserver observer, MetricPoller poller, AtlasProperties properties) {
        return new AtlasExporter(observer, poller, properties.getPublishDelay());
    }

    @Configuration
    @ConditionalOnBean(InstanceInfo.class)
    static class InstanceInfoTagProviderConfiguration {
        @Autowired
        InstanceInfo instanceInfo;

        @Bean
        public AtlasTagProvider instanceInfoTags() {
            return () -> {
                Map<String, String> tags = new HashMap<>();
                tags.put("appName", instanceInfo.getAppName());
                tags.put("cluster", Names.parseName(instanceInfo.getASGName()).getCluster());
                return tags;
            };
        }
    }
}
