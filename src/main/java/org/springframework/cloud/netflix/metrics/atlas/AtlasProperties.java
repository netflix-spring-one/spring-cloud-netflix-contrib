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
