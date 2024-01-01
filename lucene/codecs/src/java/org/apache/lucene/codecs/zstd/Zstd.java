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

import java.io.Closeable;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;

import org.apache.lucene.panama.zstd.Libzstd;


final class Zstd {

  public static class Compressor implements Closeable {

    private MemorySegment cctx;
    private boolean closed;

    Compressor() {
      cctx = Libzstd.ZSTD_createCCtx();
      if (cctx == null) {
        throw new IllegalStateException("Can't allocate compression context");
      }
    }

    public synchronized int compress(ByteBuffer dst, ByteBuffer src, int level) {
      MemorySegment dstSegment = Arena.ofAuto().allocate(dst.remaining());
      MemorySegment srcSegment = Arena.ofAuto().allocate(src.remaining());
      MemorySegment.copy(src.array(), 0, srcSegment, ValueLayout.JAVA_BYTE, 0, src.remaining());
      long ret =
          Libzstd.ZSTD_compressCCtx(cctx, dstSegment, dst.remaining(), srcSegment, src.remaining(), level);
      if (Libzstd.ZSTD_isError(ret)!=0) {
        throw new IllegalArgumentException(Libzstd.ZSTD_getErrorName(ret).getString(0));
      }
      MemorySegment.copy(dstSegment, ValueLayout.JAVA_BYTE, 0, (Object) dst.array(), 0, (int) ret);
      return (int) ret;
    }

    public synchronized int compress(
        ByteBuffer dst, ByteBuffer src, CompressionDictionary cdict, int level) {
      MemorySegment dstSegment = Arena.ofAuto().allocate(dst.remaining());
      MemorySegment srcSegment = Arena.ofAuto().allocate(src.remaining());
      MemorySegment.copy(src.array(), 0, srcSegment, ValueLayout.JAVA_BYTE, 0, src.remaining());
      long ret;
      if (cdict == null) {
        ret = Libzstd.ZSTD_compressCCtx(cctx, dstSegment, dst.remaining(), srcSegment, src.remaining(), level);
      } else {
        ret =
            Libzstd
                .ZSTD_compress_usingCDict(
                    cctx, dstSegment, dst.remaining(), srcSegment, src.remaining(), cdict.cdict);
      }
      if (Libzstd.ZSTD_isError(ret)!=0) {
        throw new IllegalArgumentException(Libzstd.ZSTD_getErrorName(ret).getString(0));
      }
      MemorySegment.copy(dstSegment, ValueLayout.JAVA_BYTE, 0, (Object) dst.array(), 0, (int) ret);
      return (int) ret;
    }

    @Override
    public synchronized void close() throws IOException {
      if (closed == false) {
        closed = true;
        Libzstd.ZSTD_freeCCtx(cctx);
        cctx = null;
      }
    }
  }

  public static class Decompressor implements Closeable {

    private MemorySegment dctx;
    private boolean closed;

    Decompressor() {
      dctx = Libzstd.ZSTD_createDCtx();
      if (dctx == null) {
        throw new IllegalStateException("Can't allocate decompression context");
      }
    }

    public synchronized int decompress(
        ByteBuffer dst, ByteBuffer src, DecompressionDictionary ddict) {
      if (closed) {
        throw new IllegalStateException();
      }
      MemorySegment srcSegment = Arena.ofAuto().allocate(src.remaining());
      MemorySegment dstSegment = Arena.ofAuto().allocate(dst.remaining());
      MemorySegment.copy(src.array(), 0, srcSegment, ValueLayout.JAVA_BYTE, 0, src.remaining());
      long ret =
          Libzstd
              .ZSTD_decompress_usingDDict(
                  dctx, dstSegment, dst.remaining(), srcSegment, src.remaining(), ddict.ddict);
      if (Libzstd.ZSTD_isError(ret)!=0) {
        throw new IllegalArgumentException(Libzstd.ZSTD_getErrorName(ret).getString(0));
      }
      MemorySegment.copy(dstSegment, ValueLayout.JAVA_BYTE, 0L, (Object) dst.array(), 0, (int) ret);
      return (int) ret;
    }

    @Override
    public synchronized void close() throws IOException {
      if (closed == false) {
        closed = true;
        Libzstd.ZSTD_freeDCtx(dctx);
        dctx = null;
      }
    }
  }

  public static class CompressionDictionary implements Closeable {

    private final MemorySegment cdict;
    private boolean closed;

    private CompressionDictionary(MemorySegment cdict) {
      this.cdict = cdict;
      if (cdict == null) {
        throw new IllegalStateException("Can't allocate compression dictionary");
      }
    }

    @Override
    public synchronized void close() throws IOException {
      if (closed == false) {
        closed = true;
        Libzstd.ZSTD_freeCDict(cdict);
      }
    }
  }

  public static class DecompressionDictionary implements Closeable {

    private final MemorySegment ddict;
    private boolean closed;

    private DecompressionDictionary(MemorySegment ddict) {
      this.ddict = ddict;
      if (ddict == null) {
        throw new IllegalStateException("Can't allocate decompression dictionary");
      }
    }

    @Override
    public synchronized void close() throws IOException {
      if (closed == false) {
        closed = true;
        Libzstd.ZSTD_freeDDict(ddict);
      }
    }
  }

  /** Create a dictionary for compression. */
  public static CompressionDictionary createCompressionDictionary(ByteBuffer buf, int level) {
    MemorySegment dictBuffer = Arena.ofAuto().allocate(buf.remaining());
    MemorySegment.copy(buf.array(), 0, dictBuffer, ValueLayout.JAVA_BYTE, 0, buf.remaining());
    MemorySegment dict = Libzstd.ZSTD_createCDict(dictBuffer, buf.remaining(), level);
    return new CompressionDictionary(dict);
  }

  public static DecompressionDictionary createDecompressionDictionary(ByteBuffer buf) {
    MemorySegment dictBuffer = Arena.ofAuto().allocate(buf.remaining());
    MemorySegment.copy(buf.array(), 0, dictBuffer, ValueLayout.JAVA_BYTE, 0, buf.remaining());
    MemorySegment dict = Libzstd.ZSTD_createDDict(dictBuffer, buf.remaining());
    return new DecompressionDictionary(dict);
  }

  public static int getMaxCompressedLen(int srcLen) {
    return (int) Libzstd.ZSTD_compressBound(srcLen);
  }

  public static int getDecompressedLen(ByteBuffer src) {

    MemorySegment srcSegment = Arena.ofAuto().allocate(src.remaining());
    MemorySegment.copy(src.array(), 0, srcSegment, ValueLayout.JAVA_BYTE, 0, src.remaining());
    return (int) Libzstd.ZSTD_getFrameContentSize(srcSegment, src.remaining());

  }

}
