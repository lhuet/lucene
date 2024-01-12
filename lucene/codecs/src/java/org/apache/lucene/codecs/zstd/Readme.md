In order to build the codec with JDK22, the gradle files has been updated and `jdk22_home` is expected to be set.

Build command used i the codecs module:

`../../gradlew assemble -Pjdk22_home=/Users/laurent/.sdkman/candidates/java/22.ea.29-open`


The `StoredFieldsBenchmarkZstdPanama` class is just a modified version (original version in the luceneutil project: https://github.com/mikemccand/luceneutil/blob/master/src/main/perf/StoredFieldsBenchmark.java)
()

Benchmark test can be executed with a command like:

`/Users/laurent/.sdkman/candidates/java/22.ea.29-open/bin/java  -Djava.library.path=/opt/homebrew/Cellar/zstd/1.5.5/lib -cp ../lucene-gh/lucene/core/build/libs/lucene-core-10.0.0-SNAPSHOT.jar:../lucene-gh/lucene/codecs/build/libs/lucene-codecs-10.0.0-SNAPSHOT.jar org.apache.lucene.codecs.zstd.StoredFieldsBenchmarkZstdPanama /Users/laurent/Documents/Consulting/sandbox/zstd/benchmark/data/allCountries.txt /Users/laurent/Documents/Consulting/sandbox/zstd/benchmark/indices ZSTD5 100000`
