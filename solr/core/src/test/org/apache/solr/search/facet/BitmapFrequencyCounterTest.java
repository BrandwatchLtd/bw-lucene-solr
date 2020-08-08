package org.apache.solr.search.facet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.lucene.util.LuceneTestCase;
import org.apache.solr.common.util.JavaBinCodec;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.junit.Test;

public class BitmapFrequencyCounterTest extends LuceneTestCase {
  private static final int TEST_ORDINAL = 5;

  @Test(expected = NegativeArraySizeException.class)
  public void givenNegativeSize_whenConstructingCounter() {
    new BitmapFrequencyCounter(-1);
  }

  @Test
  public void givenSize0_whenAddingValue_withFrequency1() {
    BitmapFrequencyCounter counter = new BitmapFrequencyCounter(0);

    counter.add(TEST_ORDINAL);

    counter.normalize();

    assertEquals(counter.getBitmaps().length, 0);
    assertEquals(counter.getOverflow().get(TEST_ORDINAL), Integer.valueOf(1));
  }

  @Test
  public void givenSize0_whenAddingValue_withFrequency2() {
    BitmapFrequencyCounter counter = new BitmapFrequencyCounter(0);

    counter.add(TEST_ORDINAL);
    counter.add(TEST_ORDINAL);

    counter.normalize();

    assertEquals(counter.getBitmaps().length, 0);
    assertEquals(counter.getOverflow().get(TEST_ORDINAL), Integer.valueOf(2));
  }

  @Test
  public void givenSize1_whenAddingValue_withFrequency1() {
    BitmapFrequencyCounter counter = new BitmapFrequencyCounter(1);

    counter.add(TEST_ORDINAL);

    counter.normalize();

    assertEquals(counter.getBitmaps().length, 1);
    assertTrue(counter.getBitmaps()[0].contains(TEST_ORDINAL));
    assertTrue(counter.getOverflow().isEmpty());

    int[] decoded = counter.decode();

    assertEquals(decoded.length, 2);
    assertEquals(decoded[0], 0);
    assertEquals(decoded[1], 1);
  }

  @Test
  public void givenSize1_whenAddingValue_withFrequency2() {
    BitmapFrequencyCounter counter = new BitmapFrequencyCounter(1);

    counter.add(TEST_ORDINAL);
    counter.add(TEST_ORDINAL);

    counter.normalize();

    assertEquals(counter.getBitmaps().length, 1);
    assertFalse(counter.getBitmaps()[0].contains(TEST_ORDINAL));
    assertEquals(counter.getOverflow().get(TEST_ORDINAL), Integer.valueOf(2));

    int[] decoded = counter.decode();

    assertEquals(decoded.length, 2);
    assertEquals(decoded[0], 0);
    assertEquals(decoded[1], 0);
  }

  @Test
  public void givenSize2_whenAddingValue_withFrequency1() {
    BitmapFrequencyCounter counter = new BitmapFrequencyCounter(2);

    counter.add(TEST_ORDINAL);

    counter.normalize();

    assertEquals(counter.getBitmaps().length, 2);
    assertTrue(counter.getBitmaps()[0].contains(TEST_ORDINAL));
    assertTrue(counter.getOverflow().isEmpty());

    int[] decoded = counter.decode();

    assertEquals(decoded.length, 2);
    assertEquals(decoded[0], 0);
    assertEquals(decoded[1], 1);
  }

  @Test
  public void givenSize2_whenAddingValue_withFrequency2() {
    BitmapFrequencyCounter counter = new BitmapFrequencyCounter(2);

    counter.add(TEST_ORDINAL);
    counter.add(TEST_ORDINAL);

    counter.normalize();

    assertEquals(counter.getBitmaps().length, 2);
    assertFalse(counter.getBitmaps()[0].contains(TEST_ORDINAL));
    assertTrue(counter.getBitmaps()[1].contains(TEST_ORDINAL));
    assertTrue(counter.getOverflow().isEmpty());

    int[] decoded = counter.decode();

    assertEquals(decoded.length, 4);
    assertEquals(decoded[0], 0);
    assertEquals(decoded[1], 0);
    assertEquals(decoded[2], 1);
    assertEquals(decoded[3], 0);
  }

  @Test
  public void givenSize2_whenAddingValue_withFrequency3() {
    BitmapFrequencyCounter counter = new BitmapFrequencyCounter(2);

    counter.add(TEST_ORDINAL);
    counter.add(TEST_ORDINAL);
    counter.add(TEST_ORDINAL);

    counter.normalize();

    assertEquals(counter.getBitmaps().length, 2);
    assertTrue(counter.getBitmaps()[0].contains(TEST_ORDINAL));
    assertTrue(counter.getBitmaps()[1].contains(TEST_ORDINAL));
    assertTrue(counter.getOverflow().isEmpty());

    int[] decoded = counter.decode();

    assertEquals(decoded.length, 4);
    assertEquals(decoded[0], 0);
    assertEquals(decoded[1], 0);
    assertEquals(decoded[2], 0);
    assertEquals(decoded[3], 1);
  }

  @Test
  public void givenSize2_whenAddingValue_withFrequency4() {
    BitmapFrequencyCounter counter = new BitmapFrequencyCounter(2);

    counter.add(TEST_ORDINAL);
    counter.add(TEST_ORDINAL);
    counter.add(TEST_ORDINAL);
    counter.add(TEST_ORDINAL);

    counter.normalize();

    assertEquals(counter.getBitmaps().length, 2);
    assertFalse(counter.getBitmaps()[0].contains(TEST_ORDINAL));
    assertFalse(counter.getBitmaps()[1].contains(TEST_ORDINAL));
    assertEquals(counter.getOverflow().get(TEST_ORDINAL), Integer.valueOf(4));

    int[] decoded = counter.decode();

    assertEquals(decoded.length, 4);
    assertEquals(decoded[0], 0);
    assertEquals(decoded[1], 0);
    assertEquals(decoded[2], 0);
    assertEquals(decoded[3], 0);
  }

  @Test
  public void givenSize2_whenAddingMultipleValues() {
    BitmapFrequencyCounter counter = new BitmapFrequencyCounter(2);

    counter.add(101);

    counter.add(102);
    counter.add(102);
    counter.add(202);
    counter.add(202);

    counter.add(103);
    counter.add(103);
    counter.add(103);
    counter.add(203);
    counter.add(203);
    counter.add(203);
    counter.add(303);
    counter.add(303);
    counter.add(303);

    counter.normalize();

    assertEquals(counter.getBitmaps().length, 2);

    assertTrue(counter.getBitmaps()[0].contains(101));
    assertFalse(counter.getBitmaps()[1].contains(101));

    assertFalse(counter.getBitmaps()[0].contains(102));
    assertTrue(counter.getBitmaps()[1].contains(102));
    assertFalse(counter.getBitmaps()[0].contains(202));
    assertTrue(counter.getBitmaps()[1].contains(202));

    assertTrue(counter.getBitmaps()[0].contains(103));
    assertTrue(counter.getBitmaps()[1].contains(103));
    assertTrue(counter.getBitmaps()[0].contains(203));
    assertTrue(counter.getBitmaps()[1].contains(203));
    assertTrue(counter.getBitmaps()[0].contains(303));
    assertTrue(counter.getBitmaps()[1].contains(303));

    assertTrue(counter.getOverflow().isEmpty());

    int[] decoded = counter.decode();

    assertEquals(decoded.length, 4);
    assertEquals(decoded[0], 0);
    assertEquals(decoded[1], 1);
    assertEquals(decoded[2], 2);
    assertEquals(decoded[3], 3);
  }

  @Test
  public void givenSize2_whenMergingValues() {
    BitmapFrequencyCounter x = new BitmapFrequencyCounter(2);
    BitmapFrequencyCounter y = new BitmapFrequencyCounter(2);

    x.add(TEST_ORDINAL);
    x.add(TEST_ORDINAL);

    x.normalize();

    assertEquals(x.getBitmaps().length, 2);
    assertFalse(x.getBitmaps()[0].contains(TEST_ORDINAL));
    assertTrue(x.getBitmaps()[1].contains(TEST_ORDINAL));
    assertTrue(x.getOverflow().isEmpty());

    y.add(TEST_ORDINAL);
    y.add(TEST_ORDINAL);

    y.normalize();

    assertEquals(y.getBitmaps().length, 2);
    assertFalse(y.getBitmaps()[0].contains(TEST_ORDINAL));
    assertTrue(y.getBitmaps()[1].contains(TEST_ORDINAL));
    assertTrue(y.getOverflow().isEmpty());

    x = x.merge(y);

    x.normalize();

    assertEquals(x.getBitmaps().length, 2);
    assertFalse(x.getBitmaps()[0].contains(TEST_ORDINAL));
    assertFalse(x.getBitmaps()[1].contains(TEST_ORDINAL));
    assertEquals(x.getOverflow().get(TEST_ORDINAL), Integer.valueOf(4));
  }

  @Test
  public void givenSize4_whenMergingValues() {
    BitmapFrequencyCounter x = new BitmapFrequencyCounter(4);
    BitmapFrequencyCounter y = new BitmapFrequencyCounter(4);

    x.add(TEST_ORDINAL);
    x.add(TEST_ORDINAL);
    x.add(TEST_ORDINAL);
    x.add(TEST_ORDINAL);
    x.add(TEST_ORDINAL);
    x.add(TEST_ORDINAL);
    x.add(TEST_ORDINAL);
    x.add(TEST_ORDINAL);
    x.add(TEST_ORDINAL);
    x.add(TEST_ORDINAL);

    x.normalize();

    assertEquals(x.getBitmaps().length, 4);
    assertFalse(x.getBitmaps()[0].contains(TEST_ORDINAL));
    assertTrue(x.getBitmaps()[1].contains(TEST_ORDINAL));
    assertFalse(x.getBitmaps()[2].contains(TEST_ORDINAL));
    assertTrue(x.getBitmaps()[3].contains(TEST_ORDINAL));
    assertTrue(x.getOverflow().isEmpty());

    y.add(TEST_ORDINAL);
    y.add(TEST_ORDINAL);
    y.add(TEST_ORDINAL);
    y.add(TEST_ORDINAL);
    y.add(TEST_ORDINAL);

    y.normalize();

    assertEquals(y.getBitmaps().length, 4);
    assertTrue(y.getBitmaps()[0].contains(TEST_ORDINAL));
    assertFalse(y.getBitmaps()[1].contains(TEST_ORDINAL));
    assertTrue(y.getBitmaps()[2].contains(TEST_ORDINAL));
    assertNull(y.getBitmaps()[3]);
    assertTrue(y.getOverflow().isEmpty());

    x = x.merge(y);

    x.normalize();

    assertEquals(x.getBitmaps().length, 4);
    assertTrue(x.getBitmaps()[0].contains(TEST_ORDINAL));
    assertTrue(x.getBitmaps()[1].contains(TEST_ORDINAL));
    assertTrue(x.getBitmaps()[2].contains(TEST_ORDINAL));
    assertTrue(x.getBitmaps()[3].contains(TEST_ORDINAL));
    assertTrue(x.getOverflow().isEmpty());

    int[] decoded = x.decode();

    assertEquals(decoded.length, 16);
    assertEquals(decoded[0], 0);
    assertEquals(decoded[1], 0);
    assertEquals(decoded[2], 0);
    assertEquals(decoded[3], 0);
    assertEquals(decoded[4], 0);
    assertEquals(decoded[5], 0);
    assertEquals(decoded[6], 0);
    assertEquals(decoded[7], 0);
    assertEquals(decoded[8], 0);
    assertEquals(decoded[9], 0);
    assertEquals(decoded[10], 0);
    assertEquals(decoded[11], 0);
    assertEquals(decoded[12], 0);
    assertEquals(decoded[13], 0);
    assertEquals(decoded[14], 0);
    assertEquals(decoded[15], 1);
  }

  @Test
  public void testSerialization() throws IOException {
    BitmapFrequencyCounter x = new BitmapFrequencyCounter(2);

    x.add(101);

    x.add(102);
    x.add(102);
    x.add(202);
    x.add(202);

    x.add(103);
    x.add(103);
    x.add(103);
    x.add(203);
    x.add(203);
    x.add(203);
    x.add(303);
    x.add(303);
    x.add(303);

    x.normalize();

    JavaBinCodec codec = new JavaBinCodec();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    codec.marshal(x.serialize(), out);

    InputStream in = new ByteArrayInputStream(out.toByteArray());
    BitmapFrequencyCounter y = new BitmapFrequencyCounter(2);
    y.deserialize((SimpleOrderedMap<Object>) codec.unmarshal(in));

    assertEquals(y.getBitmaps().length, 2);

    assertTrue(y.getBitmaps()[0].contains(101));
    assertFalse(y.getBitmaps()[1].contains(101));

    assertFalse(y.getBitmaps()[0].contains(102));
    assertTrue(y.getBitmaps()[1].contains(102));
    assertFalse(y.getBitmaps()[0].contains(202));
    assertTrue(y.getBitmaps()[1].contains(202));

    assertTrue(y.getBitmaps()[0].contains(103));
    assertTrue(y.getBitmaps()[1].contains(103));
    assertTrue(y.getBitmaps()[0].contains(203));
    assertTrue(y.getBitmaps()[1].contains(203));
    assertTrue(y.getBitmaps()[0].contains(303));
    assertTrue(y.getBitmaps()[1].contains(303));

    assertTrue(y.getOverflow().isEmpty());
  }
}
