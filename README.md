# kafka-metrics

This is a utility package for managing Kafka client metrics.

Kafka uses Yammer Metrics that it places in the default registry. The metrics are named in a standardized fashion. This utility provides a listener interface that allows the following:

 * notification of added/removed Kafka metrics
 * parsing Kafka's naming scheme to extract the metric name and tags
 * ease of exporting to another registry
 * ease of exporting to TSDB

# Usage

Implement `KafkaMetricsListener`, and pass an instance of that to `KafkaClientMetricSet`'s constructor:

    class MyKafkaListener implements KafkaMetricsListener {
        void onKafkaMetricAdded(MetricInfo metricInfoc) {}
        void onKafkaMetricRemoved(MetricInfo metricInfoc) {}
    }
    
    MyKafkaListener listener;
    KafkaClientMetricSet translator = new KafkaClientMetricSet(listener);

It makes sense to only have one instance of `KafkaClientMetricSet`.