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

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;

import java.util.*;

/**
 * A base class that performs the dirty work of filtering and extracting Kafka metrics.
 *
 * @author Adam Lugowski <adam.lugowski@turn.com>
 */
public abstract class MetricSubset {
	private List<MetricInfo> metrics = Collections.synchronizedList(new ArrayList<MetricInfo>());

	/**
	 * Membership test for a Kafka metric.
	 *
	 * @param originalName
	 * @return {@code true} if a Kafka metric with originalName is in this {@link MetricSubset}.
	 * {@code false} otherwise.
	 */
	public boolean containsMetric(MetricName originalName) {
		return metrics.contains(originalName);
	}

	protected MetricInfo addMetric(Metric originalMetric, MetricName originalName) {
		if (containsMetric(originalName))
			return null;

		MetricInfo metricInfo = new MetricInfo(originalMetric, originalName);
		metrics.add(metricInfo);
		return metricInfo;
	}

	protected MetricInfo addMetric(Metric originalMetric, MetricName originalName,
							 String name, String... keyValue) {

		// set up the tags
		Map<String, String> tags = null;

		if (keyValue != null && keyValue.length > 0) {
			tags = MetricInfo.stringArrToMap(keyValue);
		}

		return addMetric(originalMetric, originalName, name, tags);
	}

	protected MetricInfo addMetric(Metric originalMetric, MetricName originalName,
							 String name, Map<String, String> tags) {

		if (containsMetric(originalName))
			return null;

		if (tags == null)
			tags = new HashMap<String, String>();

		MetricInfo metricInfo = new MetricInfo(originalMetric, originalName, name, tags);
		metrics.add(metricInfo);
		return metricInfo;
	}

	protected MetricInfo removeMetric(MetricName originalName) {
		synchronized (metrics) {
			Iterator<MetricInfo> iter = metrics.iterator();
			while (iter.hasNext()) {
				MetricInfo mi = iter.next();
				if (mi.getOriginalName().equals(originalName)) {
					iter.remove();
					return mi;
				}
			}
		}
		return null;
	}

	/**
	 * Determines whether or not this subset should contain the given Kafka metric, and, if so,
	 * adds it to the subset.
	 *
	 * @param originalMetric the Kafka metric.
	 * @param originalName the Yammer metric name.
	 * @return the processed MetricInfo if the metric is accepted, null otherwise.
	 */
	protected abstract MetricInfo process(Metric originalMetric, MetricName originalName);

	/**
	 * A list of metrics contained in this subset.
	 *
	 * @return the metrics
	 */
	public List<MetricInfo> getMetrics() {
		return Collections.unmodifiableList(metrics);
	}
}
