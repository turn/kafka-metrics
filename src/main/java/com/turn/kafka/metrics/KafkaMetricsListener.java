/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.turn.kafka.metrics;

/**
 * An interface for a listener for Kafka metric changes.
 *
 * An implementation of this interface is sufficient to be able to process all Kafka metrics.
 *
 * New metrics can be created by Kafka at any time; for example a new topic would spawn
 * its corresponding producer and/or consumer metrics.
 *
 * @author Adam Lugowski <adam.lugowski@turn.com>
 */
public interface KafkaMetricsListener {
	/**
	 * Called when a new Kafka metric is added.
	 *
	 * @param metricInfoc the newly-added metric.
	 */
	void onKafkaMetricAdded(MetricInfo metricInfoc);

	/**
	 * Called when a Kafka metric is removed.
	 *
	 * @param metricInfoc a reference to the metric that has been removed.
	 */
	void onKafkaMetricRemoved(MetricInfo metricInfoc);
}
