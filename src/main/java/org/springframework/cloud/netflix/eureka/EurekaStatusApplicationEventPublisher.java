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

package org.springframework.cloud.netflix.eureka;

import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.discovery.DiscoveryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.scheduling.annotation.Scheduled;

public class EurekaStatusApplicationEventPublisher implements ApplicationEventPublisherAware {
    private InstanceStatus currentStatus = InstanceStatus.UNKNOWN;
    private ApplicationEventPublisher publisher;

    @Autowired
    DiscoveryClient discoveryClient;

    @Scheduled(fixedDelay = 3000L)
    public void checkDiscovery() {
        InstanceStatus latestStatus = discoveryClient.getInstanceRemoteStatus();
        if(!latestStatus.equals(currentStatus)) {
            this.currentStatus = latestStatus;
            publisher.publishEvent(new EurekaStatusChangedEvent(latestStatus));
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}