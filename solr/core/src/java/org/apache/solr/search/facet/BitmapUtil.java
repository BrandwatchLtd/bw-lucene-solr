package org.apache.solr.search.facet;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.roaringbitmap.ImmutableBitmapDataProvider;
import org.roaringbitmap.RoaringBitmap;

public class BitmapUtil {
  public static byte[] bitmapToBytes(ImmutableBitmapDataProvider bitmap) {
    ByteBuffer buffer = ByteBuffer.allocate(bitmap.serializedSizeInBytes());
    bitmap.serialize(buffer);
    return buffer.array();
  }

  public static RoaringBitmap bytesToBitmap(byte[] bytes) {
    try {
      RoaringBitmap bitmap = new RoaringBitmap();
      bitmap.deserialize(ByteBuffer.wrap(bytes));
      return bitmap;
    } catch (IOException ioe) {
      throw new RuntimeException("Failed to deserialise RoaringBitmap from bytes", ioe);
    }
  }
}
