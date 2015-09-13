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

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.netflix.servo.publish.atlas.ServoAtlasConfig;

@ConfigurationProperties(prefix = "netflix.atlas")
public class AtlasProperties implements ServoAtlasConfig {

	@NotNull
	private String uri;
	private int pushQueueSize = 1000;
	private boolean enabled = true;
	private int batchSize = 10000;
	private Long publishDelay = 5000L;
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public int getPushQueueSize() {
		return pushQueueSize;
	}
	public void setPushQueueSize(int pushQueueSize) {
		this.pushQueueSize = pushQueueSize;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public Integer getBatchSize() {
		return batchSize;
	}
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	public Long getPublishDelay() {
		return publishDelay;
	}
	public void setPublishDelay(Long publishDelay) {
		this.publishDelay = publishDelay;
	}
	
	public String getAtlasUri() {
		return getUri();
	}

	public boolean shouldSendMetrics() {
		return isEnabled();
	}
	
	public int batchSize() {
		return getBatchSize();
	}

}
