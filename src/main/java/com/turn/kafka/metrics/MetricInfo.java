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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that encapsulates a single Kafka metric.
 *
 * This is roughly equivalent to {@code Map.Entry<MetricName, Metric>}, but includes additional functions
 * useful for TSDB integration, such as extracting TSDB-compatible names and tags.
 *
 * @author Adam Lugowski <adam.lugowski@turn.com>
 */
public class MetricInfo {
	private Metric originalMetric;

	private MetricName originalName;

	private String tsdbName;

	private Map<String, String> tsdbTags;

	/**
	 * Create a TSDB-compatible name from the Kafka Yammer name.
	 *
	 * Basically, join {@code group}, {@code type}, and {@code name} with {@code '.'}.
	 *
	 * @param originalName
	 * @return a string representing the TSDB metric name for this Yammer metric name.
	 */
	private static String extractKafkaTsdbName(MetricName originalName) {
		String ret = "";

		String[] components = new String[] {originalName.getGroup(), originalName.getType(), originalName.getName()};

		String delimiter = "";
		for (String s : components) {
			if (s != null && s.length() > 0) {
				ret += delimiter + s;
				delimiter = ".";
			}
		}

		return ret;
	}

	/**
	 * Convert an array of key-value string pairs into a map.
	 *
	 * @param keyValue an array of strings where the even-indexed elements are keys and odd-indexed elements are values.
	 * @return a {@link Map} representation of keyValue
	 * @throws IllegalArgumentException if keyValue contains an odd number of elements.
	 */
	public static Map<String, String> stringArrToMap(String[] keyValue) throws IllegalArgumentException {
		Map<String, String> ret = new HashMap<String, String>();

		if(keyValue.length % 2 != 0) {
			throw new IllegalArgumentException("keyValue needs to be specified in pairs");
		} else {
			for(int i = 0; i < keyValue.length; i += 2) {
				ret.put(keyValue[i], keyValue[i + 1]);
			}
		}
		return ret;
	}

	/**
	 * Many, if not all, Kafka metrics store their tags in the scope field. This function parses
	 * the field back into a map.
	 *
	 * As per {@code toScope()} in {@code KafkaMetricsGroup.scala}, the scope is a {@code '.'}-delimited list of key-value
	 * pairs, where the key and value are themselves separated by a {@code '.'}.
	 *
	 * @param scope from {@link MetricName#getScope()}
	 * @return a {@code Map<String, String>} of the tags found in the scope.
	 * @throws IllegalArgumentException if the scope cannot be parsed.
	 */
	public static Map<String, String> parseScopeTags(String scope) throws IllegalArgumentException {
		if (scope == null || scope.length() == 0)
			return new HashMap<String, String>();

		String[] keyValue = scope.split("\\.");
		return stringArrToMap(keyValue);
	}

	/**
	 * Extract the TSDB-compatible tags from the Kafka Yammer name.
	 *
	 * As per {@code KafkaMetricsGroup.scala}, Kafka metrics include a set of tags. Kafka encodes those
	 * tags into the scope field of the Yammer MetricName. This function parses the scope field
	 * to convert the tags back into a map.
	 *
	 * @param originalName
	 * @return
	 */
	private static Map<String, String> extractKafkaTsdbTags(MetricName originalName) {
		return Collections.unmodifiableMap(parseScopeTags(originalName.getScope()));
	}


	public MetricInfo(Metric originalMetric, MetricName originalName) {
		this.originalMetric = originalMetric;
		this.originalName = originalName;
		this.tsdbName = extractKafkaTsdbName(originalName);
		this.tsdbTags = extractKafkaTsdbTags(originalName);
	}

	public MetricInfo(Metric originalMetric, MetricName originalName,
					  String tsdbName, Map<String, String> tsdbTags) {
		this.originalMetric = originalMetric;
		this.originalName = originalName;
		this.tsdbName = tsdbName;
		this.tsdbTags = tsdbTags;
	}

	public Metric getOriginalMetric() {
		return originalMetric;
	}

	public MetricName getOriginalName() {
		return originalName;
	}

	public String getTsdbName() {
		return tsdbName;
	}

	public Map<String, String> getTsdbTags() {
		return tsdbTags;
	}

	public String getTsdbTagsAsString() {
		StringBuilder stringBuilder = new StringBuilder();
		String delimeter = "";

		for (Map.Entry<String, String> tag : tsdbTags.entrySet()) {
			stringBuilder.append(delimeter)
					.append(tag.getKey())
					.append("=")
					.append(tag.getValue());
			delimeter = " ";
		}

		return stringBuilder.toString();
	}
}
