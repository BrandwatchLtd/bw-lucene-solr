package org.apache.solr.search.facet;

import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;

/**
 * Calculates the frequency-of-frequencies (number of values occurring x times) of ordinal values.
 *
 * The response is a map with the following fields:
 * - frequencies: an array where {@code frequencies[i]} is the number of values with {@code frequency = i} (omitted
 *   if empty)
 * - overflow: the number of values with {@code frequency > frequencies.length}
 *
 * Lacking a coherent definition of magnitude other than the raw count, this aggregate cannot be used for sorting.
 */
public class FrequencyOfFrequenciesAgg extends SimpleAggValueSource {
  private final int size;

  public FrequencyOfFrequenciesAgg(ValueSource vs, Integer size) {
    super("bitmapfreqfreq", vs);

    this.size = size;
  }

  @Override
  public SlotAcc createSlotAcc(FacetContext fcontext, int numDocs, int numSlots) {
    return new BitmapFrequencySlotAcc(getArg(), fcontext, numSlots, size);
  }

  @Override
  public FacetMerger createFacetMerger(Object prototype) {
    return new Merger(size);
  }

  public static class Parser extends ValueSourceParser {
    @Override
    public ValueSource parse(FunctionQParser fp) throws SyntaxError {
      ValueSource valueSource = fp.parseValueSource();

      int size = 8;
      if (fp.hasMoreArguments()) {
        size = fp.parseInt();
      }

      return new FrequencyOfFrequenciesAgg(valueSource, size);
    }
  }

  private static class Merger extends FacetMerger {
    private final int size;
    private BitmapFrequencyCounter result;

    public Merger(int size) {
      this.size = size;
      this.result = new BitmapFrequencyCounter(size);
    }

    @Override
    public void merge(Object facetResult, Context mcontext) {
      if (facetResult instanceof SimpleOrderedMap) {
        BitmapFrequencyCounter deserialized = new BitmapFrequencyCounter(size);
        deserialized.deserialize((SimpleOrderedMap<Object>) facetResult);

        result = result.merge(deserialized);
      }
    }

    @Override
    public void finish(Context mcontext) {
      // never called
    }

    @Override
    public Object getMergedResult() {
      SimpleOrderedMap<Object> map = new SimpleOrderedMap<>();

      map.add("frequencies", result.decode());
      map.add("overflow", result.getOverflow().getCardinality());

      return map;
    }
  }
}
