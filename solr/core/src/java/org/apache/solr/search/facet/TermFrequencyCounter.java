package org.apache.solr.search.facet;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.common.util.SimpleOrderedMap;

public class TermFrequencyCounter {
  private final Map<String, Integer> counters;

  public TermFrequencyCounter() {
    this.counters = new HashMap<>();
  }

  public Map<String, Integer> getCounters() {
    return this.counters;
  }

  public void add(String value) {
    counters.merge(value, 1, Integer::sum);
  }

  public SimpleOrderedMap<Object> serialize() {
    SimpleOrderedMap<Object> serialized = new SimpleOrderedMap<>();

    if (!counters.isEmpty()) {
      serialized.add("counters", counters);
    }

    return serialized;
  }

  public void deserialize(SimpleOrderedMap<Object> serialized) {
    Map<String, Integer> overflow = (Map<String, Integer>) serialized.get("counters");
    if (overflow != null) {
      this.counters.putAll(overflow);
    }
  }

  public TermFrequencyCounter merge(TermFrequencyCounter other) {
    other.counters.forEach((value, freq) -> {
      counters.merge(value, freq, Integer::sum);
    });

    return this;
  }
}
