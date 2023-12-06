/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.codecs.zstd;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.lucene.codecs.compressing.CompressionMode;
import org.apache.lucene.codecs.compressing.Compressor;
import org.apache.lucene.codecs.compressing.Decompressor;
import org.apache.lucene.codecs.lucene90.compressing.Lucene90CompressingStoredFieldsFormat;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.ByteBuffersDataInput;
import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.BytesRef;

/** Stored fields format that uses ZSTD for compression. */
public final class ZstdStoredFieldsFormat extends Lucene90CompressingStoredFieldsFormat {

  // Size of top-level blocks, which themselves contain a dictionary and 10 sub blocks
  private static final int BLOCK_SIZE = 128 * 1024;

  /** Stored fields format with specified compression level. */
  public ZstdStoredFieldsFormat(int level) {
    // same block size, max number of docs per block and block shift as the default codec with
    // DEFLATE
    super("ZstdStoredfields", new ZstdCompressionMode(level), BLOCK_SIZE, 1024, 10);
  }

  private static class ZstdCompressionMode extends CompressionMode {
    private final int level;

    ZstdCompressionMode(int level) {
      this.level = level;
    }

    @Override
    public Compressor newCompressor() {
      return new ZstdCompressor(level);
    }

    @Override
    public Decompressor newDecompressor() {
      return new ZstdDecompressor();
    }
  }

  private static final class ZstdDecompressor extends Decompressor {

    byte[] compressed;

    ZstdDecompressor() {
      compressed = BytesRef.EMPTY_BYTES;
    }

    @Override
    public void decompress(DataInput in, int originalLength, int offset, int length, BytesRef bytes)
        throws IOException {
      final int compressedLength = in.readVInt();
      compressed = ArrayUtil.growNoCopy(compressed, compressedLength);
      in.readBytes(compressed, 0, compressedLength);
      bytes.bytes = ArrayUtil.growNoCopy(bytes.bytes, originalLength);

      try (Zstd.Decompressor dctx = new Zstd.Decompressor(); Zstd.DecompressionDictionary ddict =
          Zstd.createDecompressionDictionary(ByteBuffer.allocate(0))) {
        final int l = dctx.decompress(ByteBuffer.wrap(bytes.bytes), ByteBuffer.wrap(compressed, 0, compressedLength), ddict);
        if (l != originalLength) {
          throw new CorruptIndexException("Corrupt", in);
        }
        bytes.offset = offset;
        bytes.length = length;
      }
    }

    @Override
    public Decompressor clone() {
      return new ZstdDecompressor();
    }
  }

  private static class ZstdCompressor extends Compressor {

    final int level;
    byte[] buffer;
    byte[] compressed;

    ZstdCompressor(int level) {
      this.level = level;
      compressed = BytesRef.EMPTY_BYTES;
      buffer = BytesRef.EMPTY_BYTES;
    }

    @Override
    public void compress(ByteBuffersDataInput buffersInput, DataOutput out) throws IOException {
      final int len = (int) buffersInput.size();

      buffer = ArrayUtil.growNoCopy(buffer, len);
      buffersInput.readBytes(buffer, 0, len);

      final int maxCompressedLength = Zstd.getMaxCompressedLen(len);
      compressed = ArrayUtil.growNoCopy(compressed, maxCompressedLength);

      try (Zstd.Compressor cctx = new Zstd.Compressor()) {
        int compressedLen =
            cctx.compress(
                ByteBuffer.wrap(compressed, 0, compressed.length),
                ByteBuffer.wrap(buffer, 0, len),
                level);

        out.writeVInt(compressedLen);
        out.writeBytes(compressed, compressedLen);
      }
    }

    @Override
    public void close() throws IOException {}
  }
}
