// Generated by jextract

package org.apache.lucene.panama.zstd;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;
final class constants$3 {

    // Suppresses default constructor, ensuring non-instantiability.
    private constants$3() {}
    static final MethodHandle const$0 = RuntimeHelper.downcallHandle(
        "ZSTD_minCLevel",
        constants$0.const$0
    );
    static final MethodHandle const$1 = RuntimeHelper.downcallHandle(
        "ZSTD_maxCLevel",
        constants$0.const$0
    );
    static final MethodHandle const$2 = RuntimeHelper.downcallHandle(
        "ZSTD_defaultCLevel",
        constants$0.const$0
    );
    static final MethodHandle const$3 = RuntimeHelper.downcallHandle(
        "ZSTD_createCCtx",
        constants$0.const$2
    );
    static final FunctionDescriptor const$4 = FunctionDescriptor.of(JAVA_LONG,
        RuntimeHelper.POINTER
    );
    static final MethodHandle const$5 = RuntimeHelper.downcallHandle(
        "ZSTD_freeCCtx",
        constants$3.const$4
    );
}


