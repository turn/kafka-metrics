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

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.MetricsRegistryListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A class that extracts Kafka metrics from the Yammer Metrics default registry.
 *
 * The Kafka metrics can either be returned, or individually processed through a listener. This
 * latter approach is useful for an online system as Kafka might add new metrics at any time.
 *
 * @author Adam Lugowski <adam.lugowski@turn.com>
 */
public class KafkaClientMetricSet extends MetricSubset {

	List<KafkaMetricsListener> listeners = Collections.synchronizedList(new ArrayList<KafkaMetricsListener>());

	private MetricsRegistryListener metricsRegistryListener = new MetricsRegistryListener() {
		/**
		 * New metric added to the Yammer Metrics registry.
		 *
		 * Note that Kafka uses the default registry, so there can be non-Kafka metrics there
		 * as well. This function ignores any metric whose group does not start with {@code 'kafka'}.
		 *
		 * @param metricName
		 * @param metric
		 */
		public void onMetricAdded(MetricName metricName, Metric metric) {
			// The default registry can have non-Kafka metrics. Filter those out.
			if (!metricName.getGroup().startsWith("kafka")) {
				return;
			}

			MetricInfo mi = process(metric, metricName);
			if (mi != null) {
				synchronized (listeners) {
					for (KafkaMetricsListener listener : listeners) {
						listener.onKafkaMetricAdded(mi);
					}
				}
			} else {
				// metric not recognized
				System.err.println("Unrecognized Kafka metric: " + metricName.toString()
								+ "\n group: " + metricName.getGroup()
								+ "\n type: " + metricName.getType()
								+ "\n name: " + metricName.getName()
								+ "\n scope: " + metricName.getScope()
				);
			}
		}

		/**
		 * Metric removed from the Yammer Metrics registry.
		 *
		 * Note that Kafka uses the default registry, so there can be non-Kafka metrics there
		 * as well. This function ignores any metric whose group does not start with {@code 'kafka'}.
		 *
		 * @param metricName
		 */
		public void onMetricRemoved(MetricName metricName) {
			// The default registry can have non-Kafka metrics. Filter those out.
			if (!metricName.getGroup().startsWith("kafka")) {
				return;
			}

			MetricInfo mi = removeMetric(metricName);
			if (mi != null) {
				synchronized (listeners) {
					for (KafkaMetricsListener listener : listeners) {
						listener.onKafkaMetricRemoved(mi);
					}
				}
			}
		}
	};

	/**
	 * Create new {@link KafkaClientMetricSet} that listens for Kafka metrics in the Yammer Metrics
	 * default registry.
	 *
	 * The listener's {@link KafkaMetricsListener#onKafkaMetricAdded(MetricInfo)} method will be
	 * called once per every Kafka metric currently in the registry.
	 *
	 * @param listener
	 */
	public KafkaClientMetricSet(KafkaMetricsListener listener) {
		this(Metrics.defaultRegistry(), listener);
	}

	/**
	 * Create new {@link KafkaClientMetricSet} that listens for Kafka metrics in the specified
	 * {@link MetricsRegistry}.
	 *
	 * By default, Kafka uses {@link Metrics#defaultRegistry()}, so use
	 * {@link KafkaClientMetricSet#filterExistingMetrics(MetricsRegistry)}.
	 *
	 * The listener's {@link KafkaMetricsListener#onKafkaMetricAdded(MetricInfo)} method will be
	 * called once per every Kafka metric currently in the registry.
	 *
	 * @param registry
	 * @param listener
	 */
	public KafkaClientMetricSet(MetricsRegistry registry, KafkaMetricsListener listener) {
		addKafkaMetricsListener(listener);
		registry.addListener(metricsRegistryListener);
		filterExistingMetrics(registry);
	}

	/**
	 * Add a listener that is notified whenever any new Kafka metric is added.
	 *
	 * @param listener
	 */
	public void addKafkaMetricsListener(KafkaMetricsListener listener) {
		if (listener != null)
			this.listeners.add(listener);
	}

	/**
	 * Remove listener previously added with {@link #addKafkaMetricsListener(KafkaMetricsListener)}.
	 *
	 * @param listener
	 */
	public void removeKafkaMetricsListener(KafkaMetricsListener listener) {
		this.listeners.remove(listener);
	}


	/**
	 * Scans a Yammer {@link MetricsRegistry} for Kafka metrics and notifies the registered
	 * {@link KafkaMetricsListener}s as if the metrics had just been added.
	 *
	 * Useful during initialization to catch any metrics that have been added before
	 * metricsRegistryListener was enabled.
	 *
	 * @param registry registry to scan
	 */
	private void filterExistingMetrics(MetricsRegistry registry) {
		// find the Kafka metrics currently in the registry
		for (Map.Entry<MetricName, Metric> metricEntry : registry.allMetrics().entrySet()) {
			this.metricsRegistryListener.onMetricAdded(metricEntry.getKey(), metricEntry.getValue());
		}
	}

	@Override
	protected MetricInfo process(Metric originalMetric, MetricName originalName) {
		return addMetric(originalMetric, originalName);
	}
}
