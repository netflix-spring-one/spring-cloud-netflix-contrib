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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.metrics.export.Exporter;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.Ordered;

import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.MetricPoller;
import com.netflix.servo.publish.atlas.AtlasMetricObserver;

public class AtlasExporter implements Exporter, SmartLifecycle {
	static final Logger logger = LoggerFactory.getLogger(AtlasExporter.class);
	private AtlasMetricObserver observer;
	private MetricPoller poller;
	
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private volatile ScheduledFuture<?> scheduledTask = null;
	private Long delay;
	

	public AtlasExporter(AtlasMetricObserver observer, MetricPoller poller, Long delay) {
		this.observer = observer;
		this.poller = poller;
		this.delay = delay;
	}

	@Override
	public void export() {
		observer.update(poller.poll(BasicMetricFilter.MATCH_ALL));
	}

	@Override
	public void start() {
        // use compareAndSet to make sure it starts only once and when not running
        if (running.compareAndSet(false, true)) {
            logger.info("Starting AtlasExporter");
            try {
                scheduledTask = executor.scheduleWithFixedDelay(new Runnable(){

					@Override
					public void run() {
						export();
					}
                	
                }, 0, delay, TimeUnit.MILLISECONDS);
            } catch (Throwable ex) {
                logger.error("Exception while creating the AtlasExporter task");
                ex.printStackTrace();
                running.set(false);
            }
        }
	}

	@Override
	public void stop() {
		if(this.scheduledTask != null)
		{
			scheduledTask.cancel(true);
			running.compareAndSet(true, false);
			logger.info("Stopping the AtlasExporter");
			executor.shutdown();
		}
		
	}

	@Override
	public boolean isRunning() {
		return running.get();
	}

	@Override
	public int getPhase() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		stop();
		callback.run();
	}
}
