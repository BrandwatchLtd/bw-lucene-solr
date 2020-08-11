package org.apache.solr.search.facet;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;

public class TermFrequencyOfFrequenciesAgg extends SimpleAggValueSource {
  public TermFrequencyOfFrequenciesAgg(ValueSource vs) {
    super("termfreqfreq", vs);
  }

  @Override
  public SlotAcc createSlotAcc(FacetContext fcontext, int numDocs, int numSlots) {
    return new TermFrequencySlotAcc(getArg(), fcontext, numSlots);
  }

  @Override
  public FacetMerger createFacetMerger(Object prototype) {
    return new Merger();
  }

  public static class Parser extends ValueSourceParser {
    @Override
    public ValueSource parse(FunctionQParser fp) throws SyntaxError {
      return new TermFrequencyOfFrequenciesAgg(fp.parseValueSource());
    }
  }

  private static class Merger extends FacetMerger {
    private TermFrequencyCounter result;

    public Merger() {
      this.result = new TermFrequencyCounter();
    }

    @Override
    public void merge(Object facetResult, Context mcontext) {
      if (facetResult instanceof SimpleOrderedMap) {
        TermFrequencyCounter deserialized = new TermFrequencyCounter();
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
      Map<Integer, Integer> map = new LinkedHashMap<>();

      result.getCounters()
        .forEach((value, freq) -> map.merge(freq, 1, Integer::sum));

      return map;
    }
  }
}
