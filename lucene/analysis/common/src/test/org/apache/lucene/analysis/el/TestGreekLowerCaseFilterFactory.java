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
package org.apache.lucene.analysis.el;

import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.BaseTokenStreamFactoryTestCase;
import org.apache.lucene.analysis.TokenStream;

/** Simple tests to ensure the Greek lowercase filter factory is working. */
public class TestGreekLowerCaseFilterFactory extends BaseTokenStreamFactoryTestCase {
  /** Ensure the filter actually lowercases (and a bit more) greek text. */
  public void testNormalization() throws Exception {
    Reader reader = new StringReader("Μάϊος ΜΆΪΟΣ");
    TokenStream stream = whitespaceMockTokenizer(reader);
    stream = tokenFilterFactory("GreekLowerCase").create(stream);
    assertTokenStreamContents(stream, new String[] {"μαιοσ", "μαιοσ"});
  }

  /** Test that bogus arguments result in exception */
  public void testBogusArguments() throws Exception {
    IllegalArgumentException expected =
        expectThrows(
            IllegalArgumentException.class,
            () -> {
              tokenFilterFactory("GreekLowerCase", "bogusArg", "bogusValue");
            });
    assertTrue(expected.getMessage().contains("Unknown parameters"));
  }
}
