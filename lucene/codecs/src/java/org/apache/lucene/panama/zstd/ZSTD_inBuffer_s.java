// Generated by jextract

package org.apache.lucene.panama.zstd;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;
/**
 * {@snippet :
 * struct ZSTD_inBuffer_s {
 *     void* src;
 *     unsigned long size;
 *     unsigned long pos;
 * };
 * }
 */
public class ZSTD_inBuffer_s {

    public static MemoryLayout $LAYOUT() {
        return constants$7.const$3;
    }
    public static VarHandle src$VH() {
        return constants$7.const$4;
    }
    /**
     * Getter for field:
     * {@snippet :
     * void* src;
     * }
     */
    public static MemorySegment src$get(MemorySegment seg) {
        return (java.lang.foreign.MemorySegment)constants$7.const$4.get(seg, 0L);
    }
    /**
     * Setter for field:
     * {@snippet :
     * void* src;
     * }
     */
    public static void src$set(MemorySegment seg, MemorySegment x) {
        constants$7.const$4.set(seg, 0L, x);
    }
    public static MemorySegment src$get(MemorySegment seg, long index) {
        return (java.lang.foreign.MemorySegment)constants$7.const$4.get(seg, index * sizeof());    }
    public static void src$set(MemorySegment seg, long index, MemorySegment x) {
        constants$7.const$4.set(seg, index * sizeof(), x);
    }
    public static VarHandle size$VH() {
        return constants$7.const$5;
    }
    /**
     * Getter for field:
     * {@snippet :
     * unsigned long size;
     * }
     */
    public static long size$get(MemorySegment seg) {
        return (long)constants$7.const$5.get(seg, 0L);
    }
    /**
     * Setter for field:
     * {@snippet :
     * unsigned long size;
     * }
     */
    public static void size$set(MemorySegment seg, long x) {
        constants$7.const$5.set(seg, 0L, x);
    }
    public static long size$get(MemorySegment seg, long index) {
        return (long)constants$7.const$5.get(seg, index * sizeof());    }
    public static void size$set(MemorySegment seg, long index, long x) {
        constants$7.const$5.set(seg, index * sizeof(), x);
    }
    public static VarHandle pos$VH() {
        return constants$8.const$0;
    }
    /**
     * Getter for field:
     * {@snippet :
     * unsigned long pos;
     * }
     */
    public static long pos$get(MemorySegment seg) {
        return (long)constants$8.const$0.get(seg, 0L);
    }
    /**
     * Setter for field:
     * {@snippet :
     * unsigned long pos;
     * }
     */
    public static void pos$set(MemorySegment seg, long x) {
        constants$8.const$0.set(seg, 0L, x);
    }
    public static long pos$get(MemorySegment seg, long index) {
        return (long)constants$8.const$0.get(seg, index * sizeof());    }
    public static void pos$set(MemorySegment seg, long index, long x) {
        constants$8.const$0.set(seg, index * sizeof(), x);
    }
    public static long sizeof() { return $LAYOUT().byteSize(); }
    public static MemorySegment allocate(SegmentAllocator allocator) { return allocator.allocate($LAYOUT()); }
    public static MemorySegment allocateArray(long len, SegmentAllocator allocator) {
        return allocator.allocate(MemoryLayout.sequenceLayout(len, $LAYOUT()));
    }
    public static MemorySegment ofAddress(MemorySegment addr, Arena scope) { return RuntimeHelper.asArray(addr, $LAYOUT(), 1, scope); }
}


