Libzstd generated code with jextract for JDK 22

Prerequisites:

- JDK22 ea installed 
  - SDKMAN command used: `sdk install java 22.ea.29-open`
- Build Jextract for JDK22 (branch jdk22)
  - Repo: https://github.com/openjdk/jextract/tree/jdk22
  - LLVM required for the build
  - Command used: `./gradlew -Pjdk22_home=/Users/laurent/.sdkman/candidates/java/22.ea.29-open -Pllvm_home=/Library/Developer/CommandLineTools/usr/ clean verify`
- Lib ztsd installed
  - Test with zstd 1.5.5 installed with `brew` (`brew install zstd`)

Code generation:

As libzstd comes with several header files and jextract use only 1 header file, a 'wrapper' header file (named `libzstd.h` here) has been used.
The content of this file is only include directives:

```
#include "zstd.h"
#include "zdict.h"
#include "zstd_errors.h"
```

Command line used for generated the Java 'bindings':

`../jextract/build/jextract/bin/jextract --source -t org.apache.lucene.panama.zstd -l zstd -I /opt/homebrew/Cellar/zstd/1.5.5/include --header-class-name Libzstd libzstd.h`

